package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceholderTest {
  private int estimatedSignatureSize = 30000;
  private int hashableBytesRangeThreshold = 1000;
  private GregorianCalendar signatureDate =
      new GregorianCalendar(2019, Calendar.JANUARY, 1);
  private Path unsignedPdfPath = Paths.get(
      "src", "test", "java", "resources", "unsigned_pdf.pdf"
  );

  @Mock private SignatureMetadata signatureMetadata;
  @Mock private PdfDocument pdfDocument;

  @Captor ArgumentCaptor<byte[]> contentBytesCaptor;
  @Captor ArgumentCaptor<byte[]> hashableBytesCaptor;

  @BeforeEach
  public void setup() throws IOException {
    byte[] pdfBytes = Files.readAllBytes(unsignedPdfPath);
    OutputStream contentBytesStream = new ByteArrayOutputStream();

    Streams.copyInputToOutputStream(
        new ByteArrayInputStream(pdfBytes),
        contentBytesStream
    );

    when(signatureMetadata.getContact()).thenReturn("Test contact");
    when(signatureMetadata.getDate()).thenReturn(signatureDate);
    when(signatureMetadata.getLocation()).thenReturn("Test location");
    when(signatureMetadata.getReason()).thenReturn("Test reason");

    when(pdfDocument.getReader()).thenReturn(
        new PdfReader(new ByteArrayInputStream(pdfBytes))
    );
  }

  @Test
  public void itAllocatesSpaceOnApply()
      throws IOException, SignatureException, DigestException {
    Placeholder placeholder = new Placeholder(
        signatureMetadata, estimatedSignatureSize, null
    );

    placeholder.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());
    verify(pdfDocument).setHashableBytes(hashableBytesCaptor.capture());

    byte[] contentBytes = contentBytesCaptor.getValue();
    byte[] hashableBytes = hashableBytesCaptor.getValue();

    int originalPdfLength = Files.readAllBytes(unsignedPdfPath).length;

    assertTrue(
        contentBytes.length >= originalPdfLength + estimatedSignatureSize
    );

    assertTrue(
        hashableBytes.length <= originalPdfLength + hashableBytesRangeThreshold
    );
  }

  @Test
  public void itSetsSignatureInfoOnApply()
      throws IOException, SignatureException {
    Placeholder placeholder = new Placeholder(
        signatureMetadata, estimatedSignatureSize, null
    );

    placeholder.apply(pdfDocument);

    verify(pdfDocument).setContentBytes(contentBytesCaptor.capture());

    byte[] contentBytes = contentBytesCaptor.getValue();

    PdfReader reader = new PdfReader(contentBytes);
    AcroFields fields = reader.getAcroFields();
    List<String> names = fields.getSignatureNames();
    PdfDictionary signatureDictionary
                  = fields.getSignatureDictionary(names.get(0));

    assertEquals(
        signatureDictionary.get(PdfName.CONTACTINFO).toString(), "Test contact"
    );

    assertEquals(
        signatureDictionary.get(PdfName.LOCATION).toString(), "Test location"
    );

    assertEquals(
        signatureDictionary.get(PdfName.REASON).toString(), "Test reason"
    );

    String formattedSignatureDate = new SimpleDateFormat(
        "'D:'yyyyMMDDHHmmSSX").format(signatureDate.getTime()
    );

    assertTrue(
        signatureDictionary.get(PdfName.M).toString().matches(
            "^" + formattedSignatureDate.replace("+", "\\+") + ".*$"
        )
    );
  }
}
