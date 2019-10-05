package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SignatureTest {
  private int hashableBytesRangeThreshold = 1000;
  private Path signaturePath =
      Paths.get("src", "test", "java", "resources", "signature.pkcs7");
  private Path placeholderedPdfPath =
      Paths.get("src", "test", "java", "resources", "placeholdered_pdf.pdf");
  private Path placeholderedDigestPath = Paths.get(
      "src", "test", "java", "resources", "digest.sha512"
  );

  BouncyCastleProvider provider = new BouncyCastleProvider();

  @Mock private PdfDocument pdfDocument;

  @Captor ArgumentCaptor<byte[]> contentBytesCaptor;
  @Captor ArgumentCaptor<byte[]> hashableBytesCaptor;

  @BeforeEach
  public void setup() throws IOException {
    Security.addProvider(provider);

    byte[] pdfBytes = Files.readAllBytes(placeholderedPdfPath);
    OutputStream contentBytesStream = new ByteArrayOutputStream();

    Streams.copyInputToOutputStream(
        new ByteArrayInputStream(pdfBytes),
        contentBytesStream
    );

    when(pdfDocument.getReader()).thenReturn(
        new PdfReader(new ByteArrayInputStream(pdfBytes))
    );

    when(pdfDocument.getContentBytes()).thenReturn(pdfBytes);
    doCallRealMethod().when(pdfDocument).updateHashableBytes();

    when(
        pdfDocument.signatureHexBytePosition(
          isA(PdfDictionary.class),
          anyInt()
        )
    ).thenCallRealMethod();
  }

  @Test
  public void itEmbedsSignature()
      throws IOException, SignatureException, DigestException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());
    verify(pdfDocument).setHashableBytes(hashableBytesCaptor.capture());

    byte[] contentBytes = contentBytesCaptor.getValue();
    byte[] hashableBytes = hashableBytesCaptor.getValue();

    int originalPdfLength = Files.readAllBytes(placeholderedPdfPath).length;

    assertTrue(contentBytes.length == originalPdfLength);
    assertTrue(
        hashableBytes.length <= originalPdfLength + hashableBytesRangeThreshold
    );
  }

  @Test
  public void itSetsSignatureInfoOnApply()
      throws IOException, SignatureException, GeneralSecurityException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());

    byte[] contentBytes = contentBytesCaptor.getValue();

    PdfReader reader = new PdfReader(
        new ByteArrayInputStream(contentBytes)
    );

    AcroFields fields = reader.getAcroFields();
    List<String> names = fields.getSignatureNames();

    for (String signatureName : names) {
      PdfPKCS7 pkcs7 = fields.verifySignature(
          signatureName, provider.getName()
      );

      assertEquals(
          new SimpleDateFormat("yyyy-MM-dd").format(
            pkcs7.getSignDate().getTime()
          ),
          "2018-08-13"
      );
    }
  }

  @Test
  public void itHasTheSameDigestAsPlaceholderedDocument() throws IOException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    verify(pdfDocument).setHashableBytes(hashableBytesCaptor.capture());

    byte[] hashableBytes = hashableBytesCaptor.getValue();
    byte[] placeholderedDigest = Files.readAllBytes(placeholderedDigestPath);
    byte[] signedDigest
           = new Digest(hashableBytes).calculate(HashAlgorithm.SHA_512);

    assertArrayEquals(signedDigest, placeholderedDigest);
  }
}
