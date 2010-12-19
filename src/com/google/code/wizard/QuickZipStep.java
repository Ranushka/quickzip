package com.google.code.wizard;

import com.intellij.ide.wizard.Step;

/**
 * Date: 12/19/10
 * Time: 2:50 AM
 */
public interface QuickZipStep extends Step {
   String getHelpId();

   void disposeUIResources();
}
