/**
 * Enum for certification levels.
 * @enum {number}
 */
export enum CertificationLevels {
  NotCertified = 0,
  CertifiedNoChangesAllowed = 1,
  CertifiedFormFilling = 2,
  CertifiedFormFillingAndAnnotations = 3,
}

/**
 * Enum for hash algorithms.
 * @enum {string}
 */
export enum HashAlgorithms {
  Sha256 = 'SHA-256',
  Sha384 = 'SHA-384',
  Sha512 = 'SHA-512',
}

/**
 * Interface for parameters of addSignaturePlaceholderToPdf function.
 * @interface
 */
export interface AddSignaturePlaceholderToPdfParams {
  /**
   * Path to the input PDF file.
   * @type {string}
   */
  file: string;

  /**
   * Path to the output PDF file.
   * @type {string}
   */
  out: string;

  /**
   * Estimated size of the signature.
   * @type {number}
   * @default 30000
   */
  estimatedsize?: number;

  /**
   * Certification level of the signature.
   * @type {CertificationLevels}
   * @default CertificationLevels.NotCertified
   */
  certlevel?: CertificationLevels;

  /**
   * Password for the PDF file.
   * @type {string}
   */
  password?: string;

  /**
   * Reason for signing the PDF.
   * @type {string}
   */
  reason?: string;

  /**
   * Location where the PDF is signed.
   * @type {string}
   */
  location?: string;

  /**
   * Contact information of the signer.
   * @type {string}
   */
  contact?: string;

  /**
   * Date of signing the PDF in ISO-8601 format.
   * @type {string}
   */
  date?: string;
}

/**
 * Interface for parameters of pdfDigest function.
 * @interface
 */
export interface PdfDigestParams {
  /**
   * Path to the input PDF file.
   * @type {string}
   */
  file: string;

  /**
   * Password for the PDF file.
   * @type {string}
   */
  password?: string;

  /**
   * Hash algorithm to use for calculating the digest.
   * @type {HashAlgorithms}
   * @default HashAlgorithms.Sha512
   */
  algorithm?: HashAlgorithms;
}

/**
 * Interface for parameters of signPdf function.
 * @interface
 */
export interface SignPdfParams {
  /**
   * Path to the input PDF file.
   * @type {string}
   */
  file: string;

  /**
   * Path to the output PDF file.
   * @type {string}
   */
  out: string;

  /**
   * Base64-encoded external signature.
   * @type {string}
   */
  signature: string;

  /**
   * Password for the PDF file.
   * @type {string}
   */
  password?: string;
}

/**
 * Interface for parameters of addLtvToPdf function.
 * @interface
 */
export interface AddLtvToPdfParams {
  /**
   * Path to the input PDF file.
   * @type {string}
   */
  file: string;

  /**
   * Path to the output PDF file.
   * @type {string}
   */
  out: string;

  /**
   * Certificate revocation list (base64-encoded).
   * @type {string[]}
   */
  crl: string[];

  /**
   * Online certificate status protocol responses list (base64-encoded).
   * @type {string[]}
   */
  ocsp: string[];
}

/**
 * Interface for the return type of addSignaturePlaceholderToPdf function.
 * @interface
 */
export interface AddSignaturePlaceholderToPdfResult {
  /**
   * Path to the output PDF file.
   * @type {string}
   */
  outputPath: string;
}

/**
 * Interface for the return type of pdfDigest function.
 * @interface
 */
export interface PdfDigestResult {
  /**
   * Base64-encoded document digest.
   * @type {string}
   */
  digest: string;
}

/**
 * Interface for the return type of signPdf function.
 * @interface
 */
export interface SignPdfResult {
  /**
   * Path to the signed PDF file.
   * @type {string}
   */
  outputPath: string;
}

/**
 * Interface for the return type of addLtvToPdf function.
 * @interface
 */
export interface AddLtvToPdfResult {
  /**
   * Path to the output PDF file.
   * @type {string}
   */
  outputPath: string;
}

/**
 * Interface for different types of errors that can be thrown by the functions.
 * @interface
 */
export interface PdfSignaturesError {
  /**
   * Error message.
   * @type {string}
   */
  message: string;

  /**
   * Error type.
   * @type {string}
   */
  type: string;
}

/**
 * Adds a signature placeholder to a PDF.
 * @param {AddSignaturePlaceholderToPdfParams} params - The parameters for the function.
 * @returns {Promise<AddSignaturePlaceholderToPdfResult>} - The result of the function.
 * @example
 * const result = await addSignaturePlaceholderToPdf({
 *   file: '/path/to/file.pdf',
 *   out: '/path/to/out.pdf',
 *   estimatedsize: 30000,
 *   certlevel: CertificationLevels.NotCertified,
 *   password: '123456',
 *   reason: 'I want to sign the document',
 *   location: 'Moon',
 *   contact: 'John Doe',
 *   date: '2019-09-26T20:54:41.426Z',
 * });
 * console.log(result.outputPath);
 */
export function addSignaturePlaceholderToPdf(params: AddSignaturePlaceholderToPdfParams): Promise<AddSignaturePlaceholderToPdfResult>;

/**
 * Calculates the digest of a PDF.
 * @param {PdfDigestParams} params - The parameters for the function.
 * @returns {Promise<PdfDigestResult>} - The result of the function.
 * @example
 * const result = await pdfDigest({
 *   file: '/path/to/file.pdf',
 *   password: '123456',
 *   algorithm: HashAlgorithms.Sha512,
 * });
 * console.log(result.digest);
 */
export function pdfDigest(params: PdfDigestParams): Promise<PdfDigestResult>;

/**
 * Signs a PDF with an external signature.
 * @param {SignPdfParams} params - The parameters for the function.
 * @returns {Promise<SignPdfResult>} - The result of the function.
 * @example
 * const result = await signPdf({
 *   file: '/path/to/file.pdf',
 *   out: '/path/to/out.pdf',
 *   signature: 'base64',
 *   password: '123456',
 * });
 * console.log(result.outputPath);
 */
export function signPdf(params: SignPdfParams): Promise<SignPdfResult>;

/**
 * Adds LTV (Long Time Validation) information to a PDF.
 * @param {AddLtvToPdfParams} params - The parameters for the function.
 * @returns {Promise<AddLtvToPdfResult>} - The result of the function.
 * @example
 * const result = await addLtvToPdf({
 *   file: '/path/to/file.pdf',
 *   out: '/path/to/out.pdf',
 *   crl: [
 *     'base64',
 *     'base64',
 *     '...'
 *   ],
 *   ocsp: [
 *     'base64',
 *     'base64',
 *     '...'
 *   ],
 * });
 * console.log(result.outputPath);
 */
export function addLtvToPdf(params: AddLtvToPdfParams): Promise<AddLtvToPdfResult>;
