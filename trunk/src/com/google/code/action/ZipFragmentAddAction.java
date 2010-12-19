package com.google.code.action;

import com.google.code.model.ZipFragmentContainerModel;
import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collections;
import java.util.Date;

/**
 * Date: 12/18/10
 * Time: 10:28 AM
 */
public class ZipFragmentAddAction extends AnAction {

   private static final Icon ADD_ICON = IconLoader.getIcon("/general/add.png");

   private final ZipFragmentContainerModel myContainerModel;
   private final Project myProject;

   public ZipFragmentAddAction(final ZipFragmentContainerModel selectionDialog, final Project project) {
      super(QuickZipBundle.message("add.new.quick-zip.action.name"),
            QuickZipBundle.message("add.new.quick-zip.action.name"),
            ADD_ICON);
      myContainerModel = selectionDialog;
      myProject = project;

      registerCustomShortcutSet(CommonShortcuts.INSERT, myContainerModel.getTreeComponent());
   }

   public void actionPerformed(AnActionEvent e) {
      myContainerModel.getTreeComponent().clearSelection();

      final ZipFragmentModel newFragment = new ZipFragmentModel("QuickZip-" + new Date().getTime(),
            Collections.<String>emptyList(), myProject.getBaseDir().getPath());
      final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFragment);
      myContainerModel.addFragment(newNode);

      ((DefaultTreeModel) myContainerModel.getTreeComponent().getModel()).reload();

      TreeUtil.selectInTree(newNode, false, myContainerModel.getTreeComponent());
   }
}