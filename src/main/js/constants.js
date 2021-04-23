const PackageName = 'pdf-signatures';
const PackageVersion = '0.1.0';
const ExecutablePath = `libs/${PackageName}-${PackageVersion}.jar`;

/**
 * @enum {string}
 */
const StatusTypes = {
  Success: 'SUCCESS',
  Error: 'ERROR',
};

/**
 * @enum {string}
 */
const ResponseKeys = {
  Status: 'STATUS',
  Result: 'RESULT',
  ErrorType: 'ERROR_TYPE',
  ErrorMessage: 'ERROR_MESSAGE',
};

/**
 * @enum {string}
 */
const ResponseKeysMap = {
  [ResponseKeys.Status]: 'status',
  [ResponseKeys.Result]: 'result',
  [ResponseKeys.ErrorType]: 'errorType',
  [ResponseKeys.ErrorMessage]: 'errorMessage',
};

/**
 * @enum {string}
 */
const CommandsMap = {
  CalculateDigest: 'digest',
  AddPlaceholder: 'placeholder',
  SignDocument: 'sign',
  AddLtvInformation: 'ltv',
};

/**
 * @enum {number}
 */
const CertificationLevels = {
  NotCertified: 0,
  CertifiedNoChangesAllowed: 1,
  CertifiedFormFilling: 2,
  CertifiedFormFillingAndAnnotations: 3,
};

/**
 * @enum {string}
 */
const HashAlgorithms = {
  Sha256: 'SHA-256',
  Sha384: 'SHA-384',
  Sha512: 'SHA-512',
};

module.exports = {
  PackageName,
  PackageVersion,
  ExecutablePath,
  StatusTypes,
  ResponseKeys,
  ResponseKeysMap,
  CommandsMap,
  CertificationLevels,
  HashAlgorithms,
};
