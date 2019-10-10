package com.advanon.pdfsignatures;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class adds a signature placeholder of the estimated size
 * to the document stream.
 * <p>
 *   This class is generally designed to help in pre-signin
 *   document digest calculation.
 * </p>
 */
final class Placeholder extends PdfChange {
  private int estimatedSize = Constants.DEFAULT_ESTIMATED_SIGNATURE_SIZE;
  private SignatureMetadata signatureMetadata;
  private CertificationLevel certificationLevel
          = CertificationLevel.NOT_CERTIFIED;

  Placeholder(
      @NotNull SignatureMetadata signatureMetadata,
      @Nullable Integer estimatedSize,
      @Nullable CertificationLevel certificationLevel
  ) {
    this.signatureMetadata = signatureMetadata;

    if (estimatedSize != null) {
      this.estimatedSize = estimatedSize;
    }

    if (certificationLevel != null) {
      this.certificationLevel = certificationLevel;
    }
  }

  public void apply(@NotNull PdfDocument pdf) {
    try {
      PdfReader reader = pdf.getReader();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PdfStamper stamper = this.buildStamper(reader, outputStream);

      stamper.setXmpMetadata(reader.getMetadata());

      PdfSignatureAppearance signatureAppearance =
          stamper.getSignatureAppearance();
      PdfSignature signature = buildSignature(signatureMetadata);
      signatureAppearance.setCryptoDictionary(signature);

      assertCertificationLevelChangeable(reader);

      if (certificationLevel != CertificationLevel.NOT_CERTIFIED) {
        signatureAppearance.setCertificationLevel(
            this.certificationLevel.ordinal()
        );
      }

      signatureAppearance.preClose(
          (HashMap<PdfName, Integer>) buildExclusionSizes()
      );

      InputStream rangeStream = signatureAppearance.getRangeStream();
      ByteArrayOutputStream rangeOutputStream = new ByteArrayOutputStream();
      Streams.copyInputToOutputStream(rangeStream, rangeOutputStream);
      pdf.setHashableBytes(rangeOutputStream.toByteArray());
      rangeOutputStream.close();

      assertWritingCertificationLevel(reader);

      signatureAppearance.close(buildSignaturePlaceholder(signature));
      outputStream.close();

      pdf.setContentBytes(outputStream.toByteArray());
    } catch (IOException | DocumentException e) {
      throw new SignatureException(e.getMessage());
    }
  }

  /**
   * Creates an object to perform PDF manipulations.
   *
   * @param reader PDF reader object
   * @param outputStream Output stream for the resulting document
   * @return PdfStamper object
   * @throws IOException if stamper creation fails
   * @throws DocumentException if stamper creation fails
   */
  private PdfStamper buildStamper(
      @NotNull PdfReader reader,
      @NotNull ByteArrayOutputStream outputStream
  ) throws IOException, DocumentException {
    AcroFields acroFields = reader.getAcroFields();
    boolean append = acroFields.getSignatureNames().size() > 0;
    return PdfStamper.createSignature(reader, outputStream, '\0', null, append);
  }

  private PdfSignature buildSignature(@NotNull SignatureMetadata metadata) {
    PdfSignature signature = new PdfSignature(
        Constants.SIGNATURE_FILTER, Constants.SIGNATURE_SUBFILTER
    );

    signature.setReason(metadata.getReason());
    signature.setLocation(metadata.getLocation());
    signature.setContact(metadata.getContact());
    signature.setDate(
        metadata.getDate() != null
          ? new PdfDate(metadata.getDate())
          : null
    );

    return signature;
  }

  /**
   * Make sure pdf is not certified yet in case of changing desired cert level.
   *
   * @param reader PDF reader object
   * @throws SignatureException if cert level could not be changed
   */
  private void assertCertificationLevelChangeable(
      @NotNull PdfReader reader
  ) throws SignatureException {
    if (this.certificationLevel == CertificationLevel.NOT_CERTIFIED) {
      return;
    }

    boolean uncertified = reader.getCertificationLevel()
            == CertificationLevel.NOT_CERTIFIED.ordinal();

    if (!uncertified) {
      throw new SignatureException(
        "Certificate level may not be changed on a signed document"
      );
    }
  }

  /**
   * Make sure document is not closed and signature may be still embedded.
   *
   * @param reader PDF reader
   * @throws SignatureException if changes are not allowed
   */
  private void assertWritingCertificationLevel(
      @NotNull PdfReader reader
  ) throws SignatureException {
    boolean certified = reader.getCertificationLevel()
            == CertificationLevel.CERTIFIED_NO_CHANGES_ALLOWED.ordinal();

    if (certified) {
      throw new SignatureException("No changes allowed to the document");
    }
  }

  /**
   * Create a new map with estimatedSize number of bytes to exclude
   * from the digest calculation.
   *
   * @return Map with sizes to exclude from the digest calculation
   */
  private Map<PdfName, Integer> buildExclusionSizes() {
    Map<PdfName, Integer> exclusionSizes = new HashMap<>();
    exclusionSizes.put(PdfName.CONTENTS, estimatedSize * 2 + 2);
    return exclusionSizes;
  }

  protected PdfDictionary buildSignaturePlaceholder(
      @NotNull PdfSignature signature
  ) {
    PdfLiteral signatureContents = (PdfLiteral) signature.get(PdfName.CONTENTS);
    byte[] filler = new byte[(signatureContents.getPosLength() - 2) / 2];

    Arrays.fill(filler, (byte) 0);

    PdfDictionary pdfDictionary = new PdfDictionary();
    pdfDictionary.put(
        PdfName.CONTENTS, new PdfString(filler).setHexWriting(true)
    );

    return pdfDictionary;
  }
}
