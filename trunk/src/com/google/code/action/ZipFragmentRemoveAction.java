package com.google.code.action;

import com.google.code.model.ZipFragmentContainerModel;
import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Date: 12/18/10
 * Time: 10:25 AM
 */
public class ZipFragmentRemoveAction extends AnAction {

   private static final Icon REMOVE_ICON = IconLoader.getIcon("/general/remove.png");

   private final ZipFragmentContainerModel mContainerModel;

   public ZipFragmentRemoveAction(final ZipFragmentContainerModel selectionDialog) {
      super(QuickZipBundle.message("remove.quick-zip.action.name"),
            QuickZipBundle.message("remove.quick-zip.action.name"),
            REMOVE_ICON);
      mContainerModel = selectionDialog;
      registerCustomShortcutSet(CommonShortcuts.DELETE, mContainerModel.getTreeComponent());
   }

   public void actionPerformed(final AnActionEvent e) {
      final TreePath[] selections = mContainerModel.getTreeComponent().getSelectionPaths();
      mContainerModel.getTreeComponent().clearSelection();

      int nodeIndexToSelect = -1;
      DefaultMutableTreeNode parentToSelect = null;

      for (final TreePath each : selections) {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) each.getLastPathComponent();

         final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
         nodeIndexToSelect = parent.getIndex(node);
         parentToSelect = parent;
         parent.remove(node);

         mContainerModel.removeFragment((ZipFragmentModel) node.getUserObject());
      }

      ((DefaultTreeModel) mContainerModel.getTreeComponent().getModel()).reload();

      final TreeModel treeModel = mContainerModel.getTreeComponent().getModel();
      final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
      if (rootNode.getChildCount() != 0 && parentToSelect != null) {
         final TreeNode nodeToSelect = nodeIndexToSelect < parentToSelect.getChildCount()
               ? parentToSelect.getChildAt(nodeIndexToSelect)
               : parentToSelect.getChildAt(nodeIndexToSelect - 1);
         TreeUtil.selectInTree((DefaultMutableTreeNode) nodeToSelect, true, mContainerModel.getTreeComponent());
      }
   }

   public void update(final AnActionEvent e) {
      boolean enabled = false;
      final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) mContainerModel.getTreeComponent().getModel().getRoot();
      if (rootNode.getChildCount() > 1) {
         final TreePath[] selectionPaths = mContainerModel.getTreeComponent().getSelectionPaths();
         if (selectionPaths != null) {
            for (final TreePath loopSelection : selectionPaths) {
               final DefaultMutableTreeNode node = (DefaultMutableTreeNode) loopSelection.getLastPathComponent();
               final Object userObject = node.getUserObject();
               if (userObject != null) {
                  enabled = true;
                  break;
               }
            }
         }
      }
      e.getPresentation().setEnabled(enabled);
   }
}
