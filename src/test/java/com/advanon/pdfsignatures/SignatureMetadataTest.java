package com.advanon.pdfsignatures;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;


class SignatureMetadataTest {
  @Test
  public void itSetsAndGetsData() {
    GregorianCalendar calendar
                      = new GregorianCalendar(2019, Calendar.JANUARY, 1);

    SignatureMetadata signaturemetaData = new SignatureMetadata(
        "My reason",
        "My location",
        "My contact",
        calendar
    );

    assertEquals(signaturemetaData.getReason(), "My reason");
    assertEquals(signaturemetaData.getLocation(), "My location");
    assertEquals(signaturemetaData.getContact(), "My contact");
    assertEquals(signaturemetaData.getDate().toString(), calendar.toString());
  }
}
