package com.advanon.pdfsignatures;

public class ValidationException extends RuntimeException {
  public ValidationException(String errorMessage) {
    super(errorMessage);
  }
}
