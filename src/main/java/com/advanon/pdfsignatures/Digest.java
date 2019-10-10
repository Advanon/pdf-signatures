package com.advanon.pdfsignatures;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Digest {
  private byte[] bytes;

  Digest(@NotNull byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Calculate digest of the given hashable input stream.
   *
   * @param algorithm Hashing algorithm
   * @return inputStream hash
   * @throws DigestException if hashing fails
   */
  byte[] calculate(@Nullable HashAlgorithm algorithm) throws DigestException {
    try {
      MessageDigest digest = MessageDigest.getInstance(
          algorithm == null
            ? Constants.DEFAULT_HASH_ALGORITHM.getAlgorithmName()
            : algorithm.getAlgorithmName()
      );

      return digest.digest(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new DigestException(e.getMessage());
    }
  }
}
