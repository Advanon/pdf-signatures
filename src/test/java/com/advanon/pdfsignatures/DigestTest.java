package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class DigestTest {
  final byte[] sampleSha256Sequence = {
    (byte) -31, (byte) 46, (byte) 17, (byte) 90, (byte) -49, (byte) 69,
    (byte) 82, (byte) -78, (byte) 86, (byte) -117, (byte) 85, (byte) -23,
    (byte) 60, (byte) -67, (byte) 57, (byte) 57, (byte) 76, (byte) 78,
    (byte) -8, (byte) 28, (byte) -126, (byte) 68, (byte) 127, (byte) -81,
    (byte) -55, (byte) -105, (byte) -120, (byte) 42, (byte) 2, (byte) -46,
    (byte) 54, (byte) 119
  };

  final byte[] sampleSha384Sequence = {
    (byte) 111, (byte) 23, (byte) -30, (byte) 56, (byte) -103, (byte) -46,
    (byte) 52, (byte) 90, (byte) 21, (byte) 107, (byte) -81, (byte) 105,
    (byte) -25, (byte) -64, (byte) 43, (byte) -67, (byte) -38, (byte) 59,
    (byte) -32, (byte) 87, (byte) 54, (byte) 120, (byte) 73, (byte) -64,
    (byte) 42, (byte) -35, (byte) 106, (byte) 74, (byte) -20, (byte) -69,
    (byte) -48, (byte) 57, (byte) -90, (byte) 96, (byte) -70, (byte) -127,
    (byte) 92, (byte) -107, (byte) -14, (byte) -15, (byte) 69, (byte) -120,
    (byte) 54, (byte) 0, (byte) -73, (byte) -23, (byte) 19, (byte) 61
  };

  final byte[] sampleSha512Sequence = {
    (byte) 73, (byte) -20, (byte) 85, (byte) -67, (byte) -125, (byte) -4,
    (byte) -42, (byte) 120, (byte) 56, (byte) -29, (byte) -45, (byte) -123,
    (byte) -50, (byte) -125, (byte) 22, (byte) 105, (byte) -29, (byte) -8,
    (byte) 21, (byte) -89, (byte) -12, (byte) 75, (byte) 122, (byte) -91,
    (byte) -8, (byte) -43, (byte) 43, (byte) 93, (byte) 66, (byte) 53,
    (byte) 76, (byte) 70, (byte) -40, (byte) -100, (byte) -117, (byte) -99,
    (byte) 6, (byte) -28, (byte) 122, (byte) 121, (byte) 122, (byte) -28,
    (byte) -5, (byte) -46, (byte) 34, (byte) -111, (byte) -66, (byte) 21,
    (byte) -68, (byte) -61, (byte) 91, (byte) 7, (byte) 115, (byte) 92,
    (byte) 74, (byte) 111, (byte) -110, (byte) 53, (byte) 127, (byte) -109,
    (byte) -43, (byte) -93, (byte) 61, (byte) -101
  };

  @Test
  public void itCalculatesSha256Digest() throws IOException, DigestException {
    byte[] bytes = "ABCD".getBytes(StandardCharsets.UTF_8);

    assertArrayEquals(
        sampleSha256Sequence,
        new Digest(bytes).calculate(HashAlgorithm.SHA_256)
    );
  }

  @Test
  public void itCalculatesSha384Digest() throws IOException, DigestException {
    byte[] bytes = "ABCD".getBytes(StandardCharsets.UTF_8);

    assertArrayEquals(
        sampleSha384Sequence,
        new Digest(bytes).calculate(HashAlgorithm.SHA_384)
    );
  }

  @Test
  public void itCalculatesSha512Digest() throws IOException, DigestException {
    byte[] bytes = "ABCD".getBytes(StandardCharsets.UTF_8);

    assertArrayEquals(
        sampleSha512Sequence,
        new Digest(bytes).calculate(HashAlgorithm.SHA_512)
    );
  }
}
