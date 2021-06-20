<template>
  <div class="share">
    <!-- 控制条 -->
    <ControlBar
      @mouseenter.native="onMouseEnterCtrl"
      @focus.native="onMouseEnterCtrl"
      @mouseleave.native="onMouseLeaveCtrl"
      @blur.native="onMouseLeaveCtrl"
    />

    <!-- 绿框 -->
    <div
      :style="{
        position: 'absolute',
        ...(shareType === 'application' && shareIndicationPosition
          ? {
              left: `${shareIndicationPosition.left}px`,
              top: `${shareIndicationPosition.top}px`,
              width: `${shareIndicationPosition.width}px`,
              height: `${shareIndicationPosition.height}px`
            }
          : {
              width: '100%',
              height: '100%',
              left: 0,
              top: 0
            })
      }"
    >
      <div class="share__corner" />
      <div class="share__corner" />
    </div>

    <!-- 黑底提示文字 -->
    <div class="global-indication">{{ indicationText }}</div>

    <!-- 远程控制确认框 -->
    <div v-if="modalVisible" class="confirm__modal">
      <div class="confirm__modal__body">
        {{ remoteControlUserName }} 请求远程控制您的屏幕
      </div>
      <div class="confirm__modal__footer">
        <Button @click="replyRemoteControl(false)">
          拒绝
        </Button>
        <Button
          type="primary"
          :style="{ marginLeft: '20px' }"
          @click="replyRemoteControl(true)"
        >
          同意
        </Button>
      </div>
    </div>
  </div>
</template>

<script>
import { Button } from 'element-ui';
import ControlBar from './ControlBar';

const electron = window.require('electron');

const CORNOR_BORDER_WIDTH = 8; // 角落的指示颜色条宽度

export default {
  data() {
    return {
      modalVisible: false,
      shareIndicationPosition: undefined,
      remoteControlUserName: undefined,
      shareType: 'screen', // 'screen' | 'application'
      indicationText: '',
      indicationVisible: false,
      // 共享内容的大小
      shareSize: undefined,
      remoteControlUserId: '',
      ignoreMouseEvents: true // 是否穿透鼠标
    };
  },
  components: {
    Button,
    ControlBar
  },
  methods: {
    handleMessage(_, data) {
      switch (data.command) {
        case 'setSharePosition':
          this.setSharePosition(data.payload);
          break;
        case 'showRemoteControlConfirmDialog':
          this.modalVisible = true;
          this.remoteControlUserName = data.payload.userName;
          this.remoteControlUserId = data.payload.userId;
          electron.remote.getCurrentWindow().setIgnoreMouseEvents(false);
          break;
        case 'showIndication':
          this.indicationText = data.payload.text;
          this.indicationVisible = true;
          setTimeout(() => {
            this.indicationVisible = false;
          }, 2000);
          break;
        case 'resetState':
          this.resetState();
          break;
        default:
      }
    },
    resetState() {
      this.modalVisible = false;
      this.remoteControlUserName = '';
      this.remoteControlUserId = '';
      electron.remote
        .getCurrentWindow()
        .setIgnoreMouseEvents(true, { forward: true });
    },
    /**
     * @param detail { x: number; y: number; width: number; height: number; shareType: 'screen' | 'application';}
     * 是共享整块屏幕还是共享 application，用于annotation计算位置
     */
    setSharePosition(detail) {
      this.shareSize = detail;
      if (detail.shareType === 'screen') {
        this.annotationPosition = {
          width: detail.width,
          height: detail.height,
          left: 0,
          top: 0
        };
        this.shareIndicationPosition = this.annotationPosition;
      } else {
        const display = electron.remote.app.getDisplayByPosition(detail);
        if (!display) return;
        const displayRect = display.bounds;
        // windows 高分屏的屏幕分辨率和 rtcEngine 上报的share位置是一致的，
        // 但是浏览器的最大宽度是经过 devicePixelRatio 换算的，需要再转换一下
        const scaleRatio = process.platform === 'darwin' ? 1 : devicePixelRatio;
        this.annotationPosition = {
          width: detail.width / scaleRatio,
          height: detail.height / scaleRatio,
          left: (detail.x - displayRect.x) / scaleRatio,
          top: (detail.y - displayRect.y) / scaleRatio
        };
        this.shareIndicationPosition = {
          width: detail.width / scaleRatio + CORNOR_BORDER_WIDTH * 2,
          height: detail.height / scaleRatio + CORNOR_BORDER_WIDTH * 2,
          left: (detail.x - displayRect.x - CORNOR_BORDER_WIDTH) / scaleRatio,
          top: (detail.y - displayRect.y - CORNOR_BORDER_WIDTH) / scaleRatio
        };
      }
      this.shareType = detail.shareType;
    },
    sendMessageToMainWindow(data) {
      electron.remote.app.sendToMainWindow(data);
    },
    replyRemoteControl(confirm) {
      this.modalVisible = false;
      electron.remote
        .getCurrentWindow()
        .setIgnoreMouseEvents(true, { forward: true });
      electron.remote.app.sendToMainWindow({
        command: 'replyForRemoteControl',
        payload: { confirm, userId: this.remoteControlUserId }
      });
    },
    onMouseEnterCtrl() {
      electron.remote.getCurrentWindow().setIgnoreMouseEvents(false);
    },
    onMouseLeaveCtrl() {
      // modal 可见时不需要处理
      if (this.ignoreMouseEvents && !this.modalVisible) {
        electron.remote
          .getCurrentWindow()
          .setIgnoreMouseEvents(true, { forward: true });
        // fix windows上有时会触发当选择穿透按钮时
        // annotation 窗口会被取消 AlwaysOnTop，会被别的窗口遮住
        electron.remote
          .getCurrentWindow()
          .setAlwaysOnTop(true, 'screen-saver', 2);
      }
    }
  },
  mounted() {
    electron.remote
      .getCurrentWindow()
      .setIgnoreMouseEvents(true, { forward: true });
    electron.ipcRenderer.on('msgToShareWindow', this.handleMessage);
  },
  beforeDestroy() {
    electron.ipcRenderer.off('msgToShareWindow', this.handleMessage);
  }
};
</script>

<style lang="scss" scoped>
$corner-color: rgba(97, 211, 134, 0.7);
$share-ctrl-bg: #263852;

.share {
  width: 100vw;
  height: 100vh;
  background-color: transparent;
  position: relative;
  z-index: 100;
  overflow: hidden;
  &__corner {
    position: absolute;
    width: 120px;
    height: 120px;
    border: 8px solid $corner-color;
    pointer-events: none;
    z-index: 1;
  }
  &__corner:nth-of-type(1) {
    left: 0;
    top: 0;
    border-right-color: transparent;
    border-bottom-color: transparent;
  }
  &__corner:nth-of-type(2) {
    right: 0;
    bottom: 0;
    border-left-color: transparent;
    border-top-color: transparent;
  }
}

.confirm__modal {
  position: fixed;
  left: 50%;
  top: 40%;
  transform: translateX(-50%);
  z-index: 101;
  background-color: #fff;
  background-clip: padding-box;
  border: 0;
  border-radius: 2px;
  box-shadow: 2px 2px 8px rgba(20, 14, 14, 0.2);
  pointer-events: auto;
  min-width: 400px;
  &__body {
    padding: 24px;
    font-size: 15px;
    font-weight: bold;
    line-height: 1.5715;
    word-wrap: break-word;
    padding-top: 40px;
    text-align: left;
  }
  &__footer {
    padding: 10px 16px;
    margin-bottom: 10px;
    display: flex;
    justify-content: flex-end;
    background: 0 0;
    border-radius: 0 0 2px 2px;
  }
}
</style>
