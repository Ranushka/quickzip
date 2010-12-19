package com.google.code.tree;

import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.actions.CollapseAllAction;
import com.intellij.ui.treeStructure.actions.ExpandAllAction;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;

/**
 * Date: 12/18/10
 * Time: 8:08 PM
 */
public class FileToZipSelectionTree extends AbstractFileSelectionTree<ZipFragmentModel> {

   public FileToZipSelectionTree(final Project project, final ZipFragmentModel model) {
      super(project, model, "label.fragment.select-files");
   }

   @Override
   protected DefaultTreeModel buildTreeModel() {
      final TreeModel currentTreeModel = myTree.getModel();
      final DefaultMutableTreeNode currentRootNode = (DefaultMutableTreeNode) currentTreeModel.getRoot();

      final DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(currentRootNode.getUserObject());
      addChildrenToNodeFromVirtualFile(newRootNode, myProject.getBaseDir());

      return new DefaultTreeModel(newRootNode);
   }

   private void addChildrenToNodeFromVirtualFile(final DefaultMutableTreeNode parentNode, final VirtualFile virtualFile) {
      if (virtualFile.isValid()) {
         final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(virtualFile);
         parentNode.add(newNode);
         for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
            addChildrenToNodeFromVirtualFile(newNode, loopChildVirtualFile);
         }
      }
   }

   @Override
   protected Collection<String> getSelectedItemsInModel() {
      return myModel.getSelectedFiles();
   }

   protected void includeSelection() {
      for (final String change : getSelectedItemsInTree()) {
         myModel.addFile(change);
      }
      notifyInclusionListener();
      repaint();
   }

   @SuppressWarnings({"SuspiciousMethodCalls"})
   protected void excludeSelection() {
      for (final String change : getSelectedItemsInTree()) {
         myModel.removeFile(change);
      }
      notifyInclusionListener();
      repaint();
   }

   protected void notifyInclusionListener() {
      // Update fragment model to fix up intermediate nodes
      final DefaultTreeModel treeModel = (DefaultTreeModel) myTree.getModel();
      final TreeNode root = (TreeNode) treeModel.getRoot();

      // Traverse the tree nodes in bottom-up manner
      TreeUtil.traverse(root, new TreeUtil.Traverse() {
         public boolean accept(final Object node) {
            final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            final java.util.List<String> treeNodeObjects = getObjectsFromSelectedTreeNode(treeNode, false);

            if (!treeNodeObjects.isEmpty()) {
               final String userObject = convertUserObjectToString(treeNode.getUserObject());
               if (myModel.containsAllOfTheFiles(treeNodeObjects)) {
                  // All children selected, add self to selected files list in fragment
                  myModel.addFile(userObject);
               } else if (myModel.containsNoneOfTheFiles(treeNodeObjects)) {
                  // None of the children selected, remove self from selected files list in fragment
                  myModel.removeFile(userObject);
               }
            }
            return true;
         }
      });
   }

   public ActionGroup getTreeActions() {

      final DefaultActionGroup group = new DefaultActionGroup();

      final ExpandAllAction expandAllAction = new ExpandAllAction(myTree);
      final CollapseAllAction collapseAllAction = new CollapseAllAction(myTree);
      final SelectAllAction selectAllAction = new SelectAllAction();
      final DeselectAllAction deselectAllAction = new DeselectAllAction();

      group.add(expandAllAction);
      group.add(collapseAllAction);
      group.add(selectAllAction);
      group.add(deselectAllAction);

      expandAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeymapManager.getInstance().getActiveKeymap().getShortcuts(IdeActions.ACTION_EXPAND_ALL)),
            myTree);
      collapseAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeymapManager.getInstance().getActiveKeymap().getShortcuts(IdeActions.ACTION_COLLAPSE_ALL)),
            myTree);
      selectAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_A, SystemInfo.isMac ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK)),
            this);
      deselectAllAction.registerCustomShortcutSet(
            new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_A, (SystemInfo.isMac ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK) + KeyEvent.SHIFT_DOWN_MASK)),
            this);

      return group;
   }

   protected class SelectAllAction extends AnAction {
      protected SelectAllAction() {
         super(QuickZipBundle.message("select-all.quick-zip.action.name"),
               QuickZipBundle.message("select-all.quick-zip.action.description"),
               IconLoader.getIcon("/actions/selectall.png"));
      }

      @Override
      public void actionPerformed(final AnActionEvent e) {

         final List<String> allObjectsInTree = getAllUserObjectsInTree();
         myModel.addFiles(allObjectsInTree);

         FileToZipSelectionTree.this.repaint();
      }

      @Override
      public void update(final AnActionEvent e) {
         final List<String> allObjectsInTree = getAllUserObjectsInTree();
         e.getPresentation().setEnabled(getSelectedItemsInModel().size() != allObjectsInTree.size());
         super.update(e);
      }
   }

   protected class DeselectAllAction extends AnAction {
      protected DeselectAllAction() {
         super(QuickZipBundle.message("deselect-all.quick-zip.action.name"),
               QuickZipBundle.message("deselect-all.quick-zip.action.description"),
               IconLoader.getIcon("/actions/unselectall.png"));
      }

      @Override
      public void actionPerformed(final AnActionEvent e) {

         final List<String> allObjectsInTree = getAllUserObjectsInTree();
         myModel.removeAllFiles(allObjectsInTree);

         FileToZipSelectionTree.this.repaint();
      }

      @Override
      public void update(final AnActionEvent e) {
         e.getPresentation().setEnabled(getSelectedItemsInModel().size() > 0);
         super.update(e);
      }
   }
}
