/* eslint-disable @typescript-eslint/no-var-requires */
const isElectron = process.env.IS_ELECTRON === 'true';
const webpack = require('webpack');
const { APP_NAME } = require('./src/constants/app.js');

const electronConfig = {
  pages: {
    index: {
      entry: 'src/entry.electron.ts',
    },
    share: {
      entry: 'src/entry.share.ts',
      template: 'public/share.html',
      filename: 'share.html',
    },
  },
  css: {
    loaderOptions: {
      less: {
        lessOptions: { javascriptEnabled: true },
      },
    },
  },
  chainWebpack: (config) => {
    config.module
      .rule('node')
      .test(/\.node$/)
      .use('native-ext-loader')
      .loader('native-ext-loader')
      .options({
        emit: true,
        basePath:
          process.env.NODE_ENV === 'production' &&
          process.env.PLATFORM === 'win'
            ? ['..']
            : [],
        rewritePath:
          // eslint-disable-next-line no-nested-ternary
          process.env.NODE_ENV === 'production'
            ? process.env.PLATFORM === 'win'
              ? undefined
              : '../Resources'
            : './node_modules/@pano.video/panortc-electron-sdk/native',
      })
      .end();
  },
  configureWebpack: {
    plugins: [
      new webpack.EnvironmentPlugin({
        PANO_SERVE_ENV: process.env.PANO_SERVE_ENV || 'prod',
        POC: process.env.POC || '',
      }),
    ],
  },
  pluginOptions: {
    electronBuilder: {
      nodeIntegration: true,
      builderOptions: {
        productName: APP_NAME,
        appId: 'video.pano.PanoCall',
        directories: {
          buildResources: 'build/icons',
        },
        dmg: {
          sign: false,
        },
        mac: {
          target: { target: 'dmg', arch: ['arm64', 'x64'] },
          hardenedRuntime: true,
          asar: false,
          gatekeeperAssess: false,
          identity: 'VYQ54ZA867',
          entitlements: 'build/mac/entitlements.mac.plist',
          entitlementsInherit: 'build/mac/entitlements.mac.plist',
          extendInfo: {
            NSMicrophoneUsageDescription: '请允许本程序访问您的麦克风',
            NSCameraUsageDescription: '请允许本程序访问您的摄像头',
          },
          extraResources: [
            {
              from: 'node_modules/@pano.video/panortc-electron-sdk/native/',
              to: '../Resources',
              filter: ['**/*'],
            },
          ],
        },
        win: {
          icon: './src/assets/img/pano-logo256.png',
          artifactName: '${productName}_Setup_${version}.${ext}',
          target: ['nsis'],
          asar: false,
          extraResources: [
            {
              from: 'node_modules/@pano.video/panortc-electron-sdk/native/',
              to: '../resources',
              filter: ['**/*'],
            },
          ],
          verifyUpdateCodeSignature: false,
          signingHashAlgorithms: ['sha256', 'sha1'],
          signDlls: true,
          rfc3161TimeStampServer: 'http://timestamp.digicert.com',
          sign: 'build/win/sign.js',
        },
        afterSign: 'build/mac/notarize.js',
      },
    },
  },
};

const webConfig = {
  publicPath: './',
  pages: {
    index: {
      entry: 'src/entry.web.ts',
    },
  },
  css: {
    loaderOptions: {
      less: {
        lessOptions: { javascriptEnabled: true },
      },
    },
  },
  configureWebpack: {
    plugins: [
      new webpack.EnvironmentPlugin({
        PANO_SERVE_ENV: process.env.PANO_SERVE_ENV || 'prod',
        POC: process.env.POC || '',
      }),
    ],
  },
};

module.exports = isElectron ? electronConfig : webConfig;
