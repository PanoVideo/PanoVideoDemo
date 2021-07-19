'use strict';

import { app, protocol, BrowserWindow, screen } from 'electron';
import { createProtocol } from 'vue-cli-plugin-electron-builder/lib';
import installExtension, { VUEJS_DEVTOOLS } from 'electron-devtools-installer';
const isDevelopment = process.env.NODE_ENV !== 'production';

app.setAsDefaultProtocolClient('pano');
app.allowRendererProcessReuse = false;

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

const isMac = process.platform === 'darwin';
const lock = app.requestSingleInstanceLock();

let mainWindow;
let shareWindow;
let shareCtrlWindowReadyToShow = false;

if (!lock) {
  app.quit();
}

// ipc tool function

app.sendToMainWindow = data => {
  mainWindow.webContents.send('msgToMainWindow', data);
};

app.sendToShareWindow = data => {
  shareWindow && shareWindow.webContents.send('msgToShareWindow', data);
};

app.getDisplayByPosition = position => {
  let d;
  screen.getAllDisplays().forEach(display => {
    if (
      position.x >= display.bounds.x &&
      position.x < display.bounds.x + display.bounds.width &&
      position.y >= display.bounds.y &&
      position.y < display.bounds.y + display.bounds.height
    ) {
      d = display;
    }
  });
  return d;
};

// 设置共享窗口位置，根据位置坐标判断是在哪个屏幕上（多屏幕的情况下）
app.setShareWindow = display => {
  let scaleFactor = 1;
  if (!isMac) {
    // windows 高分屏需要处理 scaleFactor 计算出屏幕的真实大小
    scaleFactor = screen.getPrimaryDisplay().scaleFactor;
  }
  // 标注窗口
  shareWindow.setPosition(display.x, display.y);
  shareWindow.setSize(
    display.width / scaleFactor,
    display.height / scaleFactor
  );
  if (shareCtrlWindowReadyToShow) {
    // annotation 窗口
    shareWindow.show();
  }
};

app.closeShareCtrlWindow = function closeShareCtrlWindow() {
  setTimeout(() => {
    shareWindow.once('closed', () => {
      shareWindow = undefined;
    });
    shareWindow.destroy();
  }, 1000);
};

app.hideShareCtrlWindow = () => {
  shareCtrlWindowReadyToShow = false;
  shareWindow.hide();
  if (isMac) {
    mainWindow.show();
  } else {
    mainWindow.restore();
  }
  app.closeShareCtrlWindow();
};

app.showShareCtrlWindow = async () => {
  shareCtrlWindowReadyToShow = true;
  // 隐藏主窗口
  if (mainWindow.isFullScreen()) {
    mainWindow.setFullScreen(false);
    await sleep(1000);
  }
  if (isMac) {
    mainWindow.hide();
  } else {
    mainWindow.minimize();
  }
};

// Scheme must be registered before the app is ready
protocol.registerSchemesAsPrivileged([
  { scheme: 'app', privileges: { secure: true, standard: true } }
]);

async function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    webPreferences: {
      // Use pluginOptions.nodeIntegration, leave this alone
      // See nklayman.github.io/vue-cli-plugin-electron-builder/guide/security.html#node-integration for more info
      nodeIntegration: true
    }
  });

  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    await mainWindow.loadURL(process.env.WEBPACK_DEV_SERVER_URL);
  } else {
    createProtocol('app');
    mainWindow.loadURL('app://./index.html');
  }
  // if (isDevelopment) {
  //   mainWindow.webContents.openDevTools();
  // }
  mainWindow.webContents.openDevTools();
}

app.createShareCtrlWindow = function createShareCtrlWindow() {
  const windowConfig = {
    show: false,
    hasShadow: false, // 关闭窗口阴影效果
    transparent: true,
    resizable: false,
    maximizable: false,
    minimizable: false,
    frame: false,
    backgroundColor: '#00000000',
    fullscreenable: false,
    enableLargerThanScreen: true, // 可以设置大于screen size
    webPreferences: {
      nodeIntegration: true,
      webSecurity: false,
      enableRemoteModule: true
    }
  };
  // annotation window
  shareWindow = new BrowserWindow(windowConfig);
  shareWindow.setSkipTaskbar(true);

  if (isMac) {
    shareWindow.setAlwaysOnTop(true, 'screen-saver', 2);
  } else {
    shareWindow.setAlwaysOnTop(true, 'screen-saver', 2);
  }

  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    shareWindow.loadURL(
      `${process.env.WEBPACK_DEV_SERVER_URL}/share.html#/annotation`
    );
  } else {
    createProtocol('app');
    shareWindow.loadURL('app://./share.html#/annotation');
  }
};

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', async () => {
  // if (isDevelopment && !process.env.IS_TEST) {
  //   // Install Vue Devtools
  //   try {
  //     await installExtension(VUEJS_DEVTOOLS);
  //   } catch (e) {
  //     console.error('Vue Devtools failed to install:', e.toString());
  //   }
  // }
  createWindow();
});

// Exit cleanly on request from parent process in development mode.
if (isDevelopment) {
  if (process.platform === 'win32') {
    process.on('message', data => {
      if (data === 'graceful-exit') {
        app.quit();
      }
    });
  } else {
    process.on('SIGTERM', () => {
      app.quit();
    });
  }
}
