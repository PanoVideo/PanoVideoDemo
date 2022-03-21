<template>
  <div
    class="control-bar"
    @mousedown="onDragStart"
    ref="shareCtrlRef"
    :style="{ left: `${left}px`, top: `${top}px` }"
  >
    <div class="toolbar">
      <button class="control-btn" @click="onClickMicMute">
        <i
          v-if="audioMuted"
          class="iconfont icon-audio-off"
          style="color: red"
        />
        <i v-else class="iconfont icon-audio" />
        <span>{{ audioMuted ? '取消静音' : '静音' }}</span>
      </button>
      <button class="control-btn" @click="onClickCamMute">
        <i
          :class="['iconfont', videoMuted ? 'icon-video-off' : 'icon-video']"
          :style="videoMuted ? { color: 'red' } : {}"
        />
        <span>{{ videoMuted ? '打开视频' : '关闭视频' }}</span>
      </button>
      <button class="control-btn share-btn__active" @click="onClickScreen">
        <i class="iconfont icon-screen_share" :style="{ color: '#72D46A' }" />
        <span
          :style="{
            marginLeft: '0px',
            color: '#72D46A',
          }"
        >
          正在共享
        </span>
      </button>
      <button
        :class="{
          'control-btn': true,
          'control-btn__active': annotationEnabled,
        }"
        @click="onClickAnnotation"
      >
        <i class="iconfont icon-pencil1" />
        <span>标注</span>
      </button>
      <button class="control-btn" @click="onClickExit">
        <i class="iconfont icon-exit" />
        <span>退出</span>
      </button>
    </div>
    <div class="sharingInfo">
      <div class="sharingLeft">
        <i class="iconfont icon-reply" />
        <span>你正在共享屏幕</span>
      </div>
      <div class="sharingRight" @click="onClickScreen">
        <i class="iconfont icon-stop" />
        <span :style="{ marginLeft: '5px' }">停止共享</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      audioMuted: true,
      videoMuted: true,
      left: 600,
      top: 0,
      isDragging: false,
      dragStartPosition: undefined,
    };
  },
  props: {
    annotationEnabled: { type: Boolean },
  },
  methods: {
    handleMessage(_, data) {
      switch (data.command) {
        case 'syncSettings':
          this.audioMuted = data.payload.audioMuted;
          this.videoMuted = data.payload.videoMuted;
          break;
        case 'stopAnnotation':
          this.$emit('update:annotationEnabled', false);
          break;
        case 'setSharePosition':
          this.setSharePosition(data.payload);
          break;
        default:
      }
    },
    async setSharePosition(detail) {
      const display = await window.ipc.sendToMainProcess({
        command: 'getDisplayByPosition',
        payload: detail,
      });
      if (!display) return;
      const displayRect = display.bounds;
      this.left =
        displayRect.width / 2 - this.$refs.shareCtrlRef.clientWidth / 2;
      this.syncFrame();
    },
    sendMessageToMainWindow(data) {
      window.ipc.sendToMainWindow(data);
    },
    hideShareCtrlWindow() {
      window.ipc.sendToMainProcess({
        command: 'hideShareCtrlWindow',
      });
    },
    onClickMicMute() {
      this.audioMuted = !this.audioMuted;
      this.sendMessageToMainWindow({ command: 'toggleMic' });
    },
    onClickCamMute() {
      this.videoMuted = !this.videoMuted;
      this.sendMessageToMainWindow({ command: 'toggleCamera' });
    },
    onClickExit() {
      this.stopAnnotation();
      this.sendMessageToMainWindow({ command: 'exit' });
    },
    onClickScreen() {
      this.stopAnnotation();
      this.sendMessageToMainWindow({ command: 'stopShare' });
    },
    startAnnotation() {
      this.$emit('update:annotationEnabled', true);
    },
    stopAnnotation() {
      this.$emit('update:annotationEnabled', false);
      this.sendMessageToMainWindow({
        command: 'stopShareAnnotation',
      });
    },
    onDrag(e) {
      if (!this.isDragging) return;
      const newLeft =
        this.dragStartPosition.left + e.clientX - this.dragStartPosition.x;
      const newTop =
        this.dragStartPosition.top + e.clientY - this.dragStartPosition.y;
      const domWidth = this.$refs.shareCtrlRef.clientWidth;
      const domHeight = this.$refs.shareCtrlRef.clientHeight;
      const windowWidth = document.body.clientWidth;
      const windowHeight = document.body.clientHeight;
      if (newLeft + domWidth > windowWidth) {
        this.left = windowWidth - domWidth;
      } else if (newLeft < 0) {
        this.left = 0;
      } else {
        this.left = newLeft;
      }
      if (newTop + domHeight > windowHeight) {
        this.top = windowHeight - domHeight;
      } else if (newTop < 0) {
        this.top = 0;
      } else {
        this.top = newTop;
      }
    },
    onDragStart(e) {
      this.dragStartPosition = {
        x: e.clientX,
        y: e.clientY,
        left: this.left,
        top: this.top,
      };
      this.isDragging = true;
      document.addEventListener('mousemove', this.onDrag);
      document.addEventListener('mouseup', this.onDragStop);
      this.syncFrame();
    },
    onDragStop() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.onDragStop);
      this.syncFrame();
    },
    syncFrame() {
      this.$emit('syncFrame', {
        frame: {
          left: this.left,
          top: this.top,
          width: this.$refs.shareCtrlRef.clientWidth,
          height: this.$refs.shareCtrlRef.clientHeight,
        },
      });
    },
    onClickAnnotation() {
      if (!this.annotationEnabled) {
        this.startAnnotation();
      } else {
        this.stopAnnotation();
      }
    },
  },
  mounted() {
    window.electron.ipcRenderer.on('msgToShareWindow', this.handleMessage);
    this.sendMessageToMainWindow({ command: 'getUserMeStatus' });
  },
  beforeDestroy() {
    window.electron.ipcRenderer.off('msgToShareWindow', this.handleMessage);
  },
};
</script>

<style lang="less" scoped>
.control-bar {
  display: flex;
  user-select: none;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: absolute;
  z-index: 1000;
  .sharingInfo {
    height: 20px;
    display: flex;
    font-size: 12px;
    background-color: transparent;
    cursor: default;
    .sharingLeft {
      display: flex;
      justify-content: center;
      align-items: center;
      color: #333;
      width: 200px;
      background-color: #72d46a;
      border-bottom-left-radius: 10px;
      position: relative;
      i {
        position: absolute;
        left: 5px;
        font-size: 18px;
      }
    }

    .sharingRight {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 120px;
      color: #fff;
      background-color: #cf3a37;
      border-bottom-right-radius: 10px;
      cursor: pointer;
      i {
        font-size: 14px;
      }
    }
  }

  .toolbar {
    padding: 0 30px;
    border-radius: 8px;
    background-color: #263852;
    color: #aaa;
    text-align: center;
    display: flex;
    position: relative;
    font-size: 12px;
    overflow: hidden;
    transition: height 0.3s ease-in-out;
    height: 49px;
  }
  .control-btn:hover,
  .control-btn:active,
  .control-btn:focus {
    background-color: transparent;
    color: white;
  }
  .control-btn {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    outline: none;
    height: auto;
    background-color: transparent;
    border: none;
    color: #ddd;
    border-radius: 0px;
    font-size: 12px;
    max-width: 72px;
    min-width: 72px;
    padding: 0;
    position: relative;
    cursor: pointer;
    .iconfont {
      font-size: 24px;
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      top: -2px;
    }
    &__active {
      color: #68d56a !important;
    }
    &:disabled {
      color: #f5f5f5;
      cursor: not-allowed;
      i {
        color: #f5f5f5;
      }
    }
    span {
      position: absolute;
      width: 100%;
      text-align: center;
      bottom: 3px;
      left: 0;
    }

    @keyframes twinkling {
      0% {
        opacity: 0;
      }
      100% {
        opacity: 1;
      }
    }
  }
}
</style>
