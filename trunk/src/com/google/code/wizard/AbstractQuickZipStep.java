package com.google.code.wizard;

import com.google.code.action.ZipFragmentAddAction;
import com.google.code.model.ZipFragmentContainerModel;
import com.google.code.model.ZipFragmentModel;
import com.google.code.action.ZipFragmentRemoveAction;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 12/19/10
 * Time: 4:53 AM
 */
public abstract class AbstractQuickZipStep implements QuickZipStep, ZipFragmentContainerModel {
   protected final Project myProject;
   protected final java.util.List<ZipFragmentModel> mySelectedFragments;
   protected final QuickZipWizard mParentWizard;
   protected final DefaultMutableTreeNode myRoot = new DefaultMutableTreeNode("Root");
   protected final Tree myTree = new Tree(myRoot);
   private final Splitter myPanel = new Splitter();
   protected final JPanel myWholePanel = new JPanel(new BorderLayout());
   protected final float proportion = 0.3f;

   public AbstractQuickZipStep(final Project project, final List<ZipFragmentModel> selectedFragments, final QuickZipWizard parentWizard) {
      super();
      myProject = project;
      mySelectedFragments = selectedFragments;
      mParentWizard = parentWizard;
   }

   public static void sortTree(final DefaultMutableTreeNode root) {
      TreeUtil.sort(root, new Comparator() {
         public int compare(final Object o1, final Object o2) {
            final Object userObject1 = ((DefaultMutableTreeNode) o1).getUserObject();
            final Object userObject2 = ((DefaultMutableTreeNode) o2).getUserObject();

            if (userObject1 instanceof String) {
               return 1;
            } else if (userObject2 instanceof String) {
               return -1;
            } else if (userObject1 instanceof ZipFragmentModel && userObject2 instanceof ZipFragmentModel) {
               final ZipFragmentModel fragment1 = (ZipFragmentModel) userObject1;
               final ZipFragmentModel fragment2 = (ZipFragmentModel) userObject2;
               return fragment1.getName().compareTo(fragment2.getName());
            }

            return 0;
         }
      });
   }

   public abstract String getHelpId();

   public void disposeUIResources() {
      // TODO Doing nothing yet
   }

   public void addFragment(final DefaultMutableTreeNode newNode) {
      final ZipFragmentModel fragmentModel = (ZipFragmentModel) newNode.getUserObject();
      mySelectedFragments.add(fragmentModel);
      myRoot.add(newNode);
   }

   public void removeFragment(final ZipFragmentModel fragmentModel) {
      mySelectedFragments.remove(fragmentModel);
   }

   public JTree getTreeComponent() {
      return myTree;
   }

   public void _init() {
      myRoot.removeAllChildren();
      for (final ZipFragmentModel loopModel : mySelectedFragments) {
         // Add initial nodes to the tree
         final DefaultMutableTreeNode initialNode = new DefaultMutableTreeNode(loopModel);
         myRoot.add(initialNode);
      }
   }

   public void _commit(final boolean finishChosen) throws CommitStepException {
      // Do nothing
   }

   public Icon getIcon() {
      return null;
   }

   public JComponent getComponent() {
      return createCenterPanel();
   }

   protected JComponent createCenterPanel() {
      myPanel.setHonorComponentsMinimumSize(true);
      myPanel.setShowDividerControls(true);
      myPanel.setProportion(proportion);
      myPanel.setHonorComponentsMinimumSize(true);

      // Add the fragment list on the left
      myPanel.setFirstComponent(createLeftSectionForCenterPanel());
      // Add the file selection tree on the right
      myPanel.setSecondComponent(createRightSectionForCenterPanel());

      myWholePanel.add(myPanel, BorderLayout.CENTER);

      mParentWizard.updateDialog();

      final Dimension d = myWholePanel.getPreferredSize();
      myWholePanel.setMinimumSize(d);
      myWholePanel.setPreferredSize(d);

      return myWholePanel;
   }

   protected abstract boolean addToolbarForLeftSection();

   private JPanel createLeftSectionForCenterPanel() {
      final JPanel leftPanel = new JPanel(new BorderLayout());

      if (addToolbarForLeftSection()) {
         final JComponent toolbarComponent = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
               createActionsGroup(), true).getComponent();
         leftPanel.add(toolbarComponent, BorderLayout.NORTH);
      }

      initTree();

      final JScrollPane pane = ScrollPaneFactory.createScrollPane(myTree);
      pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      leftPanel.add(pane, BorderLayout.CENTER);

      final JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
      leftPanel.add(bottomPanel, BorderLayout.SOUTH);

      return leftPanel;
   }

   private DefaultActionGroup createActionsGroup() {
      final DefaultActionGroup group = new DefaultActionGroup();
      group.add(new ZipFragmentAddAction(this, myProject));
      group.add(new ZipFragmentRemoveAction(this));
      return group;
   }

   private void initTree() {
      myTree.setRootVisible(false);
      myTree.setShowsRootHandles(true);
      UIUtil.setLineStyleAngled(myTree);
      TreeUtil.installActions(myTree);
      PopupHandler.installFollowingSelectionTreePopup(myTree, createActionsGroup(), ActionPlaces.UNKNOWN, ActionManager.getInstance());

      myTree.setCellRenderer(getTreeCellRenderer());

      myTree.addTreeSelectionListener(getTreeSelectionListener());
      myTree.registerKeyboardAction(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            mParentWizard.clickDefaultButton();
         }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            myTree.requestFocusInWindow();
            TreeUtil.selectFirstNode(myTree);
         }
      });

      sortTree(myRoot);
      ((DefaultTreeModel) myTree.getModel()).reload();
   }

   private TreeSelectionListener getTreeSelectionListener() {
      return new TreeSelectionListener() {
         public void valueChanged(TreeSelectionEvent e) {
            final TreePath selectionPath = myTree.getSelectionPath();
            if (selectionPath != null) {
               final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
               final Object userObject = node.getUserObject();
               updateRightPanel((ZipFragmentModel) userObject);
            }
            mParentWizard.updateDialog();
         }
      };
   }

   private ColoredTreeCellRenderer getTreeCellRenderer() {
      return new ColoredTreeCellRenderer() {
         public void customizeCellRenderer(final JTree tree, final Object value, final boolean selected,
               final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode) {
               final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
               final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
               final Object userObject = node.getUserObject();
               if (userObject instanceof ZipFragmentModel) {
                  final ZipFragmentModel fragmentModel = (ZipFragmentModel) userObject;
                  append(fragmentModel.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
               } else if (userObject instanceof String) {
                  append((String) userObject, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
               } else {
                  throw new IllegalStateException("Unknown user object type: " + userObject);
               }
            }
         }
      };
   }

   protected abstract JPanel createRightSectionForCenterPanel();

   protected abstract void updateRightPanel(final ZipFragmentModel fragmentModel);

   protected void setupDialogBounds() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            UIUtil.setupEnclosingDialogBounds(myWholePanel);
         }
      });
   }
}
