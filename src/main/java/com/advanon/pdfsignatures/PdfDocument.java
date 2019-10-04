package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PdfDocument {
  private PdfReader reader;
  private InputStream contentBytes;
  private InputStream hashableBytes;

  private Placeholder placeholder;
  private Signature signature;
  private Validation validation;

  PdfDocument(
      @NotNull String path, @Nullable String password
  ) throws PdfDocumentException {
    try {
      this.contentBytes = new ByteArrayInputStream(
        Files.readAllBytes(Paths.get(path))
      );

      this.reader = new PdfReader(
        new FileInputStream(path),
        password != null ? password.getBytes() : null
      );
    } catch (IOException e) {
      throw new PdfDocumentException(e.getMessage());
    }
  }

  /**
   * Get document's PDF reader object.
   * @return document's PDF reader
   */
  public PdfReader getReader() {
    return this.reader;
  }

  /**
   * Replace document content bytes kept in memory.
   *
   * @param contentBytes new document content
   */
  public void setContentBytes(@NotNull InputStream contentBytes) {
    this.contentBytes = contentBytes;
  }

  /**
   * Returns document content.
   *
   * @return document content stream
   * @throws IOException if copying to the output stream fails
   */
  public OutputStream getContentBytes() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(contentBytes, outputStream);
    outputStream.close();

    return outputStream;
  }

  /**
   * Replace bytes which "participiate" in digest calculation.
   *
   * @param hashableBytes stream with new bytes sequence
   */
  public void setHashableBytes(@NotNull InputStream hashableBytes) {
    this.hashableBytes = hashableBytes;
  }

  /**
   * Returns bytes stream with bytes sequence which may "participiate"
   * in document digest calculation.
   * <p>
   *   NOTE that {@link #addSignaturePlaceholder} has to be called first.
   *   to calculate hashable bytes sequence.
   * </p>
   *
   * @return document hashable bytes stream
   * @throws IOException if copying to the output stream fails
   */
  public OutputStream getHashableBytes() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(hashableBytes, outputStream);
    outputStream.close();

    return outputStream;
  }

  public void addSignaturePlaceholder(
      @NotNull Placeholder placeholder
  ) throws SignatureException {
    this.placeholder = placeholder;
    placeholder.apply(this);
  }

  public void addSignature(
      @NotNull Signature signature
  ) throws SignatureException {
    this.signature = signature;
    signature.apply(this);
  }

  public void addValidation(
      @NotNull Validation validation
  ) throws ValidationException {
    this.validation = validation;
    validation.apply(this);
  }

  public byte[] digest(
      @NotNull HashAlgorithm algorithm
  ) throws DigestException {
    return new Digest(this.hashableBytes).calculate(algorithm);
  }
}
