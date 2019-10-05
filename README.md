[![Build Status](https://travis-ci.org/Advanon/pdf-signatures.svg?branch=master)](https://travis-ci.org/Advanon/pdf-signatures)

# PDF-Signatures

Sign PDFs with PKCS7 signatures like a pro!

## Dependencies
* UNIX-like OS
* Java runtime >= 1.8
* Nodejs >= 8.10

## License

This library is distributed under the AGPL license. However, some dependencies
may be licenced under different licence types.

See https://www.gnu.org/licenses/agpl-3.0.en.html.


## Development dependencies
* Gradle https://gradle.org/install/

## Testing
* npm run lint
* npm run test
* gradle checkstyleMain
* gradle test

## Building (Before publishing an npm package)
* gradle build
* gradle copyJarToLibs
* gradle clean (optional)

## Usage (Nodejs)

### Add signature placeholder

```js
const { addSignaturePlaceholderToPdf, CertificationLevels } = require('pdf-signatures');

const base64EncodedDigest = await addSignaturePlaceholderToPdf({
  file: '/path/to/file.pdf',                   // Path to file, Required
  out: '/path/to/out.pdf',                     // Output file path, Required
  estimatedsize: 30000,                        // Estimated signature size, Optional, Default is 30000
  certlevel: CertificationLevels.NotCertified, // Certification level, Optional, Default is CertificationLevels.NotCertified
  password: '123456',                          // Document password, Optional
  reason: 'I want to sign the document',       // Signing reason, Optional, Default is undefined
  location: 'Moon',                            // Signing location, Optional, Default is undefined
  contact: 'John Doe',                         // Signing contact, Optional, Default is undefined
  date: '2019-09-26T20:54:41.426Z',            // Signing date in ISO-8601 format, Optional, Default is undefined
});
```

### Calculate document digest

```js
const { preparePdf, HashAlgorithms } = require('pdf-signatures');

const base64EncodedDigest = await pdfDigest({
  file: '/path/to/file.pdf',                   // Path to file, Required
  password: '123456',                          // Document password, Optional
  algorithm: HashAlgorithms.Sha512,            // Hash algorithm, Optional, Default is HashAlgorithms.Sha512
});
```

### Sign PDF with external signature

```js
const outputPath = await signPdf({
  file: '/path/to/file.pdf',                   // Path to file, Required
  out: '/path/to/out.pdf',                     // Output file path, Required
  signature: 'base64',                         // Base64-encoded external signature
  password: '123456',                          // Document password, Optional
});
```

### Embed LTV (Long Time Validation) information

```js
const outputPath = await addLtvToPdf({
  file: '/path/to/file.pdf', // Path to file, Required
  out: '/path/to/out.pdf',   // Output file path, Required
  crl: [                     // Certificate revocation list (bbase64-encoded), Required
    'base64',
    'base64',
    '...'
  ],
  ocsp: [                    // Online certificate status protocol responses list, (base64-encoded), Required
    'base64',
    'base64',
    '...'
  ],
});
```

## Usage (Jar)

General invokation format:

```bash
$ java -jar <path-to-jar> <arguments>
```

Where `<arguments>` is one of the following:

```bash
Advanon PKCS7 document signer

Usage:
  help                                        Show this help
  version                                     Display current version number
  --version                                   Display current version number
  -v                                          Display current version number
  placeholder                                 Add a signature placeholder
    --file <path>                             Path to the document
    --out <path>                              Path where to save a new document
    [--estimatedsize <int>]                   Estimated signature size, default is 30000 bytes
    [--certlevel <int>]                       Desired certification level, default is 0
      * 0                                     Not certified
      * 1                                     Certified, no changes allowed
      * 2                                     Certified, form filling
      * 3                                     Certified, form filling and annotations
    [--password <string>]                     Document password
    [--reason <reason>]                       Signing reason
    [--location <location>]                   Signing location
    [--contact <contact>]                     Signing contact
    [--date <contact>]                        Date of signing in ISO 8601 format
  digest                                      Calculate document digest excluding signatures
    --file <path>                             Path to the document
    [--password <string>]                     Document password
    [--algorithm <SHA-256|SHA-384|SHA-512>]   Encryption algorithm, default is SHA-512
  sign                                        Sign the document with external signature
    --file <path>                             Path to the document
    --out <path>                              Path where to save a new document
    --signature <base64 string>               Base64-encoded signature
    [--password <string>]                     Document password
  ltv                                         Add LTV information to the document
    --file <path>                             Path to the document
    --out <path>                              Path where to save a new document
    --crl <base64 string>...                  Base64-encoded CRL (each single CRL should be prepended with -crl)
    --ocsp <base64 string>...                 Base64-encoded OCSP (each single OCSP should be prepended with -ocsp)
    [--password <string>]                     Document password

Example
  placeholder --file file.pdf --out placeholdered.pdf                                                                   Add signature placeholder
  digest --file placeholdered.pdf --algorithm sha512                                                                    Calculate document digest
  sign --file placeholdered.pdf --out signed.pdf --signature abb4rjfh=                                                  Sign the document with external signature
  ltv --file signed.pdf --out signedltv.pdf --crl abb4rjfh= --crl fgsllldj5kg= --oscp abb4rjfh= --ocsp fgsllldj5kg=     Insert LTV information into signed document
```

## Notes

### Pdf objects, their encoding, position and length

You may notice in the source code a some "magic" conversions, like `(N + 2) / 2`,
or `(X - 2 / 2)`, this is because PDF objects are HEX-encoded, which means
that every two bytes of the PDF object you read is actually one HEX-digit.

For example, if you are writing byte `255` into PDF, you should bare in mind
that this will (or at least that should) be actually written as `FF` which is
now `TWO!` bytes.

Now you may wonder what does `+2` or `-2` mean. The answer is pretty
straitforward - content of PDF objects begins with `<` and ends with `>`,
and sometimes you simply don't need these markers taken into account.

### Signature byte range and document hash

Every signature has a byte range information, which contains... a byte range
of the signature and of the document content surrounding it.

For instance, consider the next byte range: `[ 0, 200, 400, 250 ]`:

```
|----------------------------|  0
| /Content <                 |
|----------------------------|  200
| signature HEX              |
|----------------------------|  400
| >                          |
|----------------------------|  650 (400 + 250)
```

When calculating hash of a signed document, signature bytes are exluded from
the calculation. From the example, these are 200 bytes `200 -> 400`.
