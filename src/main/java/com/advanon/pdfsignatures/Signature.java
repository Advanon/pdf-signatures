package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jetbrains.annotations.NotNull;

class Signature extends PdfChange {
  private byte[] signature;

  Signature(@NotNull byte[] signature) {
    this.signature = signature;
  }

  /**
   * Find the placeholder position in a document byte sequence and put an
   * external signature on top of these bytes.
   *
   * @param pdf PDF document to sign
   * @throws SignatureException if document could not be signed
   */
  public void apply(@NotNull PdfDocument pdf) throws SignatureException {
    try {
      PdfReader reader = pdf.getReader();
      OutputStream outputStream = new ByteArrayOutputStream();

      assertWritingAllowedByCertificationlevel(reader);

      AcroFields acroFields = reader.getAcroFields();
      List<String> signatureNames = acroFields.getSignatureNames();
      byte[] pdfBytes
             = ((ByteArrayOutputStream) pdf.getContentBytes()).toByteArray();

      for (String name : signatureNames) {
        PdfDictionary signatureDict = acroFields.getSignatureDictionary(name);
        long signatureStartHexByte = signatureHexBytePosition(
            signatureDict, SIGNATURE_START_BYTE_POS
        );
        long signatureEndHexByte = signatureHexBytePosition(
            signatureDict, SIGNATURE_END_BYTE_POS
        );

        assertActualSignatureSizeFitsPlaceholder(
            (signatureEndHexByte - signatureStartHexByte - 2) / 2
        );

        byte[] hexSignature = hexEncode(signature);
        System.arraycopy(
            hexSignature,
            0,
            pdfBytes,
            (int) signatureStartHexByte + 1, // Ignore "<" marker
            hexSignature.length
        );
      }

      outputStream.close();

      updateHashableBytesStream(pdf);

      pdf.setContentBytes(new ByteArrayInputStream(pdfBytes));
    } catch (IOException e) {
      throw new SignatureException(e.getMessage());
    }
  }

  private byte[] hexEncode(byte[] sequence) throws IOException {
    ByteBuffer byteBuffer = new ByteBuffer();

    int sequenceLength = sequence.length;
    for (int i = 0; i < sequenceLength; i++) {
      byteBuffer.appendHex(sequence[i]);
    }

    byteBuffer.close();

    return byteBuffer.toByteArray();
  }

  /**
   * Make sure document is not closed and signature may be embedded.
   *
   * @param reader PDF reader
   * @throws SignatureException if changes are not allowed
   */
  private void assertWritingAllowedByCertificationlevel(
      @NotNull PdfReader reader
  ) throws SignatureException {
    boolean certified = reader.getCertificationLevel()
            == CertificationLevel.CERTIFIED_NO_CHANGES_ALLOWED.ordinal();

    if (certified) {
      throw new SignatureException("No changes allowed to the document");
    }
  }

  /**
   * Make sure actual signature size fits the placeholder.
   *
   * @throws SignatureException if signature is too big
   */
  private void assertActualSignatureSizeFitsPlaceholder(long asctualSize)
      throws SignatureException {
    if (this.signature.length > asctualSize) {
      throw new SignatureException(
        "Actual signature length is greater than the placeholder"
      );
    }
  }
}
