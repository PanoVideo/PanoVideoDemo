# pano-video-call-vue-ts

PanoVideoCall 是基于 [pano](https://pano.video) sdk
 构建的会议软件，支持高性能实时音视频、白板、桌面共享、音频共享、远程控制等。

本项目使用 `Vue2.x + Typescript + electron` 编写，同一套代码支持 `web/mac/windows`平台。

## dev和打包命令介绍

1. 安装依赖 `yarn` 或 `npm i`

2. 开发模式
   - web端 `yarn dev:web`
   - electron `yarn dev`

3. 打包
   - web端 `yarn build:web`
   - electron
     - windows:  `yarn pkg:win`
     - macOs:  `yarn pkg:mac`

### 文件目录介绍

```
pano-video-call-vue
├─ babel.config.js
├─ build  # 打包相关配置
│  ├─ icons   # app图标
│  └─ mac
│     ├─ entitlements.mac.plist
│     └─ notarize.js  # mac公证相关配置
├─ public   # 静态资源目录
├─ src
│  ├─ assets
│  ├─ background.ts   # electron 主进程代码
│  ├─ components    # 界面组件目录
│  ├─ constants   # 常量
│  ├─ directives    # vue directives
│  ├─ entry.electron.ts   # electron 入口文件
│  ├─ entry.share.ts    # 共享桌面提示窗口入口文件
│  ├─ entry.web.ts    # web 入口文件
│  ├─ main.ts  # 界面组件入口文件
│  ├─ menu.ts # electron 菜单配置
│  ├─ pano  # pano sdk 相关初始化代码
│  │  ├─ ElectronSdkAdapter.ts   # panortc electron sdk 封装，使接口和 pano web sdk 一致 
│  │  ├─ index.ts   # panortc sdk 初始化
│  │  └─ panorts.ts   # panorts sdk 初始化
│  ├─ router  # 路由相关
│  ├─ store   # vuex 相关
│  ├─ utils   # 工具类
│  └─ views   # 界面组件目录
├─ tsconfig.json    # ts编译配置
├─ types    # 类型文件
├─ package.json
└─ vue.config.js    # vue配置
```

### 如何区分只能在 electron 中运行的代码

- 通过全局变量 `window.IS_ELECTRON` 判断是否是处于electron环境
- 在vue组件内，可以使用 `this.$IS_ELECTRON` 判断是否是electron环境。

### 修改 app 名称和icon

app 名称：修改 `src/constants/app.js`

icon: 修改 `build/icons/icon.png`

### window 打包、签名

运行 `yarn pkg:win` 打包 windows 并签名

### macOS 打包、签名以及公证

运行 `yarn pkg:mac` 打包 mac 并签名及公证

- 签名：在 vue.config.js 中 `electronConfig.pluginOptions.electronBuilder.builderOptions.mac.identity` 指定使用某个证书签名，值填证书 id
- 公证：需要配置 `build/mac/notarize.js` 文件，需配置apple开发者账号和短密码

> 注意 mac 端公证需要使用 Developer ID Application 类型证书，普通证书无法公证