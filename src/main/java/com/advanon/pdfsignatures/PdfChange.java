package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jetbrains.annotations.NotNull;

abstract class PdfChange {
  protected static int CONTENT_START_BYTE_POS = 0;
  protected static int SIGNATURE_START_BYTE_POS = 1;
  protected static int SIGNATURE_END_BYTE_POS = 2;
  protected static int CONTENT_END_BYTE_POS = 3;

  /**
   * Apply change to the document.
   *
   * @param pdf Document to apply the change to
   */
  abstract void apply(@NotNull PdfDocument pdf);

  /**
   * After changing the PDF we may need to recalculate and
   * fill in the hashable bytes stream.
   *
   * <p>
   *  This could be done by exluding signature byte (HEX!) sequences.
   * </p>
   *
   * @param pdf PDF for which we need to re-calculate hashable bytes
   * @throws IOException if stream error occurs
   */
  protected void updateHashableBytesStream(PdfDocument pdf) throws IOException {
    PdfReader reader = pdf.getReader();
    AcroFields acroFields = reader.getAcroFields();
    List<String> signatureNames = acroFields.getSignatureNames();
    OutputStream hashableBytesStream = new ByteArrayOutputStream();
    byte[] pdfBytes
           = ((ByteArrayOutputStream) pdf.getContentBytes()).toByteArray();

    for (String name : signatureNames) {
      PdfDictionary signatureDict = acroFields.getSignatureDictionary(name);

      // Take bytes sequence before signature
      long signatureStartHexByte
           = signatureHexBytePosition(signatureDict, SIGNATURE_START_BYTE_POS);
      long contentStartHexByte
           = signatureHexBytePosition(signatureDict, CONTENT_START_BYTE_POS);
      for (long i = contentStartHexByte; i < signatureStartHexByte; i++) {
        hashableBytesStream.write(pdfBytes[(int) i]);
      }

      // Take bytes sequence after signature
      long contentEndHexByte
          = signatureHexBytePosition(signatureDict, CONTENT_END_BYTE_POS);
      long signatureEndHexByte
          = signatureHexBytePosition(signatureDict, SIGNATURE_END_BYTE_POS);
      for (long i = signatureEndHexByte + 1; i <= contentEndHexByte; i++) {
        hashableBytesStream.write(pdfBytes[(int) i]);
      }
    }

    hashableBytesStream.close();

    InputStream contentStream = new ByteArrayInputStream(pdfBytes);
    pdf.setHashableBytes((ByteArrayInputStream) contentStream);
  }

  /**
   * Get position of signature relatively to the document content.
   * Signature byterange has the next format:
   * [
   *    documents content start byte,
   *    document content end byte (excl) and signature start byte (incl),
   *    signature end byte (incl) and document start byte (excl),
   *    document end byte
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
  protected long signatureHexBytePosition(
      @NotNull PdfDictionary signatureDict,
      @NotNull int position
  ) {
    long[] ranges = signatureDict.getAsArray(PdfName.BYTERANGE).asLongArray();
    return ranges[position];
  }
}
