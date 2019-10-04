const { CommandsMap } = require('./constants');
const { executeCommand } = require('./command');

/**
 * Create a new pdf with signature placeholder and calculate it's digest.
 *
 * @param {object} params
 * @param {string} params.file
 * @param {string} params.out
 * @param {number} [params.estimatedsize=30000]
 * @param {number} [params.certlevel=0]
 * @param {string} [params.password]
 * @param {string} [params.algorithm='SHA-512']
 * @param {string} [params.reason]
 * @param {string} [params.location]
 * @param {string} [params.contact]
 * @param {string} [params.date]
 *
 * @returns {string} Bae64-encoded document digest
 */
const preparePdf = async ({
  file,
  out,
  estimatedsize,
  certlevel,
  password,
  algorithm,
  reason,
  location,
  contact,
  date,
}) => {
  if (!(file && out)) {
    throw new Error('\'file\' and \'out\' attributes are mandatory');
  }

  return executeCommand(CommandsMap.PrepareDocument, {
    file,
    out,
    estimatedsize,
    certlevel,
    password,
    algorithm,
    reason,
    location,
    contact,
    date,
  });
};

/**
 * Embed external signature into the document.
 *
 * @param {object} params
 * @param {string} params.file
 * @param {string} params.out
 * @param {string} params.signature - Base64-encoded external signature
 * @param {string} [params.password]
 *
 * @returns {string} Signed document path
 */
const signPdf = async ({
  file,
  out,
  signature,
  password,
}) => {
  if (!(file && out && signature)) {
    throw new Error(
      '\'file\', \'out\' and \'signature\' attributes are mandatory'
    );
  }

  return executeCommand(CommandsMap.SignDocument, {
    file,
    out,
    signature,
    password,
  });
};

/**
 * Embed LTV information into the document.
 *
 * @param {object} params
 * @param {string} params.file
 * @param {string} params.out
 * @param {Array<string>} params.crl
 * @param {Array<string>} params.ocsp
 *
 * @returns {string} Path of a new document
 */
const addLtvToPdf = async ({ file, out, crl, ocsp }) => {
  if (!(file && out && crl && ocsp)) {
    throw new Error(
      '\'file\', \'out\', \'crl\' and \'ocsp\' attributes are mandatory'
    );
  }

  if (!(crl instanceof Array && ocsp instanceof Array)) {
    throw new Error('\'crl\' and \'ocsp\' attributes must be arrays');
  }

  return executeCommand(
    CommandsMap.AddLtvInformation, { file, out, crl, ocsp }
  );
};

module.exports = {
  preparePdf,
  signPdf,
  addLtvToPdf,
};
