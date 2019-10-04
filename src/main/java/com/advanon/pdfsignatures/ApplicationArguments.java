package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.codec.Base64;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.DatatypeConverter;

import org.jetbrains.annotations.NotNull;

class ApplicationArguments {
  private Stack<String> arguments = new Stack<String>();

  private String file;
  private String out;
  private String password;
  private Integer estimatedSize;
  private CertificationLevel certificationLevel;
  private String reason;
  private String location;
  private String contact;
  private Calendar date;
  private HashAlgorithm hashAlgorithm;
  private byte[] signature;
  private List<byte[]> crls = new ArrayList<>();
  private List<byte[]> ocsps = new ArrayList<>();

  ApplicationArguments(@NotNull List<String> arguments) {
    for (String argument : arguments) {
      this.arguments.add(argument);
    }
  }

  public ApplicationArguments parse() {
    while (arguments.size() > 0) {
      String value = arguments.pop();
      String name = arguments.pop();

      fillArguments(name, value);
    }

    return this;
  }

  public String getFile() {
    return this.file;
  }

  public String getOut() {
    return this.out;
  }

  public String getPassword() {
    return this.password;
  }

  public Integer getEstimatedSize() {
    return this.estimatedSize;
  }

  public CertificationLevel getCertificationLevel() {
    return this.certificationLevel;
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

  public HashAlgorithm getHashAlgorithm() {
    return this.hashAlgorithm;
  }

  public byte[] getSignature() {
    return this.signature;
  }

  public List<byte[]> getCrls() {
    return this.crls;
  }

  public List<byte[]> getOcsps() {
    return this.ocsps;
  }

  private void fillArguments(@NotNull String name, @NotNull String value) {
    switch (name) {
      case "--file":
        file = value;
        break;
      case "--buffer":
        file = value;
        break;
      case "--out":
        out = value;
        break;
      case "--password":
        password = value;
        break;
      case "--estimatedsize":
        estimatedSize = Integer.parseInt(value);
        break;
      case "--certlevel":
        certificationLevel =
          CertificationLevel.values()[Integer.parseInt(value)];
        break;
      case "--algorithm":
        hashAlgorithm = HashAlgorithm.valueByKey(value);
        break;
      case "--reason":
        reason = value;
        break;
      case "--location":
        location = value;
        break;
      case "--contact":
        contact = value;
        break;
      case "--date":
        date = DatatypeConverter.parseDateTime(value);
        break;
      case "--signature":
        signature = Base64.decode(value);
        break;
      case "--crl":
        crls.add(Base64.decode(value));
        break;
      case "--ocsp":
        ocsps.add(Base64.decode(value));
        break;
      default:
        break;
    }
  }
}
