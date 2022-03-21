'use strict';

import electron, {
  app,
  protocol,
  BrowserWindow,
  screen,
  ipcMain,
  globalShortcut,
} from 'electron';
import { createProtocol } from 'vue-cli-plugin-electron-builder/lib';
import installExtension, { VUEJS_DEVTOOLS } from 'electron-devtools-installer';
import { initialize, enable } from '@electron/remote/main';
import MenuBuilder from './menu';

const isDevelopment = process.env.NODE_ENV !== 'production';

initialize();

app.setAsDefaultProtocolClient('pano');

async function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const isMac = process.platform === 'darwin';
let canQuit = false;
let destoyWinTimeout: NodeJS.Timeout | undefined;

/**
 * 主窗口
 */
let mainWindow: BrowserWindow | null = null;

/**
 * 显示标注/共享控制条的窗口
 */
let shareWindow: BrowserWindow | null = null;

let shareCtrlWindowReadyToShow = false;
const minWidth = 700;
const minHeight = 580;

let shareControlBarFrame = { left: 0, top: 0, width: 0, height: 0 };
let timer: NodeJS.Timeout | undefined;
let isIn = false;

ipcMain.handle('messageMainWindow', (e, data: any) => {
  mainWindow!.webContents.send('msgToMainWindow', data);
});

ipcMain.handle(
  'messageToMainProcess',
  (e, data: { command: string; payload?: any }) => {
    console.log('messageToMainProcess:', data);
    switch (data.command) {
      case 'closeWindow':
        mainWindow && mainWindow.webContents.send('closeWindow');
        break;
      case 'closeApp':
        canQuit = true;
        mainWindow && mainWindow.close();
        app.exit();
        break;
      case 'createShareCtrlWin':
        createShareCtrlWindow();
        break;
      case 'showShareCtrlWindow':
        showShareCtrlWindow();
        break;
      case 'hideShareCtrlWindow':
        hideShareCtrlWindow();
        break;
      case 'destoryShareCtrlWin':
        destoryShareCtrlWin();
        break;
      case 'setShareWindow':
        setShareWindow(data.payload);
        break;
      case 'getDisplayByPosition':
        return getDisplayByPosition(data.payload);
      case 'minApp':
        // 最小化App
        mainWindow && mainWindow.minimize();
        break;
      case 'setMaxBtnEnable':
        if (mainWindow) {
          mainWindow.webContents.send('setMaxBtnEnable', data.payload.enable);
        }
        break;
      case 'maximizeRestoreApp':
        if (mainWindow) {
          if (mainWindow.isMaximized() || data.payload) {
            mainWindow.restore();
            const size = mainWindow.getSize();
            if (size[0] < minWidth || size[1] < minHeight) {
              mainWindow.setSize(minWidth, minHeight);
              mainWindow.center();
            }
          } else {
            mainWindow.maximize();
          }
        }
        break;
      case 'updateMainWindow':
        // 更新Window bounds
        mainWindow && mainWindow.setBounds(data.payload, false);
        if (!isMac && mainWindow && mainWindow.isFullScreen()) {
          mainWindow.setFullScreen(false);
        }
        break;
      case 'getMainWindowBounds':
        // 获取当前 Window的bounds
        return mainWindow && mainWindow.getBounds();
      case 'syncShareControlBar':
        if (data.payload.frame) {
          // 同步共享窗口的 Frame
          shareControlBarFrame = data.payload.frame;
        }
        break;
      case 'setFrameVisibility':
        if (isMac && mainWindow && data.payload) {
          mainWindow.setWindowButtonVisibility(data.payload.visible);
        }
        if (!isMac && mainWindow && data.payload) {
          mainWindow.setMaximizable(false);
          mainWindow.setMinimizable(false);
          mainWindow.center();
        }
        break;
      default:
        console.warn(
          'Command:',
          data.command,
          'NOT IMPLEMENTED!',
          'payload:',
          data.payload
        );
        break;
    }
  }
);

ipcMain.handle('messageToShareCtrlWindow', (e, data: any) => {
  shareWindow && shareWindow.webContents.send('msgToShareWindow', data);
});

const getDisplayByPosition = (position: { x: number; y: number }) => {
  let d;
  screen.getAllDisplays().forEach((display) => {
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
const setShareWindow = (display: {
  x: number;
  y: number;
  displayId: number;
  width: number;
  height: number;
}) => {
  let scaleFactor = 1;
  if (!isMac) {
    // windows 高分屏需要处理 scaleFactor 计算出屏幕的真实大小
    scaleFactor = screen.getPrimaryDisplay().scaleFactor;
  }
  if (shareCtrlWindowReadyToShow && shareWindow) {
    shareWindow.setPosition(display.x, display.y);
    shareWindow.setSize(
      display.width / scaleFactor,
      display.height / scaleFactor
    );
    shareWindow.show();
  }
};

const showShareCtrlWindow = async () => {
  shareCtrlWindowReadyToShow = true;
  if (mainWindow) {
    mainWindow.setFullScreen(false);
    if (isMac) {
      mainWindow.hide();
    } else {
      mainWindow.minimize();
    }
  }
  timer = setInterval(() => {
    if (!shareControlBarFrame.width) {
      return;
    }
    // 获取当前鼠标的位置，如果鼠标在共享窗口的状态栏上，模拟mouseEnter事件
    // 因为Electron在多窗口的情况下
    const p1 = electron.screen.getCursorScreenPoint();
    const inControlBar =
      p1.x > shareControlBarFrame.left &&
      p1.x < shareControlBarFrame.left + shareControlBarFrame.width &&
      p1.y > shareControlBarFrame.top &&
      p1.y < shareControlBarFrame.top + shareControlBarFrame.height;

    if (inControlBar && !isIn) {
      isIn = true;
      shareWindow &&
        shareWindow.webContents.send('msgToShareWindow', {
          command: 'onMouseEnter',
        });
    }
    if (!inControlBar && isIn) {
      isIn = false;
    }
  }, 500);
};

function createShareCtrlWindow() {
  destoyWinTimeout && clearTimeout(destoyWinTimeout);
  if (shareWindow && !shareWindow.isDestroyed()) {
    shareWindow.once('closed', () => {
      createShareCtrlWindow();
    });
    shareWindow.destroy();
    return;
  }
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
      enableRemoteModule: true,
      contextIsolation: !process.env.ELECTRON_NODE_INTEGRATION,
    },
  };
  // annotation window
  shareWindow = new BrowserWindow(windowConfig);
  shareWindow.setSkipTaskbar(true);
  enable(shareWindow.webContents);
  if (isMac) {
    shareWindow.setAlwaysOnTop(true, 'screen-saver', 2);
  } else {
    shareWindow.setAlwaysOnTop(true, 'screen-saver', 2);
  }
  // shareWindow.webContents.openDevTools();
  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    shareWindow.loadURL(
      `${process.env.WEBPACK_DEV_SERVER_URL}/share.html#/annotation`
    );
  } else {
    createProtocol('app');
    shareWindow.loadURL('app://./share.html#/annotation');
  }
}

function hideShareCtrlWindow() {
  shareWindow && shareWindow.hide();
  if (mainWindow) {
    if (isMac) {
      mainWindow.show();
    }
    mainWindow.setSize(minWidth, minHeight);
    mainWindow.center();
    mainWindow.maximize();
  }
}
function destoryShareCtrlWin() {
  destoyWinTimeout && clearTimeout(destoyWinTimeout);
  destoyWinTimeout = setTimeout(() => {
    try {
      shareWindow && shareWindow.destroy();
      destoyWinTimeout = undefined;
    } catch (error) {
      // DO NOTHING
    }
  }, 3000);
  timer && clearInterval(timer);
  shareControlBarFrame = { left: 0, top: 0, width: 0, height: 0 };
}

// Scheme must be registered before the app is ready
protocol.registerSchemesAsPrivileged([
  { scheme: 'app', privileges: { secure: true, standard: true } },
]);

async function createWindow() {
  // Create the browser window.
  mainWindow = new BrowserWindow({
    width: minWidth,
    height: minHeight,
    frame: false,
    webPreferences: {
      // Use pluginOptions.nodeIntegration, leave this alone
      // See nklayman.github.io/vue-cli-plugin-electron-builder/guide/security.html#node-integration for more info
      nodeIntegration: true,
      webSecurity: false,
      contextIsolation: !process.env.ELECTRON_NODE_INTEGRATION,
    },
  });
  if (isMac) {
    mainWindow.setWindowButtonVisibility(true);
  }
  enable(mainWindow.webContents);

  if (process.env.WEBPACK_DEV_SERVER_URL) {
    // Load the url of the dev server if in development mode
    await mainWindow.loadURL(process.env.WEBPACK_DEV_SERVER_URL);
  } else {
    createProtocol('app');
    // Load the index.html when not in development
    mainWindow.loadURL('app://./index.html');
  }

  (mainWindow as any).resize = async (params: any) => {
    if (mainWindow && mainWindow.isFullScreen()) {
      mainWindow.setFullScreen(false);
      await sleep(1000);
    }
    if (mainWindow) {
      mainWindow.unmaximize();
      mainWindow.setContentSize(params.width, params.height, false);
    }
  };

  mainWindow.on('maximize', () => {
    mainWindow && mainWindow.webContents.send('isMaximized');
  });

  mainWindow.on('unmaximize', () => {
    mainWindow && mainWindow.webContents.send('isRestored');
  });

  mainWindow.on('enter-full-screen', () => {
    mainWindow && mainWindow.webContents.send('fullscreenStatus', true);
  });

  mainWindow.on('leave-full-screen', () => {
    mainWindow && mainWindow.webContents.send('fullscreenStatus', false);
  });

  mainWindow.on('ready-to-show', () => {
    if (!mainWindow) {
      throw new Error('"mainWindow" is not defined');
    }
    mainWindow.show();
    mainWindow.focus();
  });

  // mainWindow.webContents.openDevTools();
  mainWindow.on('closed', () => {
    mainWindow = null;
  });
  /**
   * 拦截退出按钮, 二次确认再退出会议
   */
  mainWindow.on('close', (e) => {
    if (!canQuit && mainWindow && mainWindow.isVisible) {
      mainWindow.show();
      e.preventDefault();
      mainWindow.webContents.send('closeWindow');
    } else {
      canQuit = false;
    }
  });

  mainWindow.on('will-resize', (e, newBounds) => {
    if (newBounds.width < minWidth || newBounds.height < minHeight) {
      e.preventDefault();
    }
  });

  const menuBuilder = new MenuBuilder(mainWindow);
  menuBuilder.buildMenu();
  globalShortcut.register('CommandOrControl+Shift+0', () => {
    mainWindow && mainWindow.webContents.openDevTools();
  });
}

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
  if (isDevelopment && !process.env.IS_TEST && isMac) {
    // Install Vue Devtools
    try {
      await installExtension(VUEJS_DEVTOOLS);
    } catch (e: any) {
      console.error('Vue Devtools failed to install:', e.toString());
    }
  }
  createWindow();
});

// Exit cleanly on request from parent process in development mode.
if (isDevelopment) {
  if (process.platform === 'win32') {
    process.on('message', (data) => {
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
