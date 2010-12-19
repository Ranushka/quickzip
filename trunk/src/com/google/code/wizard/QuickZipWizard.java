package com.google.code.wizard;

import com.google.code.model.ZipFragmentModel;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * Date: 12/19/10
 * Time: 2:51 AM
 */
public class QuickZipWizard extends AbstractWizard<QuickZipStep> {

   public QuickZipWizard(final String title, final Project project, final List<ZipFragmentModel> selectedFragments) {
      super(title, project);

      addStep(new SelectFilesStep(project, selectedFragments, this));
      addStep(new SelectBaseFolderStep(project, selectedFragments, this));
      addStep(new MakeZipFileStep(project, selectedFragments, this));

      init();
   }

   @Override
   protected String getHelpID() {
      final QuickZipStep step = getCurrentStepObject();
      if (step != null) {
         return step.getHelpId();
      }
      return null;
   }

   @Override
   protected void dispose() {
      for (QuickZipStep step : mySteps) {
         step.disposeUIResources();
      }
      super.dispose();
   }

   protected void updateDialog() {
      // TODO Doing nothing yet
   }
}
