package com.advanon.pdfsignatures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

public class Streams {
  /**
   * Copies Input stream to Output stream byte by byte.
   *
   * @param inputStream Source stream
   * @param outputStream Destinatin stream
   * @throws IOException if read or write fails
   */
  public static void copyInputToOutputStream(
      @NotNull InputStream inputStream,
      @NotNull OutputStream outputStream
  ) throws IOException {
    int readByte;
    while ((readByte = inputStream.read()) != -1) {
      outputStream.write(readByte);
    }
  }
}
