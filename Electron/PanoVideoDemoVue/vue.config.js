module.exports = {
  pages: {
    index: {
      // page 的入口
      entry: 'src/main.js',
      // 模板来源
      template: 'public/index.html',
      // 在 dist/index.html 的输出
      filename: 'index.html',
      // 在这个页面中包含的块，默认情况下会包含
      // 提取出来的通用 chunk 和 vendor chunk。
      chunks: ['chunk-vendors', 'chunk-common', 'index']
    },
    share: {
      // page 的入口
      entry: 'src/share.entry.js',
      // 模板来源
      template: 'public/share.html',
      // 在 dist/index.html 的输出
      filename: 'share.html'
    }
  },
  chainWebpack: config => {
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
            : './node_modules/@pano.video/panortc-electron-sdk/native'
      })
      .end();
  },
  pluginOptions: {
    electronBuilder: {
      nodeIntegration: true,
      builderOptions: {
        appId: 'demo.pano.video',
        directories: {
          buildResources: 'appico' // icon配置位置
        },
        dmg: {
          sign: false
        },
        mac: {
          hardenedRuntime: true,
          gatekeeperAssess: false,
          // identity: '', // 签名证书id
          entitlements: 'pkg/mac/entitlements.mac.plist',
          entitlementsInherit: 'pkg/mac/entitlements.mac.plist',
          extendInfo: {
            NSMicrophoneUsageDescription: '请允许本程序访问您的麦克风',
            NSCameraUsageDescription: '请允许本程序访问您的摄像头'
          },
          extraResources: [
            {
              from: 'node_modules/@pano.video/panortc-electron-sdk/native/',
              to: '../Resources',
              filter: ['**/*']
            }
          ]
        },
        win: {
          target: ['nsis'],
          asar: false,
          extraResources: [
            {
              from: 'node_modules/@pano.video/panortc-electron-sdk/native/',
              to: '../resources',
              filter: ['**/*']
            }
          ],
          verifyUpdateCodeSignature: false,
          signingHashAlgorithms: ['sha256', 'sha1'],
          signDlls: true,
          rfc3161TimeStampServer: 'http://timestamp.digicert.com'
          // certificateFile: '', // windows签名 pfx 文件位置
          // certificatePassword: '' // pfx password
        },
        afterSign: 'pkg/mac/notarize.js'
      }
    }
  }
};
