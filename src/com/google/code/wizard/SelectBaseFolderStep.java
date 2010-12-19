package com.google.code.wizard;

import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipBundle;
import com.google.code.tree.BaseFolderSelectionTree;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Date: 12/19/10
 * Time: 2:54 AM
 */
public class SelectBaseFolderStep extends AbstractQuickZipStep {

   private final JPanel myRightPanel = new JPanel(new BorderLayout());

   public SelectBaseFolderStep(final Project project, final List<ZipFragmentModel> selectedFragments, final QuickZipWizard parentWizard) {
      super(project, selectedFragments, parentWizard);
   }

   public String getHelpId() {
      return QuickZipBundle.message("quick-zip.select-files.helpId");
   }

   @Override
   protected boolean addToolbarForLeftSection() {
      return false;
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
         final JPanel fragmentPanel = new JPanel();

         final BaseFolderSelectionTree selectionTreePanel = new BaseFolderSelectionTree(myProject, fragmentModel);

         final JComponent toolbarComponent = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
               selectionTreePanel.getTreeActions(), true).getComponent();
         myRightPanel.add(toolbarComponent, BorderLayout.NORTH);

         fragmentPanel.add(selectionTreePanel, BorderLayout.CENTER);

         myRightPanel.add(fragmentPanel, BorderLayout.CENTER);

      }
      setupDialogBounds();
   }
}
