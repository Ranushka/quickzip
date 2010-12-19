package com.google.code.tree;

import com.google.code.util.QuickZipBundle;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.DiffBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Date: 12/19/10
 * Time: 2:07 AM
 */
public abstract class AbstractFileSelectionTree<T> extends JPanel {
   protected static final String ROOT = "root";

   protected final Tree myTree;
   protected final JScrollPane myTreeScrollPane;
   protected final Project myProject;
   protected final T myModel;
   protected Runnable myDoubleClickHandler = EmptyRunnable.getInstance();

   public AbstractFileSelectionTree(final Project project, final T model, final String borderLabelKey) {
      myProject = project;
      myModel = model;

      setLayout(new BorderLayout());

      final int checkboxWidth = new JCheckBox().getPreferredSize().width;
      myTree = new Tree(new DefaultMutableTreeNode(ROOT)) {
         public Dimension getPreferredScrollableViewportSize() {
            final Dimension size = super.getPreferredScrollableViewportSize();
            return new Dimension(size.width + 10, size.height);
         }

         protected void processMouseEvent(final MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
               final int row = myTree.getRowForLocation(e.getX(), e.getY());
               if (row >= 0) {
                  final Rectangle baseRect = myTree.getRowBounds(row);
                  baseRect.setSize(checkboxWidth, baseRect.height);
                  if (baseRect.contains(e.getPoint())) {
                     myTree.setSelectionRow(row);
                     toggleSelection();
                  }
               }
            }
            super.processMouseEvent(e);
         }

         public int getToggleClickCount() {
            return -1;
         }
      };

      myTree.setRootVisible(false);
      myTree.setShowsRootHandles(true);

      myTree.setCellRenderer(new VirtualFilesTreeCellRenderer(this));

      displayFilesInTree();
      // TODO Enable tree speed search

      myTreeScrollPane = ScrollPaneFactory.createScrollPane(myTree);
      setScrollPaneBorder(new TitledBorder(QuickZipBundle.message(borderLabelKey)));
      add(myTreeScrollPane, BorderLayout.CENTER);

      new MyToggleSelectionAction().registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)), this);

      registerKeyboardAction(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            includeSelection();
         }

      }, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

      registerKeyboardAction(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            excludeSelection();
         }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

      myTree.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            final int row = myTree.getRowForLocation(e.getPoint().x, e.getPoint().y);
            if (row >= 0) {
               final Rectangle baseRect = myTree.getRowBounds(row);
               baseRect.setSize(checkboxWidth, baseRect.height);
               if (!baseRect.contains(e.getPoint()) && e.getClickCount() == 2) {
                  myDoubleClickHandler.run();
                  e.consume();
               }
            }
         }
      });

      showTreeView();

      String emptyText = StringUtil.capitalize(DiffBundle.message("diff.count.differences.status.text", 0));
      myTree.getEmptyText().setText(emptyText);
   }

   public void setDoubleClickHandler(final Runnable doubleClickHandler) {
      myDoubleClickHandler = doubleClickHandler;
   }

   @Override
   public Dimension getMinimumSize() {
      return new Dimension(400, 400);
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(400, 400);
   }

   public void setScrollPaneBorder(final Border border) {
      myTreeScrollPane.setBorder(border);
   }

   public void showTreeView() {
      final List<String> wasSelected = getSelectedItemsInTree();
      select(wasSelected);
      if (myTree.hasFocus()) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               requestFocus();
            }
         });
      }
   }

   public void requestFocus() {
      myTree.requestFocus();
   }

   protected String convertUserObjectToString(Object obj) {
      if (obj instanceof VirtualFile) {
         return ((VirtualFile) obj).getPath();
      }
      return String.valueOf(obj);
   }

   public void displayFilesInTree() {
      final DefaultTreeModel model = buildTreeModel();
      myTree.setModel(model);

      TreeUtil.sort((DefaultMutableTreeNode) model.getRoot(), new Comparator() {
         public int compare(final Object o1, final Object o2) {
            final Object userObject1 = ((DefaultMutableTreeNode) o1).getUserObject();
            final Object userObject2 = ((DefaultMutableTreeNode) o2).getUserObject();

            return convertUserObjectToString(userObject1).compareTo(convertUserObjectToString(userObject2));
         }
      });

      final Runnable runnable = new Runnable() {
         public void run() {
            if (myProject.isDisposed()) {
               return;
            }

            TreeUtil.expandAll(myTree);

            int scrollRow = 0;
            if (getSelectedItemsInModel().size() > 0) {
               final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

               handleUnselectedNodes(root);
               scrollRow = getScrollRowIndex(root);
            }

            myTree.setSelectionRow(scrollRow);
            TreeUtil.showRowCentered(myTree, scrollRow, false);
         }
      };
      if (ApplicationManager.getApplication().isDispatchThread()) {
         runnable.run();
      } else {
         SwingUtilities.invokeLater(runnable);
      }
   }

   protected void handleUnselectedNodes(final DefaultMutableTreeNode pRoot) {
      // Collapse unselected nodes
      final Enumeration enumeration = pRoot.depthFirstEnumeration();
      while (enumeration.hasMoreElements()) {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
         final CheckBoxSelectionStateEnum state = getNodeStatus(node);
         if (node != pRoot && state == CheckBoxSelectionStateEnum.CLEAR) {
            myTree.collapsePath(new TreePath(node.getPath()));
         }
      }
   }

   private int getScrollRowIndex(final DefaultMutableTreeNode pRoot) {
      // Scroll to item which is selected
      final Enumeration enumeration = pRoot.depthFirstEnumeration();
      while (enumeration.hasMoreElements()) {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
         final CheckBoxSelectionStateEnum state = getNodeStatus(node);
         if (state == CheckBoxSelectionStateEnum.FULL && node.isLeaf()) {
            return myTree.getRowForPath(new TreePath(node.getPath()));
         }
      }
      return 0;
   }

   protected abstract Collection<String> getSelectedItemsInModel();

   protected abstract DefaultTreeModel buildTreeModel();

   public abstract ActionGroup getTreeActions();

   @SuppressWarnings({"SuspiciousMethodCalls"})
   protected void toggleSelection() {
      boolean hasExcluded = false;
      final Collection<String> selectedItemsInModel = getSelectedItemsInModel();
      for (final String value : getSelectedItemsInTree()) {
         if (!selectedItemsInModel.contains(value)) {
            hasExcluded = true;
         }
      }

      if (hasExcluded) {
         includeSelection();
      } else {
         excludeSelection();
      }

      repaint();
   }

   protected abstract void includeSelection();

   protected abstract void excludeSelection();

   @NotNull
   public List<String> getSelectedItemsInTree() {
      final Set<String> changes = new HashSet<String>();

      final TreePath[] paths = myTree.getSelectionPaths();

      if (paths != null) {
         for (final TreePath path : paths) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            final List<String> objects = getObjectsFromSelectedTreeNode(node, true);
            for (final String object : objects) {
               changes.add(object);
            }
         }
      }

      return new LinkedList<String>(changes);
   }

   protected List<String> getObjectsFromSelectedTreeNode(final DefaultMutableTreeNode node, final boolean addParentObject) {
      // All nodes under a selected node are selected implicitly
      final List<String> resultList = new LinkedList<String>();
      addSelectedObjectsToList(resultList, node, addParentObject);
      return resultList;
   }

   private void addSelectedObjectsToList(final List<String> resultList, final DefaultMutableTreeNode parentNode, final boolean addParentObject) {
      if (addParentObject) {
         resultList.add(convertUserObjectToString(parentNode.getUserObject()));
      }

      for (int i = 0; i < parentNode.getChildCount(); i++) {
         addSelectedObjectsToList(resultList, (DefaultMutableTreeNode) parentNode.getChildAt(i), true);
      }
   }

   protected CheckBoxSelectionStateEnum getNodeStatus(final DefaultMutableTreeNode node) {
      boolean hasIncluded = false;
      boolean hasExcluded = false;

      final Collection<String> selectedItemsInModel = getSelectedItemsInModel();
      for (final String change : getObjectsFromSelectedTreeNode(node, true)) {
         if (selectedItemsInModel.contains(change)) {
            hasIncluded = true;
         } else {
            hasExcluded = true;
         }
      }

      final CheckBoxSelectionStateEnum resultState;
      if (hasIncluded && hasExcluded) {
         resultState = CheckBoxSelectionStateEnum.PARTIAL;
      } else if (hasIncluded) {
         resultState = CheckBoxSelectionStateEnum.FULL;
      } else {
         resultState = CheckBoxSelectionStateEnum.CLEAR;
      }
      return resultState;
   }

   protected List<String> getAllUserObjectsInTree() {
      final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) myTree.getModel().getRoot();
      return getObjectsFromSelectedTreeNode(rootNode, false);
   }

   public void select(final List<String> changes) {
      final DefaultTreeModel treeModel = (DefaultTreeModel) myTree.getModel();
      final TreeNode root = (TreeNode) treeModel.getRoot();
      final List<TreePath> treeSelection = new ArrayList<TreePath>(changes.size());
      TreeUtil.traverse(root, new TreeUtil.Traverse() {
         public boolean accept(Object node) {
            final String change = convertUserObjectToString(((DefaultMutableTreeNode) node).getUserObject());
            if (changes.contains(change)) {
               treeSelection.add(new TreePath(((DefaultMutableTreeNode) node).getPath()));
            }
            return true;
         }
      });
      myTree.setSelectionPaths(treeSelection.toArray(new TreePath[treeSelection.size()]));
   }

   protected class MyToggleSelectionAction extends AnAction {
      public void actionPerformed(AnActionEvent e) {
         toggleSelection();
      }
   }
}
