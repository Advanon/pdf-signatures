package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
      "src", "test", "java", "resources", "placeholdered_digest.sha512"
  );

  BouncyCastleProvider provider = new BouncyCastleProvider();

  @Mock private PdfDocument pdfDocument;

  @Captor ArgumentCaptor<ByteArrayInputStream> contentBytesCaptor;
  @Captor ArgumentCaptor<ByteArrayInputStream> hashableBytesCaptor;

  @BeforeEach
  public void setup() throws IOException {
    Security.addProvider(provider);

    byte[] pdfBytes = Files.readAllBytes(placeholderedPdfPath);
    OutputStream contentBytesStream = new ByteArrayOutputStream();

    Streams.copyInputToOutputStream(
        new ByteArrayInputStream(pdfBytes),
        contentBytesStream
    );

    InputStream readerInputStream
        = new FileInputStream(placeholderedPdfPath.toFile().getAbsolutePath());

    when(pdfDocument.getReader()).thenReturn(
        new PdfReader(new ByteArrayInputStream(pdfBytes))
    );

    when(pdfDocument.getContentBytes()).thenReturn(
        (ByteArrayOutputStream) contentBytesStream
    );

    readerInputStream.close();
  }

  @Test
  public void itEmbedsSignature()
      throws IOException, SignatureException, DigestException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());
    verify(pdfDocument).setHashableBytes(hashableBytesCaptor.capture());

    ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(
        contentBytesCaptor.getValue(), contentBytes
    );

    ByteArrayOutputStream hashableBytes = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(
        hashableBytesCaptor.getValue(), hashableBytes
    );

    int originalPdfLength = Files.readAllBytes(placeholderedPdfPath).length;

    assertTrue(
        contentBytes.toByteArray().length == originalPdfLength
    );

    assertTrue(
        hashableBytes.toByteArray().length
          <= originalPdfLength + hashableBytesRangeThreshold
    );

    contentBytes.close();
    hashableBytes.close();
  }

  @Test
  public void itSetsSignatureInfoOnApply()
      throws IOException, SignatureException, GeneralSecurityException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());

    ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(
        contentBytesCaptor.getValue(), contentBytes
    );

    PdfReader reader = new PdfReader(
        new ByteArrayInputStream(contentBytes.toByteArray())
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

    contentBytes.close();
  }

  public void itHasTheSameDigestAsPlaceholderedDocument() throws IOException {
    byte[] signatureBytes = Files.readAllBytes(signaturePath);

    Signature signature = new Signature(signatureBytes);

    signature.apply(pdfDocument);

    ByteArrayOutputStream hashableBytes = new ByteArrayOutputStream();
    Streams.copyInputToOutputStream(
        hashableBytesCaptor.getValue(), hashableBytes
    );

    byte[] placeholderedDigest = Files.readAllBytes(placeholderedDigestPath);
    byte[] signedDigest = new Digest(
      new ByteArrayInputStream(hashableBytes.toByteArray())
    ).calculate(HashAlgorithm.SHA_512);

    assertEquals(signedDigest, placeholderedDigest);
  }
}
