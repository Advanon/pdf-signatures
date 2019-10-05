package com.advanon.pdfsignatures;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.LtvVerification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;

import org.jetbrains.annotations.NotNull;

/**
 * Embeds OCSP / CRL LTV information into the document
 * For OCSP @see https://tools.ietf.org/html/rfc2560
 * For CRL @see https://tools.ietf.org/html/rfc5280
 * For PAdES LTV @see https://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.01_60/ts_10277804v010101p.pdf
 */
class Validation extends PdfChange {
  private List<OCSPResp> ocsps;
  private List<X509CRL> crls = new ArrayList<>();

  Validation(
      @NotNull List<byte[]> ocsps,
      @NotNull List<byte[]> crls
  ) throws ValidationException {
    this.ocsps = ocsps.stream().map(ocsp -> {
      try {
        return new OCSPResp(ocsp);
      } catch (IOException e) {
        throw new ValidationException(e.getMessage());
      }
    }).collect(Collectors.toList());

    this.crls = crls.stream().map(crl -> {
      try {
        return (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(
          new ByteArrayInputStream(crl)
        );
      } catch (CRLException | CertificateException e) {
        throw new ValidationException(e.getMessage());
      }
    }).collect(Collectors.toList());
  }

  /**
   * Apply Long Term Validation to the document.
   *
   * @param pdf document to apply the LTV to
   * @throws ValidationException if application fails
   */
  public void apply(@NotNull PdfDocument pdf) throws ValidationException {
    try {
      PdfReader reader = pdf.getReader();

      assertCertificationLevel(reader);

      OutputStream outputStream = new ByteArrayOutputStream();
      PdfStamper stamper = new PdfStamper(reader, outputStream, '\0', true);
      LtvVerification ltvVerification = stamper.getLtvVerification();

      for (String signatureName : reader.getAcroFields().getSignatureNames()) {
        boolean result = ltvVerification.addVerification(
            signatureName,
            asnOneEncodedOcspList(),
            asnOneEncodedCrlList(),
            null
        );

        if (!result) {
          throw new ValidationException("Failed to embed LTV information");
        }
      }

      ltvVerification.merge();
      stamper.close();
      outputStream.close();

      pdf.setContentBytes(((ByteArrayOutputStream) outputStream).toByteArray());
      pdf.updateHashableBytes();
    } catch (IOException | GeneralSecurityException | DocumentException e) {
      throw new ValidationException(e.getMessage());
    }
  }

  /**
   * Make sur cahnges are allowed.
   * @param reader PdfReader
   */
  private void assertCertificationLevel(PdfReader reader) {
    if (reader.getCertificationLevel()
        == CertificationLevel.CERTIFIED_NO_CHANGES_ALLOWED.ordinal()) {
      throw new ValidationException("Changes are not allowed");
    }
  }

  /**
   * Return ASN.1 (X.509) encoded list of OCSPs.
   *
   * @return list of ASN.1-encoded OCSPs
   * @throws ValidationException if encoding fails
   */
  private List<byte[]> asnOneEncodedOcspList() throws ValidationException {
    return ocsps.stream().map(ocsp -> {
      try {
        return ((BasicOCSPResp) ocsp.getResponseObject()).getEncoded();
      } catch (OCSPException | IOException e) {
        throw new ValidationException(e.getMessage());
      }
    }).collect(Collectors.toList());
  }

  /**
   * Return ASN.1 (X.509) encoded list of CRLs.
   *
   * @return list of ASN.1-encoded CRLs
   * @throws ValidationException if encoding fails
   */
  private List<byte[]> asnOneEncodedCrlList() throws ValidationException {
    return crls.stream().map(crl -> {
      try {
        return crl.getEncoded();
      } catch (CRLException e) {
        throw new ValidationException(e.getMessage());
      }
    }).collect(Collectors.toList());
  }
}
