package com.advanon.pdfsignatures;

import org.jetbrains.annotations.NotNull;

public enum HashAlgorithm {
  SHA_256("SHA-256"),
  SHA_384("SHA-384"),
  SHA_512("SHA-512");

  private String algorithm;

  HashAlgorithm(@NotNull String algorithm) {
    this.algorithm = algorithm;
  }

  public String getAlgorithmName() {
    return algorithm;
  }

  /**
   * Get HashAlgorythm by it's key.
   * @param key - HashAlgorythm key (i.e. "SHA-256")
   * @return HashAlgorithm or null
   */
  public static HashAlgorithm valueByKey(@NotNull String key) {
    for (HashAlgorithm alg : values()) {
      if (alg.getAlgorithmName().equals(key)) {
        return alg;
      }
    }

    return null;
  }
}
