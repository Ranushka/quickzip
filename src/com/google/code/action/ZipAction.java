package com.google.code.action;

import com.google.code.model.ZipFragmentModel;
import com.google.code.util.QuickZipBundle;
import com.google.code.wizard.QuickZipWizard;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Date: 12/17/10
 * Time: 11:24 PM
 */
public class ZipAction extends AnAction {

   @Override
   public void update(AnActionEvent event) {
      super.update(event);

      final DataContext dataContext = event.getDataContext();
      final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
      final VirtualFile[] files = getSelectedFiles(dataContext);

      // QuickZip action should be active only when there are some selected files/fodlers
      final boolean visible = project != null && files.length > 0;

      event.getPresentation().setVisible(visible);
      event.getPresentation().setEnabled(visible);
   }

   @Override
   public void actionPerformed(final AnActionEvent event) {

      final DataContext dataContext = event.getDataContext();

      final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

      final VirtualFile[] files = getSelectedFiles(dataContext);

      final String suggestedFileName = getSuggestedFileName(files);

      final List<ZipFragmentModel> currentModels = new LinkedList<ZipFragmentModel>();
      final ZipFragmentModel initialFragment = new ZipFragmentModel(suggestedFileName, convertToExpandedCollection(files), project.getBaseDir().getPath());
      currentModels.add(initialFragment);

      if (true) {
         final QuickZipWizard wizard = new QuickZipWizard(QuickZipBundle.message("quick-zip.title"), project, currentModels);
         wizard.show();
         return;
      }
   }

   private String getSuggestedFileName(final VirtualFile[] virtualFiles) {
      if (virtualFiles.length > 0) {
         return PathUtil.getFileName(virtualFiles[0].getName());
      }
      return "quickZip";
   }

   private List<String> convertToExpandedCollection(final VirtualFile[] selectedFiles) {
      final List<String> resultList = new LinkedList<String>();
      for (int i = 0; i < selectedFiles.length; i++) {
         addFilesToList(resultList, selectedFiles[i]);
      }
      return resultList;
   }

   private void addFilesToList(final List<String> resultList, final VirtualFile virtualFile) {
      if (virtualFile.isValid()) {
         final String virtualFilePath = virtualFile.getPath();
         resultList.add(virtualFilePath);
         for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
            addFilesToList(resultList, loopChildVirtualFile);
         }
      }
   }

   private VirtualFile[] getSelectedFiles(final DataContext dataContext) {
      final VirtualFile[] resultFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
      return resultFiles != null ? resultFiles : new VirtualFile[0];
   }
}
