jest.mock('child_process');
jest.mock('get-installed-path');

const { getInstalledPath } = require('get-installed-path');
const { exec } = require('child_process');

const {
  initResult,
  buildArguments,
  parseResponse,
  executeCommand,
} = require('./command');

const { ExecutablePath } = require('./constants');

describe('#initResult', () => {
  it('initializes result object', () => {
    expect(initResult()).toEqual({
      status: null,
      result: null,
      errorType: null,
      errorMessage: null,
    });
  });
});

describe('#buildArguments', () => {
  it('initializes result object', () => {
    const date = new Date().toISOString();

    const args = {
      hello: 'world',
      my: 'name',
      is: 'John Doe',
      lets: [
        'have a party',
        'rock'
      ],
      at: date,
    };

    const argumentsString = `--hello "world" --my "name" --is "John Doe" --lets "have a party" --lets "rock" --at "${date}"`;

    expect(buildArguments(args)).toEqual(argumentsString);
  });
});

describe('#parseResponse', () => {
  it('parses successfull response', () => {
    const response = "STATUS=SUCCESS\nRESULT=HELLO\n";
    const parsedResponse = {
      status: 'SUCCESS',
      result: 'HELLO',
      errorMessage: null,
      errorType: null,
    };

    expect(parseResponse(response)).toEqual(parsedResponse);
  });

  it('parses error response', () => {
    const response = "STATUS=ERROR\nERROR_TYPE=DocumentException\nERROR_MESSAGE=Error\n";
    const parsedResponse = {
      status: 'ERROR',
      result: null,
      errorMessage: 'Error',
      errorType: 'DocumentException',
    };

    expect(parseResponse(response)).toEqual(parsedResponse);
  });

  it('throws exception if paese fails', () => {
    const response = "random string";

    expect(() => {
      parseResponse(response);
    }).toThrowError('Could not parse response');
  });

  it('throws exception for unsupported keys', () => {
    const response = "UNSUPPORTED_KEY=value\n";

    expect(() => {
      parseResponse(response);
    }).toThrowError('Unsupported response key UNSUPPORTED_KEY');
  });
});

describe('#executeCommand', () => {
  it('initializes result object', async () => {
    getInstalledPath.mockImplementation(() => 'fake-self-path');

    exec.mockImplementation((cmd, callback) => {
      expect(cmd).toEqual(`java -jar fake-self-path/${ExecutablePath} command --a "b"`);
      callback(null, 'STATUS=SUCCESS');
    });

    await executeCommand('command', { a: 'b' });
  });
});
