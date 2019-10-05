const {
  addSignaturePlaceholderToPdf,
  pdfDigest,
  signPdf,
  addLtvToPdf,
} = require('./pdf');

jest.mock('./command');

const { executeCommand } = require('./command');

describe('#addSignaturePlaceholderToPdf', () => {
  it('requires "file" to be set', async () => {
    await expect(addSignaturePlaceholderToPdf({ out: 'out.pdf' }))
      .rejects
      .toEqual(new Error('\'file\' and \'out\' attributes are mandatory'));
  });

  it('requires "out" to be set', async () => {
    await expect(addSignaturePlaceholderToPdf({ file: 'file.pdf' }))
      .rejects
      .toEqual(new Error('\'file\' and \'out\' attributes are mandatory'));
  });

  it('calls #addSignaturePlaceholderToPdf with proper arguments', async () => {
    const date = new Date().toISOString();

    await addSignaturePlaceholderToPdf({
      file: 'my-file.pdf',
      out: 'out-file.pdf',
      estimatedsize: 10000,
      certlevel: 0,
      password: '123456',
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
      reason: 'reason',
      location: 'test location',
      contact: 'Major Payne',
      date,
    });
  });
});

describe('#pdfDigest', () => {
  it('requires "file" to be set', async () => {
    await expect(pdfDigest({}))
      .rejects
      .toEqual(new Error('\'file\' attribute is mandatory'));
  });

  it('calls #pdfDigest with proper arguments', async () => {
    const date = new Date().toISOString();

    await pdfDigest({
      file: 'my-file.pdf',
      password: '123456',
      algorithm: 'SHA-512',
    });

    expect(executeCommand).toHaveBeenCalledWith('digest', {
      file: 'my-file.pdf',
      password: '123456',
      algorithm: 'SHA-512',
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
