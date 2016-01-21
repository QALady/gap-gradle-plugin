package com.gap.gradle.utils

import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat;

class EncryptedStringTest {

   private EncryptedString encryptedString;

   @Test
   public void decryptEncryptedStringTest() {
       encryptedString = new EncryptedString("ENC(05WjMhY0IGDKqpYefeTzKg==)", "jasypt")
       assertThat(encryptedString.decrypt(), is("blah"))
   }

   @Test
   public void decryptUnEncryptedStringTest() {
       encryptedString = new EncryptedString("blah", "jasypt")
       assertThat(encryptedString.decrypt(), is("blah"))
   }
}