import { message } from 'ant-design-vue';
import store from '../store/index';
import { UPDATE_FULLSCREEN } from '../store/mutations';

export function isFullScreen() {
  if (window.IS_ELECTRON) {
    return window.remote.getCurrentWindow().isFullScreen();
  }
  const el = document as any;
  return !!(
    el.fullscreen ||
    el.mozFullScreen ||
    el.webkitIsFullScreen ||
    el.webkitFullScreen ||
    el.msFullScreen
  );
}

export function enterFullScreen() {
  if (window.IS_ELECTRON) {
    window.remote.getCurrentWindow().setFullScreen(true);
  } else {
    const el = document.documentElement as any;
    const rfs =
      el.requestFullScreen ||
      el.webkitRequestFullScreen ||
      el.mozRequestFullScreen ||
      el.msRequestFullscreen;
    if (typeof rfs !== 'undefined' && rfs) {
      rfs.call(el);
    }
  }
}

export function exitFullscreen() {
  if (!isFullScreen()) {
    return;
  }
  if (window.IS_ELECTRON) {
    window.remote.getCurrentWindow().setFullScreen(false);
  } else {
    const el = document as any;
    const cfs =
      el.exitFullscreen ||
      el.mozCancelFullScreen ||
      el.webkitCancelFullScreen ||
      el.msExitFullscreen;
    if (typeof cfs !== 'undefined' && cfs) {
      cfs.call(el);
    }
  }
}

export function toggleFullScreen() {
  isFullScreen() ? exitFullscreen() : enterFullScreen();
}

export function addObserverForScreen(callout: (fullscreen: boolean) => any) {
  window.electron?.ipcRenderer.on(
    'fullscreenStatus',
    (_: any, fullscreen: boolean) => {
      store.commit(UPDATE_FULLSCREEN, { fullscreen });
      callout(fullscreen);
    }
  );
  if (!process.env.IS_ELECTRON) {
    const onResize = function () {
      setTimeout(function () {
        const fullscreen = isFullScreen();
        store.commit(UPDATE_FULLSCREEN, { fullscreen });
        callout(fullscreen);
      }, 100);
    };
    window.addEventListener('resize', onResize);
  }
}

export function isMobile() {
  let hasTouchScreen = false;
  if ('maxTouchPoints' in navigator) {
    hasTouchScreen = navigator.maxTouchPoints > 0;
  } else {
    const mQ = window.matchMedia('(pointer:coarse)');
    if (mQ.media === '(pointer:coarse)' && mQ.matches) {
      hasTouchScreen = true;
    } else if ('orientation' in window) {
      hasTouchScreen = true; // deprecated, but good fallback
    }
  }
  if (!hasTouchScreen) {
    return false;
  }
  // Only as a last resort, fall back to user agent sniffing
  const UA = navigator.userAgent;
  if (
    /\b(Android|iPhone|iPod|iPad|BlackBerry|IEMobile|Windows Phone)\b/i.test(UA)
  ) {
    return true;
  }
  return false;
}

function isSAFARI_15_1(ua: string) {
  return (
    ua.includes('Macintosh') &&
    ua.includes('Safari') &&
    ua.includes('Version/15.1')
  );
}
export const SAFARI_15_1 = isSAFARI_15_1(navigator.userAgent);

export const SAFARI_15_1_MESSAGE = {
  content: 'Safari 15.1 不支持打开视频',
  key: 'SAFARI_15_1',
};

export function info(
  content: any,
  duration?: any,
  onClose?: any,
  forward = false
) {
  if (duration && typeof duration === 'boolean') {
    forward = duration;
  }
  if (forward && window.IS_ELECTRON) {
    if (typeof content === 'object') {
      content = content.content;
    }
    window.ipc.sendToShareCtrlWindow({
      command: 'showIndication',
      payload: { text: content, duration: duration },
    });
    return;
  }
  return message.info(content, duration, onClose);
}

export const IMessage = {
  info,
};
