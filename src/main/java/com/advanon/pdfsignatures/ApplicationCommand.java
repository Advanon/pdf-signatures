package com.advanon.pdfsignatures;

import com.itextpdf.text.pdf.codec.Base64;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

class ApplicationCommand {
  private String command;
  private ApplicationArguments arguments;

  ApplicationCommand(
      @NotNull String command,
      @NotNull ApplicationArguments arguments
  ) {
    this.command = command;
    this.arguments = arguments;
  }

  public void execute() {
    try {
      String result = executeCommand();
      System.out.println(formatResult(result));
    } catch (Exception e) {
      System.err.println(formatError(e));
    }
  }

  private String executeCommand()
      throws PdfDocumentException, SignatureException, IOException,
             DigestException, ValidationException {
    switch (command) {
      case "help":
        return executeHelp();
      case "version":
      case "--version":
      case "-v":
        return executeVersion();
      case "placeholder":
        return executePlaceholder();
      case "digest":
        return executeDigest();
      case "sign":
        return executeSign();
      case "ltv":
        return executeLtv();
      default:
        return executeHelp();
    }
  }

  // CHECKSTYLE:OFF
  private String executeHelp() {
    return "\n" +
      "Advanon PKCS7 document signer\n" +
      "\n" +
      "Usage:\n" +
      "  help                                        Show this help\n" +
      "  version                                     Display current version number\n" +
      "  --version                                   Display current version number\n" +
      "  -v                                          Display current version number\n" +
      "  placeholder                                 Add a signature placeholder\n" +
      "    --file <path>                             Path to the document\n" +
      "    --out <path>                              Path where to save a new document\n" +
      "    [--estimatedsize <int>]                   Estimated signature size, default is 30000 bytes\n" +
      "    [--certlevel <int>]                       Desired certification level, default is 0\n" +
      "      * 0                                     Not certified\n" +
      "      * 1                                     Certified, no changes allowed\n" +
      "      * 2                                     Certified, form filling\n" +
      "      * 3                                     Certified, form filling and annotations\n" +
      "    [--password <string>]                     Document password\n" +
      "    [--reason <reason>]                       Signing reason\n" +
      "    [--location <location>]                   Signing location\n" +
      "    [--contact <contact>]                     Signing contact\n" +
      "    [--date <contact>]                        Date of signing in ISO 8601 format\n" +
      "  digest                                      Calculate document digest excluding signatures\n" +
      "    --file <path>                             Path to the document\n" +
      "    [--password <string>]                     Document password\n" +
      "    [--algorithm <SHA-256|SHA-384|SHA-512>]   Encryption algorithm, default is SHA-512\n" +
      "  sign                                        Sign the document with external signature\n" +
      "    --file <path>                             Path to the document\n" +
      "    --out <path>                              Path where to save a new document\n" +
      "    --signature <base64 string>               Base64-encoded signature\n" +
      "    [--password <string>]                     Document password\n" +
      "  ltv                                         Add LTV information to the document\n" +
      "    --file <path>                             Path to the document\n" +
      "    --out <path>                              Path where to save a new document\n" +
      "    --crl <base64 string>...                  Base64-encoded CRL (each single CRL should be prepended with -crl)\n" +
      "    --ocsp <base64 string>...                 Base64-encoded OCSP (each single OCSP should be prepended with -ocsp)\n" +
      "    [--password <string>]                     Document password\n" +
      "\n" +
      "Example\n" +
      "  placeholder --file file.pdf --out placeholdered.pdf                                                                   Add signature placeholder\n" +
      "  digest --file placeholdered.pdf --algorithm sha512                                                                    Calculate document digest\n" +
      "  sign --file placeholdered.pdf --out signed.pdf --signature abb4rjfh=                                                  Sign the document with external signature\n" +
      "  ltv --file signed.pdf --out signedltv.pdf --crl abb4rjfh= --crl fgsllldj5kg= --oscp abb4rjfh= --ocsp fgsllldj5kg=     Insert LTV information into signed document"
    ;
  }
  // CHECKSTYLE:ON

  private String executeVersion() {
    return "Advanon PKCS7 document signer v" + Constants.VERSION;
  }

  private String executePlaceholder()
      throws PdfDocumentException, SignatureException,
             DigestException, IOException {
    SignatureMetadata metadata = new SignatureMetadata(
        arguments.getReason(),
        arguments.getLocation(),
        arguments.getContact(),
        arguments.getDate()
    );

    Placeholder placeholder = new Placeholder(
        metadata,
        arguments.getEstimatedSize(),
        arguments.getCertificationLevel()
    );

    PdfDocument pdf = new PdfDocument(
        arguments.getFile(),
        arguments.getPassword()
    );

    pdf.addSignaturePlaceholder(placeholder);

    OutputStream fileOutputStream = new FileOutputStream(arguments.getOut());
    fileOutputStream.write(pdf.getContentBytes());
    fileOutputStream.close();

    return arguments.getOut();
  }

  private String executeDigest()
      throws PdfDocumentException, SignatureException,
             DigestException, IOException {
    PdfDocument pdf = new PdfDocument(
        arguments.getFile(),
        arguments.getPassword()
    );

    return Base64.encodeBytes(
        pdf.digest(arguments.getHashAlgorithm()), Base64.DONT_BREAK_LINES
    );
  }

  private String executeSign()
      throws PdfDocumentException, SignatureException, IOException {
    Signature signature = new Signature(arguments.getSignature());
    PdfDocument pdf = new PdfDocument(
        arguments.getFile(),
        arguments.getPassword()
    );

    pdf.addSignature(signature);

    OutputStream fileOutputStream = new FileOutputStream(arguments.getOut());
    fileOutputStream.write(pdf.getContentBytes());
    fileOutputStream.close();

    return arguments.getOut();
  }

  private String executeLtv()
      throws PdfDocumentException, ValidationException, IOException {
    PdfDocument pdf = new PdfDocument(
        arguments.getFile(),
        arguments.getPassword()
    );

    Validation ltv = new Validation(
        arguments.getOcsps(),
        arguments.getCrls()
    );

    pdf.addValidation(ltv);

    OutputStream fileOutputStream = new FileOutputStream(arguments.getOut());
    fileOutputStream.write(pdf.getContentBytes());
    fileOutputStream.close();

    return arguments.getOut();
  }

  private String formatResult(String result) {
    if (clearFormatting()) {
      return result;
    }

    return "STATUS=SUCCESS\n"
           + "RESULT=" + result;
  }

  private String formatError(Exception error) {
    return "STATUS=ERROR\n"
           + "ERROR_TYPE=" + error.getClass() + "\n"
           + "ERROR_MESSAGE=" + error.getMessage();
  }

  private boolean clearFormatting() {
    return command.equals("help")
          || command.equals("version")
          || command.equals("--version")
          || command.equals("-v");
  }
}
