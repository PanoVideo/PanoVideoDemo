const { notarize } = require('electron-notarize');

exports.default = async function notarizing(context) {
  const { electronPlatformName, appOutDir } = context;
  if (electronPlatformName !== 'darwin') {
    return;
  }
  const appName = context.packager.appInfo.productFilename;
  console.log('notarizing app, please wait...');
  // await notarize({
  //   appBundleId: 'video.pano.PanoCallDemo',
  //   appPath: `${appOutDir}/${appName}.app`,
  //   appleId: '', // appid
  //   appleIdPassword: '' // appid 短密码
  // });
};
