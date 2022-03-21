<template>
  <header v-if="$IS_ELECTRON" id="titlebar">
    <div id="drag-region">
      <div class="window-title">
        <div class="text-overflow">{{ title }}</div>
      </div>
      <div id="window-controls">
        <div class="button close-button" id="closeBtn" @click="closeApp">
          <span class="iconfont icon-close-app-win" style="color: #333" />
        </div>
        <div class="button min-button" id="minimizeBtn" @click="minApp">
          <span class="iconfont icon-min-app-win" style="color: #333" />
        </div>
        <div class="button max-button" @click="maxApp">
          <span
            id="maxResBtn"
            ref="maxResBtn"
            class="iconfont icon-enter-full-screen-win"
            style="color: #333"
          />
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { APP_NAME } from '@/constants/app';

export default {
  name: 'Frame',
  props: {
    maxBtnEnable: Boolean,
  },
  data() {
    return {
      isMac: process.platform === 'darwin',
    };
  },
  computed: {
    title() {
      return APP_NAME;
    },
  },
  methods: {
    minApp() {
      window.ipc.sendToMainProcess({ command: 'minApp' });
    },
    maxApp() {
      if (!this.maxBtnEnable) {
        return;
      }
      window.ipc.sendToMainProcess({ command: 'maximizeRestoreApp' });
    },
    changeMaxResBtn(isMaximizedApp) {
      const span = document.getElementById('maxResBtn');
      if (isMaximizedApp) {
        span.className = 'iconfont icon-leave-full-screen-win';
      } else {
        span.className = 'iconfont icon-enter-full-screen-win';
      }
    },
    closeApp() {
      window.ipc.sendToMainProcess({ command: 'closeWindow' });
    },
  },
  mounted() {
    const wincontrols = document.getElementById('window-controls');
    const isMac = process.platform === 'darwin';
    wincontrols.style.display = isMac ? 'none' : 'grid';
    document.getElementById('titlebar').style.height = isMac ? '28px' : '32px';

    this.$refs.maxResBtn.style.color = this.maxBtnEnable ? '#333' : '#ccc';

    const ipc = window.electron?.ipcRenderer;

    ipc.on('isMaximized', () => {
      this.changeMaxResBtn(true);
    });
    ipc.on('isRestored', () => {
      this.changeMaxResBtn(false);
    });

    ipc.on('fullscreenStatus', (e, fullscreen) => {
      document.getElementById('titlebar').style.display = fullscreen
        ? 'none'
        : 'block';
    });
  },
};
</script>

<style lang="less" scoped>
#titlebar {
  display: block;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 32px;
  width: 100%;
  padding: 0;
  background-color: white;
  left: 0;
  top: 0;
  z-index: 1000;
  color: #333;
}

#titlebar #drag-region {
  width: 100%;
  height: 100%;
  -webkit-app-region: drag;
  .window-title {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    color: #555;
    height: 100%;
    font-weight: bold;
    display: flex;
    align-items: center;
    font-size: 12px;
    max-width: 200px;
    user-select: none;
  }
}

#titlebar {
  color: #fff;
}

#titlebar-title {
  display: none;
  font-weight: 400;
  padding: 5px;
}

#titlebar #drag-region {
  display: grid;
  grid-template-columns: auto 138px;
}

#window-title {
  grid-column: 1;
  display: flex;
  align-items: center;
  margin-left: 8px;
  overflow: hidden;
  font-family: 'Segoe UI', sans-serif;
  font-size: 12px;
}

#window-title {
  margin-left: 12px;
}

#window-title span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
}

#window-controls {
  display: grid;
  grid-template-columns: repeat(3, 46px);
  position: absolute;
  top: 0;
  right: 0;
  height: 100%;
}

#window-controls {
  -webkit-app-region: no-drag;
}

#window-controls .button {
  grid-row: 1 / span 1;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

@media (-webkit-device-pixel-ratio: 1.5),
  (device-pixel-ratio: 1.5),
  (-webkit-device-pixel-ratio: 2),
  (device-pixel-ratio: 2),
  (-webkit-device-pixel-ratio: 3),
  (device-pixel-ratio: 3) {
  #window-controls .icon {
    width: 10px;
    height: 10px;
  }
}

#window-controls .button {
  user-select: none;
}

#window-controls .button:hover {
  background: rgba(255, 255, 255, 0.1);
}

#window-controls .button:active {
  background: rgba(255, 255, 255, 0.2);
}

.close-button:hover {
  background: #e81123 !important;
}

.close-button:active {
  background: #f1707a !important;
}
.close-button:active .icon {
  filter: invert(1);
}

.min-button {
  grid-column: 1;
}
.max-button {
  grid-column: 2;
}
.close-button {
  grid-column: 3;
}
.max-button {
  display: none;
}
/* Scroll bar */
</style>
