package com.google.code.tree;

import com.google.code.tree.AbstractFileSelectionTree;
import com.google.code.tree.CheckBoxSelectionStateEnum;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Date: 12/18/10
 * Time: 8:12 PM
 */
public class VirtualFilesTreeCellRenderer extends JPanel implements TreeCellRenderer {

   private static final Icon FOLDER_ICON = IconLoader.getIcon("/help/icons/folder-16.png");
   private static final Icon FILE_ICON = IconLoader.getIcon("/help/icons/file-16.png");

   private final AbstractFileSelectionTree myZipFilesTreeList;
   private final ColoredTreeCellRenderer myTextRenderer;
   private final JCheckBox myCheckBox;

   public VirtualFilesTreeCellRenderer(final AbstractFileSelectionTree zipFilesTreeList) {
      super(new BorderLayout());
      myZipFilesTreeList = zipFilesTreeList;
      myCheckBox = new JCheckBox();
      myTextRenderer = new ColoredTreeCellRenderer() {
         @Override
         public void customizeCellRenderer(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            final Object userObject = node.getUserObject();
            if (userObject instanceof VirtualFile) {
               final VirtualFile virtualFile = (VirtualFile) userObject;
               append(virtualFile.getName());
               setIcon(virtualFile.isDirectory() ? FOLDER_ICON : FILE_ICON);
            } else {
               append(userObject.toString());
            }
         }
      };

      add(myCheckBox, BorderLayout.WEST);
      add(myTextRenderer, BorderLayout.CENTER);
   }

   public Component getTreeCellRendererComponent(JTree tree,
         Object value,
         boolean selected,
         boolean expanded,
         boolean leaf,
         int row,
         boolean hasFocus) {

      if (UIUtil.isUnderGTKLookAndFeel() || UIUtil.isUnderNimbusLookAndFeel()) {
         NonOpaquePanel.setTransparent(this);
         NonOpaquePanel.setTransparent(myCheckBox);
      } else {
         setBackground(null);
         myCheckBox.setBackground(null);
      }


      myTextRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

      CheckBoxSelectionStateEnum state = myZipFilesTreeList.getNodeStatus(node);
      myCheckBox.setSelected(state != CheckBoxSelectionStateEnum.CLEAR);
      myCheckBox.setEnabled(state != CheckBoxSelectionStateEnum.PARTIAL);
      revalidate();

      return this;

   }
}