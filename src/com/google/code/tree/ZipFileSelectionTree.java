package com.google.code.tree;

import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipHelper;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.actions.CollapseAllAction;
import com.intellij.ui.treeStructure.actions.ExpandAllAction;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 12/18/10
 * Time: 8:08 PM
 */
public class ZipFileSelectionTree extends AbstractFileSelectionTree<List<ZipFragmentModel>> {

   public ZipFileSelectionTree(final Project project, final List<ZipFragmentModel> model) {
      super(project, model, "label.fragment.folder-structure-in-zip");
   }

   @Override
   protected CheckBoxSelectionStateEnum getNodeStatus(final DefaultMutableTreeNode node) {
      // All checkboxes have the full state
      return CheckBoxSelectionStateEnum.FULL;
   }

   @Override
   protected DefaultTreeModel buildTreeModel() {

      final TreeModel currentTreeModel = myTree.getModel();
      final DefaultMutableTreeNode currentRootNode = (DefaultMutableTreeNode) currentTreeModel.getRoot();

      final DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(currentRootNode.getUserObject());
      for (final ZipFragmentModel fragmentModel : myModel) {
         addChildrenToNodeFromVirtualFile(newRootNode, myProject.getBaseDir(), fragmentModel);
      }

      return new DefaultTreeModel(newRootNode);
   }

   private void addChildrenToNodeFromVirtualFile(final DefaultMutableTreeNode parentNode, final VirtualFile virtualFile, final ZipFragmentModel fragmentModel) {
      if (virtualFile.isValid()) {
         final String virtualFilePath = virtualFile.getPath();
         if (virtualFilePath.startsWith(fragmentModel.getParentFolder())) {
            if (virtualFilePath.equals(fragmentModel.getParentFolder()) ||
                  fragmentModel.isPrefixForSomeSelected(virtualFilePath)) {
               final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(virtualFile);
               parentNode.add(newNode);
               for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
                  addChildrenToNodeFromVirtualFile(newNode, loopChildVirtualFile, fragmentModel);
               }
            }
         } else if (fragmentModel.getParentFolder().startsWith(virtualFilePath)) {
            for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
               addChildrenToNodeFromVirtualFile(parentNode, loopChildVirtualFile, fragmentModel);
            }
         }
      }
   }

   @Override
   protected Collection<String> getSelectedItemsInModel() {
      final List<List<String>> rawFilesToZip = QuickZipHelper.getFilesStructureToZip(myProject, myModel);
      final List<String> filesToZip = new LinkedList<String>();
      for (final List<String> loopData : rawFilesToZip) {
         filesToZip.addAll(loopData);
      }
      return filesToZip;
   }

   protected void includeSelection() {
      // Nothing to do
   }

   protected void excludeSelection() {
      // Nothing to do
   }

   protected void notifyInclusionListener() {
      // Nothing to do
   }

   public ActionGroup getTreeActions() {

      final DefaultActionGroup group = new DefaultActionGroup();

      final ExpandAllAction expandAllAction = new ExpandAllAction(myTree);
      final CollapseAllAction collapseAllAction = new CollapseAllAction(myTree);

      group.add(expandAllAction);
      group.add(collapseAllAction);

      expandAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeymapManager.getInstance().getActiveKeymap().getShortcuts(IdeActions.ACTION_EXPAND_ALL)),
            myTree);
      collapseAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeymapManager.getInstance().getActiveKeymap().getShortcuts(IdeActions.ACTION_COLLAPSE_ALL)),
            myTree);

      return group;
   }
}
