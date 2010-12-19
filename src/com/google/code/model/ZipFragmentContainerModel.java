package com.google.code.model;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Date: 12/19/10
 * Time: 4:07 AM
 */
public interface ZipFragmentContainerModel {
   void addFragment(DefaultMutableTreeNode newNode);

   void removeFragment(ZipFragmentModel fragmentModel);

   // TODO Figure out a way to not having this method
   JTree getTreeComponent();
}
