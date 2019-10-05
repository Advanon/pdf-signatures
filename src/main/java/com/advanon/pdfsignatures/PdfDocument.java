package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PdfDocument {
  public static final int CONTENT_START_BYTE_POS = 0;
  public static final int SIGNATURE_START_BYTE_POS = 1;
  public static final int SIGNATURE_END_BYTE_POS = 2;
  public static final int CONTENT_END_BYTE_POS = 3;

  private PdfReader reader;
  private byte[] contentBytes;
  private byte[] hashableBytes;

  PdfDocument(
      @NotNull String path, @Nullable String password
  ) throws PdfDocumentException {
    try {
      this.contentBytes = Files.readAllBytes(Paths.get(path));
      this.reader = new PdfReader(
        new FileInputStream(path),
        password != null ? password.getBytes() : null
      );

      updateHashableBytes();
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
  public void setContentBytes(@NotNull byte[] contentBytes) {
    this.contentBytes = contentBytes;
  }

  /**
   * Returns document content bytes.
   *
   * @return document content stream
   * @throws IOException if copying to the output stream fails
   */
  public byte[] getContentBytes() throws IOException {
    return contentBytes;
  }

  /**
   * Replace bytes which "participiate" in digest calculation.
   *
   * @param hashableBytes stream with new bytes sequence
   */
  public void setHashableBytes(@NotNull byte[] hashableBytes) {
    this.hashableBytes = hashableBytes;
  }

  /**
   * Returns bytes sequence which may "participiate"
   * in document digest calculation.
   *
   * @return document hashable bytes stream
   * @throws IOException if copying to the output stream fails
   */
  public byte[] getHashableBytes() throws IOException {
    return hashableBytes;
  }

  public void addSignaturePlaceholder(
      @NotNull PdfChange placeholder
  ) throws SignatureException {
    placeholder.apply(this);
  }

  public void addSignature(
      @NotNull PdfChange signature
  ) throws SignatureException {
    signature.apply(this);
  }

  public void addValidation(
      @NotNull PdfChange validation
  ) throws ValidationException {
    validation.apply(this);
  }

  public byte[] digest(
      @NotNull HashAlgorithm algorithm
  ) throws DigestException {
    return new Digest(hashableBytes).calculate(algorithm);
  }

  /**
   * After changing the PDF we may need to recalculate and
   * fill in the hashable bytes stream.
   *
   * <p>
   *  This could be done by exluding signature byte (HEX!) sequences.
   * </p>
   *
   * @throws IOException if stream error occurs
   */
  public void updateHashableBytes() throws IOException {
    PdfReader reader = getReader();
    AcroFields acroFields = reader.getAcroFields();
    List<String> signatureNames = acroFields.getSignatureNames();
    OutputStream hashableBytesStream = new ByteArrayOutputStream();

    for (String name : signatureNames) {
      PdfDictionary signatureDict = acroFields.getSignatureDictionary(name);

      // Take bytes sequence before signature
      long signatureStartHexByte
           = signatureHexBytePosition(signatureDict, SIGNATURE_START_BYTE_POS);
      long contentStartHexByte
           = signatureHexBytePosition(signatureDict, CONTENT_START_BYTE_POS);
      for (long i = contentStartHexByte; i < signatureStartHexByte; i++) {
        hashableBytesStream.write(getContentBytes()[(int) i]);
      }

      // Take bytes sequence after signature
      long contentEndHexByte
          = signatureHexBytePosition(signatureDict, CONTENT_END_BYTE_POS);
      long signatureEndHexByte
          = signatureHexBytePosition(signatureDict, SIGNATURE_END_BYTE_POS);
      for (long i = signatureEndHexByte; i < contentEndHexByte; i++) {
        hashableBytesStream.write(getContentBytes()[(int) i]);
      }
    }

    hashableBytesStream.close();

    setHashableBytes(
        ((ByteArrayOutputStream) hashableBytesStream).toByteArray()
    );
  }

  /**
   * Get position of signature relatively to the document content.
   * Signature byterange has the next format:
   * [
   *    documents content start byte,
   *    document content length before signature,
   *    document content length with signature,
   *    document content length after signature
   * ]
   * Be aware that these are positions of HEX-bytes
   * (each digit is represented bytwo bytes),
   * content may be also surrounded with 0x60 and 0x62 bytes
   * (which are `<` and `>` markers)
   *
   * @param signatureDict Signature dictionary which signature info
   * @param position Position in the byterange to pick
   * @return Desired position in bytes
   */
  public long signatureHexBytePosition(
      @NotNull PdfDictionary signatureDict,
      @NotNull int position
  ) {
    long[] ranges = signatureDict.getAsArray(PdfName.BYTERANGE).asLongArray();

    switch (position) {
      case CONTENT_END_BYTE_POS:
        return ranges[SIGNATURE_END_BYTE_POS] + ranges[CONTENT_END_BYTE_POS];
      default:
        return ranges[position];
    }
  }
}
