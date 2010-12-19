package com.google.code.wizard;

import com.google.code.model.ZipFragmentModel;
import com.google.code.tree.FileToZipSelectionTree;
import com.google.code.util.QuickZipBundle;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

/**
 * Date: 12/19/10
 * Time: 2:54 AM
 */
public class SelectFilesStep extends AbstractQuickZipStep {

   private final JPanel myRightPanel = new JPanel(new BorderLayout());
   private final JTextField myNameTextField = new JTextField(30);

   public SelectFilesStep(final Project project, final List<ZipFragmentModel> selectedFragments, final QuickZipWizard parentWizard) {
      super(project, selectedFragments, parentWizard);
   }

   @Override
   public void _commit(final boolean finishChosen) throws CommitStepException {
      for (final ZipFragmentModel selectedFragment : mySelectedFragments) {
         // First validate the fragment names and selected files
         validateFragment(selectedFragment);
         // Now update the base folder
         setBaseFolderFromVirtualFile(selectedFragment, myProject.getBaseDir());
      }
   }

   private void validateFragment(final ZipFragmentModel selectedFragment) throws CommitStepException {
      final String loopFragmentName = selectedFragment.getName();
      if (loopFragmentName == null || loopFragmentName.trim().isEmpty()) {
         throw new CommitStepException(QuickZipBundle.message("error.fragment-empty-name"));
      }
      if (selectedFragment.getSelectedFiles().size() == 0) {
         throw new CommitStepException(QuickZipBundle.message("error.fragment-missing-files", loopFragmentName));
      }
   }

   private boolean setBaseFolderFromVirtualFile(final ZipFragmentModel selectedFragment, final VirtualFile virtualFile) {
      if (virtualFile.isValid() && virtualFile.isDirectory()) {
         final String virtualFilePath = virtualFile.getPath();
         if (selectedFragment.isPrefixForAllSelected(virtualFilePath)) {
            selectedFragment.setParentFolder(virtualFilePath);
            for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
               if (setBaseFolderFromVirtualFile(selectedFragment, loopChildVirtualFile)) {
                  break;
               }
            }
            return true;
         }
      }
      return false;
   }

   @Override
   public String getHelpId() {
      return QuickZipBundle.message("quick-zip.select-parent-folder.helpId");
   }

   @Override
   protected boolean addToolbarForLeftSection() {
      return true;
   }

   @Override
   protected JPanel createRightSectionForCenterPanel() {
      final Dimension d = myWholePanel.getPreferredSize();
      d.width = Math.max(d.width, 780);
      d.height = Math.max(d.height, 580);
      final int rightPanelWidth = (int) Math.max(400, d.width * (1 - proportion - 0.05));
      myRightPanel.setPreferredSize(new Dimension(rightPanelWidth, d.height - 100));

      return myRightPanel;
   }

   @Override
   protected void updateRightPanel(final ZipFragmentModel fragmentModel) {
      myRightPanel.removeAll();

      if (fragmentModel != null) {
         // Add the new component on the right

         final FileToZipSelectionTree selectionTreePanel = new FileToZipSelectionTree(myProject, fragmentModel);

         final JComponent toolbarComponent = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
               selectionTreePanel.getTreeActions(), true).getComponent();

         final JLabel nameLabel = new JLabel(QuickZipBundle.message("label.fragment.name"));

         final KeyListener[] currentKeyListeners = myNameTextField.getKeyListeners();
         for (final KeyListener loopListener : currentKeyListeners) {
            myNameTextField.removeKeyListener(loopListener);
         }
         final Dimension dimension = new Dimension(300, 20);
         myNameTextField.setMaximumSize(dimension);
         myNameTextField.setMinimumSize(dimension);
         myNameTextField.setText(fragmentModel.getName());
         myNameTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(final KeyEvent e) {
               fragmentModel.setName(myNameTextField.getText());
               ((DefaultTreeModel) myTree.getModel()).reload();
            }
         });
         nameLabel.setLabelFor(myNameTextField);

         final JPanel fragmentPanel = new JPanel(new BorderLayout());
         final JPanel namePanel = new JPanel(new BorderLayout());
         namePanel.add(nameLabel, BorderLayout.WEST);
         namePanel.add(myNameTextField, BorderLayout.EAST);

         final JPanel bodyPanel = new JPanel(new BorderLayout());
         bodyPanel.add(namePanel, BorderLayout.NORTH);
         bodyPanel.add(selectionTreePanel, BorderLayout.CENTER);

         fragmentPanel.add(toolbarComponent, BorderLayout.NORTH);
         fragmentPanel.add(bodyPanel, BorderLayout.CENTER);

         myRightPanel.add(fragmentPanel, BorderLayout.CENTER);
      }
      setupDialogBounds();
   }

}


