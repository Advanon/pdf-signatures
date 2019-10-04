const { preparePdf, signPdf, addLtvToPdf } = require('./pdf');

jest.mock('./command');

const { executeCommand } = require('./command');

describe('#preparePdf', () => {
  it('requires "file" to be set', async () => {
    await expect(preparePdf({ out: 'out.pdf' }))
      .rejects
      .toEqual(new Error('\'file\' and \'out\' attributes are mandatory'));
  });

  it('requires "out" to be set', async () => {
    await expect(preparePdf({ file: 'file.pdf' }))
      .rejects
      .toEqual(new Error('\'file\' and \'out\' attributes are mandatory'));
  });

  it('calls #executeCommand with proper arguments', async () => {
    const date = new Date().toISOString();

    await preparePdf({
      file: 'my-file.pdf',
      out: 'out-file.pdf',
      estimatedsize: 10000,
      certlevel: 0,
      password: '123456',
      algorithm: 'SHA-512',
      reason: 'reason',
      location: 'test location',
      contact: 'Major Payne',
      date,
    });

    expect(executeCommand).toHaveBeenCalledWith('placeholder', {
      file: 'my-file.pdf',
      out: 'out-file.pdf',
      estimatedsize: 10000,
      certlevel: 0,
      password: '123456',
      algorithm: 'SHA-512',
      reason: 'reason',
      location: 'test location',
      contact: 'Major Payne',
      date,
    });
  });
});

describe('#signPdf', () => {
  it('requires "file" to be set', async () => {
    await expect(signPdf({ signature: 'signature', out: 'out.pdf' }))
      .rejects
      .toEqual(new Error('\'file\', \'out\' and \'signature\' attributes are mandatory'));
  });

  it('requires "out" to be set', async () => {
    await expect(signPdf({ file: 'file.pdf', signature: 'signature' }))
      .rejects
      .toEqual(new Error('\'file\', \'out\' and \'signature\' attributes are mandatory'));
  });

  it('requires "signature" to be set', async () => {
    await expect(signPdf({ file: 'file.pdf', out: 'out.pdf' }))
      .rejects
      .toEqual(new Error('\'file\', \'out\' and \'signature\' attributes are mandatory'));
  });

  it('calls #executeCommand with proper arguments', async () => {
    const date = new Date().toISOString();

    await signPdf({
      file: 'file.pdf',
      out: 'out.pdf',
      signature: 'signature',
      password: '123456',
    });

    expect(executeCommand).toHaveBeenCalledWith('sign', {
      file: 'file.pdf',
      out: 'out.pdf',
      signature: 'signature',
      password: '123456',
    });
  });
});

describe('#addLtvToPdf', () => {
  it('requires "file" to be set', async () => {
    await expect(addLtvToPdf({ out: 'out.pdf', ocsp: [], crl: [] }))
      .rejects
      .toEqual(new Error('\'file\', \'out\', \'crl\' and \'ocsp\' attributes are mandatory'));
  });

  it('requires "out" to be set', async () => {
    await expect(addLtvToPdf({ file: 'file.pdf', ocsp: [], crl: [] }))
      .rejects
      .toEqual(new Error('\'file\', \'out\', \'crl\' and \'ocsp\' attributes are mandatory'));
  });

  it('requires "crl" to be set', async () => {
    await expect(addLtvToPdf({ file: 'file.pdf', out: 'out.pdf', ocsp: [] }))
      .rejects
      .toEqual(new Error('\'file\', \'out\', \'crl\' and \'ocsp\' attributes are mandatory'));
  });

  it('requires "ocsp" to be set', async () => {
    await expect(addLtvToPdf({ file: 'file.pdf', out: 'out.pdf', crl: [] }))
      .rejects
      .toEqual(new Error('\'file\', \'out\', \'crl\' and \'ocsp\' attributes are mandatory'));
  });

  it('calls #executeCommand with proper arguments', async () => {
    await addLtvToPdf({
      file: 'file.pdf',
      out: 'out.pdf',
      ocsp: [],
      crl: [],
    });

    expect(executeCommand).toHaveBeenCalledWith('ltv', {
      file: 'file.pdf',
      out: 'out.pdf',
      ocsp: [],
      crl: [],
    });
  });
});
