<template>
  <div
    class="control-bar"
    @mousedown="onDragStart"
    ref="shareCtrlRef"
    :style="{ left: `${left}px`, top: `${top}px` }"
  >
    <div
      class="toolbar"
      :style="{
        height: isRemoteControling && hideControlBar ? '0' : '49px'
      }"
      @mouseleave="onMouseLeave"
    >
      <button class="control-btn" @click="onClickMicMute">
        <i
          v-if="audioMuted"
          class="iconfont icon-microphone-slash"
          :style="{ color: 'red' }"
        />
        <i v-else class="iconfont icon-microphone" />
        <span>{{ audioMuted ? '取消静音' : '静音' }}</span>
      </button>
      <button class="control-btn" @click="onClickCamMute">
        <i
          v-if="videoMuted"
          class="iconfont icon-video-camera1"
          :style="{ color: 'red' }"
        />
        <i v-else class="iconfont icon-video-camera1" />
        <span>{{ videoMuted ? '打开视频' : '关闭视频' }}</span>
      </button>
      <button class="control-btn share-btn__active" @click="onClickScreen">
        <i
          class="iconfont icon-screen_share"
          :style="{ transform: 'scale(1.24)', color: '#72D46A' }"
        />
        <span
          :style="{
            marginLeft: '0px',
            color: '#72D46A'
          }"
        >
          正在共享
        </span>
      </button>
      <button
        :class="{
          'control-btn': true,
          'control-btn__active': annotationEnabled
        }"
        @click="onClickAnnotation"
        :disabled="isRemoteControling"
      >
        <i class="iconfont icon-pencil1" />
        <span>标注</span>
      </button>
      <button
        v-if="isRemoteControling"
        class="control-btn"
        @click="onClickRemoteCtrl"
      >
        <i class="iconfont icon-remote-ctrl-off" style="color: red" />
        <span>中断控制</span>
      </button>
      <button class="control-btn" @click="onClickExit">
        <i class="iconfont icon-exit" />
        <span>退出</span>
      </button>
    </div>
    <div class="sharingInfo" @mouseenter="onMenuEnterBottom">
      <div class="sharingLeft">
        <i class="iconfont icon-reply" />
        <span>{{
          isRemoteControling ? '你的屏幕正在被远程控制' : '你正在共享屏幕'
        }}</span>
      </div>
      <div class="sharingRight" @click="onClickScreen">
        <i class="iconfont icon-stop" />
        <span :style="{ marginLeft: '5px' }">停止共享</span>
      </div>
    </div>
  </div>
</template>

<script>
const electron = window.require('electron');

export default {
  data() {
    return {
      audioMuted: false,
      videoMuted: false,
      isRemoteControling: false,
      hideControlBar: true,
      left: 600,
      top: 0,
      isDragging: false,
      dragStartPosition: undefined
    };
  },
  props: {
    annotationEnabled: { type: Boolean }
  },
  methods: {
    onMouseLeave() {
      setTimeout(() => {
        this.hideControlBar = true;
      }, 1000);
    },
    onMenuEnterBottom() {
      this.hideControlBar = false;
    },
    handleMessage(_, data) {
      switch (data.command) {
        case 'syncSettings':
          this.audioMuted = data.payload.audioMuted;
          this.videoMuted = data.payload.videoMuted;
          this.isRemoteControling = data.payload.isControl;
          if (this.isRemoteControling) {
            this.top = 0;
            this.left =
              document.body.clientWidth / 2 -
              this.$refs.shareCtrlRef.clientWidth / 2;
            this.hideControlBar = true;
          }
          break;
        case 'stopAnnotation':
          this.$emit('update:annotationEnabled', false);
          break;
        case 'hideShareCtrlWindow':
          this.hideShareCtrlWindow();
          break;
        case 'setSharePosition':
          this.setSharePosition(data.payload);
          break;
        default:
      }
    },
    setSharePosition(detail) {
      const display = electron.remote.app.getDisplayByPosition(detail);
      if (!display) return;
      const displayRect = display.bounds;
      this.left =
        displayRect.width / 2 - this.$refs.shareCtrlRef.clientWidth / 2;
    },
    sendMessageToMainWindow(data) {
      electron.remote.app.sendToMainWindow(data);
    },
    hideShareCtrlWindow() {
      electron.remote.app.hideShareCtrlWindow();
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
      this.hideShareCtrlWindow();
    },
    onClickScreen() {
      this.stopAnnotation();
      this.sendMessageToMainWindow({ command: 'stopShare' });
      this.hideShareCtrlWindow();
    },
    startAnnotation() {
      this.$emit('update:annotationEnabled', true);
    },
    stopAnnotation() {
      this.$emit('update:annotationEnabled', false);
      this.sendMessageToMainWindow({
        command: 'stopShareAnnotation'
      });
    },
    onClickRemoteCtrl() {
      this.sendMessageToMainWindow({ command: 'stopRemoteCtrl' });
      this.isRemoteControling = false;
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
      // 远程控制时禁止拖拽
      if (this.isRemoteControling) return;
      this.dragStartPosition = {
        x: e.clientX,
        y: e.clientY,
        left: this.left,
        top: this.top
      };
      this.isDragging = true;
      document.addEventListener('mousemove', this.onDrag);
      document.addEventListener('mouseup', this.onDragStop);
    },
    onDragStop() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.onDragStop);
    },
    onClickAnnotation() {
      if (!this.annotationEnabled) {
        this.startAnnotation();
      } else {
        this.stopAnnotation();
      }
    }
  },
  mounted() {
    electron.ipcRenderer.on('msgToShareWindow', this.handleMessage);
  },
  beforeDestroy() {
    electron.ipcRenderer.off('msgToShareWindow', this.handleMessage);
  }
};
</script>

<style lang="scss" scoped>
$share-ctrl-bg: #263852;
$share-ctrl-height: 50px;

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
    background-color: $share-ctrl-bg;
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
    i {
      font-size: 20px;
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
    &__contentUpdated {
      &::after {
        position: absolute;
        right: 12px;
        top: 12px;
        content: '';
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background-color: #f93032;
        animation: twinkling 1s infinite ease-in-out alternate;
      }
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
