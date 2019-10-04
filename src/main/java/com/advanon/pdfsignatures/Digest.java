package com.advanon.pdfsignatures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Digest {
  private InputStream inputStream;

  Digest(@NotNull InputStream inputStream) {
    this.inputStream = inputStream;
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

      OutputStream outputStream = new ByteArrayOutputStream();
      Streams.copyInputToOutputStream(this.inputStream, outputStream);

      outputStream.close();

      return digest.digest(
        ((ByteArrayOutputStream) outputStream).toByteArray()
      );
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new DigestException(e.getMessage());
    }
  }
}
