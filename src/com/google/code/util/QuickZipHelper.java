package com.google.code.util;

import com.google.code.model.ZipFragmentModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

/**
 * Date: 12/19/10
 * Time: 8:29 AM
 */
public class QuickZipHelper {

   private QuickZipHelper() {
      super();
   }

   public static List<List<String>> getFilesStructureToZip(final Project project, final List<ZipFragmentModel> fragmentModels) {
      // Build the selected items from the List of fragments
      final List<List<String>> actualResult = new LinkedList<List<String>>();

      for (final ZipFragmentModel fragmentModel : fragmentModels) {
         final List<String> resultList = new LinkedList<String>();

         resultList.add(fragmentModel.getParentFolder());
         resultList.addAll(fragmentModel.getSelectedFiles());

         addIntermediateDirectoriesToSelectedItemsList(fragmentModel, resultList, project.getBaseDir());

         Collections.sort(resultList);
         actualResult.add(resultList);
      }

      return actualResult;
   }

   private static void addIntermediateDirectoriesToSelectedItemsList(final ZipFragmentModel fragmentModel,
         final List<String> resultList, final VirtualFile virtualFile) {
      if (virtualFile.isValid() && virtualFile.isDirectory()) {
         final String virtualFilePath = virtualFile.getPath();
         if (fragmentModel.isPrefixForSomeSelected(virtualFilePath)) {
            if (virtualFilePath.startsWith(fragmentModel.getParentFolder())) {
               // Only add if this directory falls between the parent and some selected file
               resultList.add(virtualFilePath);
            }
            // Look into the sub-directories in any case
            for (final VirtualFile loopChildVirtualFile : virtualFile.getChildren()) {
               addIntermediateDirectoriesToSelectedItemsList(fragmentModel, resultList, loopChildVirtualFile);
            }
         }
      }
   }
}
