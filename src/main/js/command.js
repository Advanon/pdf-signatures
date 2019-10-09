const { getInstalledPath } = require('get-installed-path');
const { exec } = require('child_process');

const {
  PackageName,
  ResponseKeys,
  ResponseKeysMap,
  ExecutablePath,
  StatusTypes,
} = require('./constants');

/**
 * Initialize object based on available response keys
 */
const initResult = () => ({
  ...(Object.values(ResponseKeysMap).reduce(
    (result, value) => ({ ...result, [value]: null }), {}
  )),
});

/**
 * Convert object to the arguments string.
 * Object may contain arrays, which would be converted to repeated args.
 *
 * @param {object} args
 */
const buildArguments = (args) => {
  const filteredArgPairs = Object.entries(args).filter(
    ([, value]) => value !== null && value !== undefined
  );

  return filteredArgPairs.map(([name, value]) => {
    if (value instanceof Array) {
      return value.map((subArg) => `--${name} "${subArg}"`).join(' ');
    }

    return `--${name} "${value}"`;
  }).join(' ');
};

/**
 * Parse response in the multiline KEY=value format
 *
 * @param {string} response
 * @returns {object}
 */
const parseResponse = (response) => {
  const lines = response.split('\n').filter((line) => line);

  const pairs = lines.map((line) => {
    const matches = /^([a-z_]+)=(.+)$/i.exec(line);

    if (!matches) {
      throw new Error(`Could not parse response: ${line}`);
    }

    const [key, value] = matches.slice(1);
    if (!Object.values(ResponseKeys).includes(key)) {
      throw new Error(`Unsupported response key ${key}`);
    }

    return { [ResponseKeysMap[key]]: value };
  });

  return pairs.reduce((result, pair) => ({ ...result, ...pair }), initResult());
};

/**
 * Execute command with given args
 *
 * @param {string} command
 * @param {object} args
 */
const executeCommand = async (command, args = {}) => {
  const selfPath = await getInstalledPath(PackageName, { local: true });
  const parsedArgs = buildArguments(args);

  return new Promise((resolve, reject) => {
    exec(
      `java -jar ${selfPath}/${ExecutablePath} ${command} ${parsedArgs}`,
      (error, stdout, stderr) => {
        if (error) {
          reject(error);
        } else {
          const response = parseResponse(stdout || stderr);
          if (response.status === StatusTypes.Success) {
            resolve(response.result);
          } else {
            reject(`Error ${response.errorType}: ${response.errorMessage}`);
          }
        }
      }
    );
  });
};

module.exports = {
  initResult,
  buildArguments,
  parseResponse,
  executeCommand,
};
