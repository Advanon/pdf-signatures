const fs = require('fs');

const { PackageName, PackageVersion, ExecutablePath } = require('./constants');

describe('PackageName', () => {
  it('has right package name', () => {
    const packageJson = JSON.parse(fs.readFileSync('package.json'));
    const gradleSettings = fs.readFileSync('settings.gradle');

    const regex = /rootProject\.name\s*=\s*'(.+)'/i;
    const matches = regex.exec(gradleSettings);

    expect(packageJson.name).toEqual(PackageName);
    expect(matches[1]).toEqual(PackageName);
  });
});

describe('PackageVersion', () => {
  it('has right package version', () => {
    const packageJson = JSON.parse(fs.readFileSync('package.json'));
    const gradleSettings = fs.readFileSync('build.gradle');

    const regex = /version\s*=\s*'(.+)'/i;
    const matches = regex.exec(gradleSettings);

    expect(packageJson.version).toEqual(PackageVersion);
    expect(matches[1]).toEqual(PackageVersion);
  });
});

describe('ExecutablePath', () => {
  it('has right executable path', () => {
    expect(ExecutablePath).toEqual(`libs/${PackageName}-${PackageVersion}.jar`);
    expect(fs.existsSync(ExecutablePath)).toEqual(true);
  });
});
