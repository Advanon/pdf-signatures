package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.PdfName;

class Constants {
  static final String VERSION = "0.0.2";
  static final PdfName SIGNATURE_FILTER = PdfName.ADOBE_PPKLITE;
  static final PdfName SIGNATURE_SUBFILTER = PdfName.ADBE_PKCS7_DETACHED;
  static final int DEFAULT_ESTIMATED_SIGNATURE_SIZE = 30000;
  static final HashAlgorithm DEFAULT_HASH_ALGORITHM = HashAlgorithm.SHA_512;
}
