# PANO SDK （Android）

如果开发者采用手工集成/本地依赖的方式，可以将 SDK 包放到此目录，并在 Module 的 build.gradle - dependencies 中，添加：  
`implementation fileTree(dir: '../../SDK', include: ['*.aar'])`