package com.google.code.util;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Date: 12/18/10
 * Time: 4:00 AM
 */
public class QuickZipBundle {
   private static Reference<ResourceBundle> ourBundle;

   @NonNls
   protected static final String PATH_TO_BUNDLE = "messages.QuickZipBundle";

   private QuickZipBundle() {
   }

   public static String message(@PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, Object... params) {
      return CommonBundle.message(getBundle(), key, params);
   }

   private static ResourceBundle getBundle() {
      ResourceBundle bundle = null;
      if (ourBundle != null) {
         bundle = ourBundle.get();
      }
      if (bundle == null) {
         bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
         ourBundle = new SoftReference<ResourceBundle>(bundle);
      }
      return bundle;
   }
}

