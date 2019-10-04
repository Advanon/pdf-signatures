package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itextpdf.text.pdf.PdfName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class ConstantsTest {
  @Test
  public void itHasCorrectVersionNumber() throws IOException {
    Path gradleBuildfilePath = Paths.get("build.gradle");
    List<String> gradleBuildLines = Files.readAllLines(gradleBuildfilePath);
    Pattern versionPattern
            = Pattern.compile("version\\s*=\\s*'(\\d+\\.\\d+\\.\\d+)'");

    String gradleVersion = gradleBuildLines.stream().reduce(
        null, (acc, line) -> {
          Matcher m = versionPattern.matcher(line);
          if (m.matches()) {
              return m.group(1);
          } else {
              return acc;
          }
        }
    );

    assertEquals(gradleVersion, Constants.VERSION);
  }

  @Test
  public void itHasCorrectAlgorithms() {
    assertEquals(HashAlgorithm.SHA_256.ordinal(), 0);
    assertEquals(HashAlgorithm.SHA_384.ordinal(), 1);
    assertEquals(HashAlgorithm.SHA_512.ordinal(), 2);
  }

  @Test
  public void itDefinesCorrectFilter() {
    assertEquals(Constants.SIGNATURE_FILTER, PdfName.ADOBE_PPKLITE);
    assertEquals(Constants.SIGNATURE_SUBFILTER, PdfName.ADBE_PKCS7_DETACHED);
  }
}
