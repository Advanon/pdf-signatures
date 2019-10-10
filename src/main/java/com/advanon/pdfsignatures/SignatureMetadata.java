package com.advanon.pdfsignatures;

import java.util.Calendar;

import org.jetbrains.annotations.Nullable;

class SignatureMetadata {
  @Nullable private String reason;
  @Nullable private String location;
  @Nullable private String contact;
  @Nullable private Calendar date;

  SignatureMetadata(
      @Nullable String reason,
      @Nullable String location,
      @Nullable String contact,
      @Nullable Calendar date
  ) {
    this.reason = reason;
    this.location = location;
    this.contact = contact;
    this.date = date;
  }

  public String getReason() {
    return this.reason;
  }

  public String getLocation() {
    return this.location;
  }

  public String getContact() {
    return this.contact;
  }

  public Calendar getDate() {
    return this.date;
  }
}
