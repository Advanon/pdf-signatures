package com.advanon.pdfsignatures;

import org.jetbrains.annotations.NotNull;

abstract class PdfChange {
  /**
   * Apply change to the document.
   *
   * @param pdf Document to apply the change to
   */
  abstract void apply(@NotNull PdfDocument pdf);
}
