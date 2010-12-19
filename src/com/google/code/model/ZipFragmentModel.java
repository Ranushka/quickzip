package com.google.code.model;

import java.util.*;

/**
 * Date: 12/18/10
 * Time: 9:26 AM
 */
public class ZipFragmentModel {

   private String name;
   private final Set<String> selectedFiles;
   private String parentFolder;

   public ZipFragmentModel(final String pName, final Collection<String> pSelectedFiles, final String baseFolder) {
      name = pName;
      selectedFiles = new HashSet<String>(pSelectedFiles);
      parentFolder = baseFolder;
   }

   public String getName() {
      return name;
   }

   public void setName(final String pName) {
      name = pName;
   }

   public Collection<String> getSelectedFiles() {
      return Collections.unmodifiableSet(selectedFiles);
   }

   public boolean addFiles(final Collection<String> filePaths) {
      return selectedFiles.addAll(filePaths);
   }

   public boolean addFile(final String filePath) {
      return selectedFiles.add(filePath);
   }

   public boolean removeFile(final String filePath) {
      return selectedFiles.remove(filePath);
   }

   public boolean removeAllFiles(final Collection<String> filePaths) {
      return selectedFiles.removeAll(filePaths);
   }

   public boolean containsFile(final String filePath) {
      return selectedFiles.contains(filePath);
   }

   public boolean containsNoneOfTheFiles(final Collection<String> filePaths) {
      for (final String loopStr : filePaths) {
         if (containsFile(loopStr)) {
            return false;
         }
      }
      return true;
   }

   public boolean containsAllOfTheFiles(final Collection<String> filePaths) {
      for (final String loopStr : filePaths) {
         if (!containsFile(loopStr)) {
            return false;
         }
      }
      return true;
   }

   public boolean isPrefixForAllSelected(String candidatePrefix) {
      for (final String selectedFile : selectedFiles) {
         if (!selectedFile.startsWith(candidatePrefix)) {
            return false;
         }
      }
      return true;
   }

   public boolean isPrefixForSomeSelected(String candidatePrefix) {
      for (final String selectedFile : selectedFiles) {
         if (selectedFile.startsWith(candidatePrefix)) {
            return true;
         }
      }
      return false;
   }

   public String autoComputeParentFolder() {
      final List<String> localFiles = new LinkedList<String>(selectedFiles);
      Collections.sort(localFiles);
      String autocomputedFolder = parentFolder;
      for (final String selectedFile : localFiles) {
         if (isPrefixForAllSelected(selectedFile)) {
            autocomputedFolder = selectedFile;
         }
      }
      return autocomputedFolder;
   }

   public String getParentFolder() {
      return parentFolder;
   }

   public void setParentFolder(final String pParentFolder) {
      parentFolder = pParentFolder;
   }

   @Override
   public String toString() {
      return "ZipFragmentModel{" +
            "name='" + name + '\'' +
            ", selectedFiles=" + selectedFiles +
            ", parentFolder='" + parentFolder + '\'' +
            '}';
   }
}
