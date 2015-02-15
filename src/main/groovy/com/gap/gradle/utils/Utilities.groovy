package com.gap.gradle.utils

import java.security.MessageDigest

class Utilities {

  public static def calculateSha1(File file) {
    int KB = 1024
    int MB = 1024*KB

    def messageDigest = MessageDigest.getInstance("SHA1")

    file.eachByte(MB) { byte[] buf, int bytesRead ->
      messageDigest.update(buf, 0, bytesRead);
    }

    def sha1Hex = new BigInteger(1, messageDigest.digest()).toString(16).padLeft( 40, '0' )

    sha1Hex
  }
}
