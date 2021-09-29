# PanoVideoDemo (Electron)

欢迎使用 PanoVideoDemo (Electron)

## 安装依赖

```shell
yarn install
```

### dev 模式运行

```shell
yarn dev
```

**macOS 在 vscode 集成的 shell 里执行 `npm run dev` 命令编译出来的界面无法弹出权限申请框，应用会直接崩溃，需要用系统的terminal或者item等执行 `npm run dev` 命令**

### 关于打包

本项目已经做好 mac 和 windows 的打包配置，`yarn pkg:mac` 和 `yarn pkg:win` 分别打包 mac 和 windows app，打包配置请查看 `vue.config.json` electronBuilder 部分，打包完需要签名：

- mac：需要用 DeveloperID 证书签名，可以指定证书id，配置到 electronBuilder 配置的 identity 字段中
- windows：windows签名需要pfx证书，在 certificateFile 字段指定证书位置，certificatePassword 指定证书密码

## 关于 macOS 公证 (notarize)

`vue.config.json` 中已经添加了公证的脚本，需要在脚本中填入您的 apple 开发者账号ID和开发短密码。

## 配置 icon

icon 存放在 `apppico` 目录，`.icns` 为 macOS application icon，`.ico` 文件为 windows application icon
