package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationTest {
  private Path crlPath =
      Paths.get("src", "test", "java", "resources", "crl.x509");
  private Path ocspPath =
      Paths.get("src", "test", "java", "resources", "ocsp.x509");
  private Path signedPdfPath =
      Paths.get("src", "test", "java", "resources", "signed_pdf.pdf");

  @Mock private PdfDocument pdfDocument;
  @Captor ArgumentCaptor<byte[]> contentBytesCaptor;

  @BeforeEach
  public void setup() throws IOException {
    byte[] pdfBytes = Files.readAllBytes(signedPdfPath);
    OutputStream contentBytesStream = new ByteArrayOutputStream();

    Streams.copyInputToOutputStream(
        new ByteArrayInputStream(pdfBytes),
        contentBytesStream
    );

    contentBytesStream.close();

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
  public void itEmbedsLtvInformation() throws IOException {
    byte[] crlBytes = Files.readAllBytes(crlPath);
    byte[] ocspBytes = Files.readAllBytes(ocspPath);

    Validation validation = new Validation(
        Arrays.asList(ocspBytes),
        Arrays.asList(crlBytes)
    );

    validation.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());

    byte[] contentBytes = contentBytesCaptor.getValue();

    int originalPdfLength = Files.readAllBytes(signedPdfPath).length;

    assertTrue(
        contentBytes.length
          > originalPdfLength + crlBytes.length + ocspBytes.length
    );
  }
}
