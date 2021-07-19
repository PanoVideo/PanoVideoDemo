<template>
  <div class="playerContainer" @mousemove="onMouseMove">
    <!-- 上方小图列表 -->
    <SmallVideoList />

    <!-- 大图/白板 -->
    <div class="areaUpTools">
      <PanoWhiteboard v-show="isWhiteboardOpen" />
      <MainView
        v-if="!isWhiteboardOpen"
        @toggleRemoteControl="onToggleRemoteControl"
      />
    </div>

    <!-- 会议信息 -->
    <MeetingInfo />

    <!-- MenuBar -->
    <div
      :class="{
        toolWrapper: true,
        toolWrapperHidden: !(showToolbar || isMouseOnToolbar)
      }"
      @mouseEnter="onMouseEnterMenu"
      @mouseLeave="onMouseLeaveMenu"
    >
      <div class="toolBar">
        <Button class="btnClass" @click="onClickMicMute">
          <i
            v-if="userMe.audioMuted"
            class="iconfont icon-microphone-slash"
            style="color: red"
          />
          <i v-else class="iconfont icon-microphone" />
          <span style="margin-left: 0px">
            {{ userMe.audioMuted ? '取消静音' : '静音' }}
          </span>
        </Button>

        <Button class="btnClass" @click="onClickCamMute">
          <i
            v-if="userMe.videoMuted"
            class="iconfont icon-video-camera1"
            style="color: red"
          />
          <i v-else class="iconfont icon-video-camera1" />
          <span style="margin-left: 0px">
            {{ userMe.videoMuted ? '打开视频' : '关闭视频' }}
          </span>
        </Button>
        <Button
          :class="{
            btnClass: true,
            btnClass__contentUpdated: !isWhiteboardOpen && whiteboardUpdated
          }"
          @click="onClickWhiteboard"
        >
          <i class="iconfont icon-pencil-square-o" />
          <span style="margin-left: 0px">
            {{ isWhiteboardOpen ? '关闭白板' : '打开白板' }}
          </span>
        </Button>
        <Button
          :class="{
            btnClass: true,
            btnClass__active: userMe.screenOpen
          }"
          @click="onClickScreen"
        >
          <i class="iconfont icon-external-link" />
          <span style="margin-left: 0px">
            {{ userMe.screenOpen ? '正在共享' : '共享屏幕' }}
          </span>
        </Button>
        <!-- <Button class="btnClass" @click="onClickInvite">
          <i class="iconfont icon-user-plus" />
          <span style="margin-left: 0px">邀请</span>
        </Button> -->
        <Button class="btnClass" @click="openSetting">
          <i class="iconfont icon-cog" />
          <span style="margin-left: 0px">设置</span>
        </Button>

        <Button class="btnClass" @click="onExit">
          <i class="iconfont icon-exit" />
          <span style="margin-left: 0px">退出</span>
        </Button>
      </div>
    </div>
    <Setting :visible.sync="settingVisible" />
    <ShareSelector
      v-if="shareSelectorVisible"
      :visible.sync="shareSelectorVisible"
    />
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { VideoScalingMode } from '@pano.video/panortc-electron-sdk';
import { RtsService, RemoteControl } from '@pano.video/whiteboard';
import MainView from '../components/userVideo/MainView';
import SmallVideoList from '../components/SmallVideoList';
import PanoWhiteboard from '../components/whiteboard/PanoWhiteboard';
import MeetingInfo from '../components/MeetingInfo';
import ShareSelector from '../components/ShareSelector';
import Setting from '../components/Setting';
import { applyForWbAdmin } from '../setup-panortc';
import robotjs from '@pano.video/robotjs';

const electron = window.require('electron');

export default {
  name: 'Meeting',
  data() {
    return {
      showToolbar: false,
      isMouseOnToolbar: false,
      hideToolbarTimer: '',
      closeConfirmVisible: false,
      settingVisible: false,
      shareSelectorVisible: false,
      isControlledByOthers: false, // 是否正在被别人远程控制
      sharedScreenOrAppDispay: undefined, // 共享桌面时窗口位置信息
      captureRect: undefined,
      remoteControl: undefined // 远程控制对象
    };
  },
  components: {
    SmallVideoList,
    PanoWhiteboard,
    MainView,
    MeetingInfo,
    Setting,
    ShareSelector
  },
  computed: {
    ...mapGetters([
      'isWhiteboardOpen',
      'userMe',
      'whiteboardUpdated',
      'wbAdminUser',
      'meetingStatus',
      'meetingEndReason',
      'myVideoProfileType',
      'whiteboardAvailable',
      'getUserById',
      'isRemoteControling'
    ])
  },
  watch: {
    meetingStatus() {
      switch (this.meetingStatus) {
        case 'ended':
          this.$alert(`您已离开会议，因为${this.meetingEndReason}`, '提示')
            .then(this.leaveChannel)
            .catch(this.leaveChannel);
          break;
        case 'countdownover':
          this.$alert('会议时间已用完，会议已结束', '提示')
            .then(this.leaveChannel)
            .catch(this.leaveChannel);
          break;
        case 'reconnecting':
          this.$message.info('已从服务器断开，正在重连...');
          break;
        case 'disconnected':
          this.$confirm(
            '无法连接到服务器，已离开会议，请尝试重新加入会议',
            '提示'
          )
            .then(this.leaveChannel)
            .catch(this.leaveChannel);
          break;
        case 'connected':
          this.$message.success('会议重连成功');
          break;
        default:
          break;
      }
    }
  },
  methods: {
    ...mapMutations([
      'updateUser',
      'setWhiteboardOpenState',
      'resetUserStore',
      'resetMeetingStore',
      'setIsRemoteControling'
    ]),
    onMouseMove() {
      this.showToolbar = true;
      clearTimeout(this.hideToolbarTimer);
      this.hideToolbarTimer = setTimeout(() => {
        this.showToolbar = false;
      }, 2000);
    },
    onMouseEnterMenu() {
      this.isMouseOnToolbar = true;
    },
    onMouseLeaveMenu() {
      this.isMouseOnToolbar = false;
    },
    onClickMicMute() {
      if (this.userMe.audioMuted) {
        window.rtcEngine.startAudio();
      } else {
        window.rtcEngine.stopAudio();
      }
      this.updateUser({
        userId: this.userMe.userId,
        audioMuted: !this.userMe.audioMuted
      });
    },
    onClickCamMute() {
      if (this.userMe.videoMuted) {
        window.rtcEngine.startVideo(this.userMe.videoDomRef, {
          profile: this.myVideoProfileType,
          scaling: VideoScalingMode.Fit,
          mirror: true
        });
      } else {
        if (this.userMe.videoAnnotationOpen) {
          // 关闭视频标注
          RtsService.getInstance()
            .getAnnotation(this.userMe.userId, 'video')
            ?.stop();
        }
        window.rtcEngine.stopVideo();
      }
      this.updateUser({
        userId: this.userMe.userId,
        videoMuted: !this.userMe.videoMuted
      });
    },
    onClickWhiteboard() {
      if (!this.whiteboardAvailable) {
        this.$message.info('白板尚未准备好，请稍后重试');
        return;
      }
      this.setWhiteboardOpenState(!this.isWhiteboardOpen);
      // 如果没有演示用户，且是我打开了白板，那么广播我是演示用户
      if (this.isWhiteboardOpen && !this.wbAdminUser) {
        applyForWbAdmin();
      }
    },
    onClickScreen() {
      if (!this.userMe.screenOpen) {
        this.shareSelectorVisible = true;
        this.updateUser({
          userId: this.userMe.userId,
          screenOpen: !this.userMe.screenOpen
        });
      } else {
        window.rtcEngine.stopScreen();
        window.rtcEngine.stopSoundCardShare();
        this.resetRemoteControlState();
        this.resetAnonotation();
        this.updateUser({
          userId: this.userMe.userId,
          screenOpen: !this.userMe.screenOpen
        });
      }
    },
    openSetting() {
      this.settingVisible = true;
    },
    closeSetting() {
      this.settingVisible = false;
    },
    onExit() {
      this.$msgbox
        .confirm('确认退出会议', '提示')
        .then(this.leaveChannel)
        .catch(e => console.error(e));
    },
    leaveChannel(goLogin = true) {
      electron.remote.getCurrentWindow().setFullScreen(false);
      window.rtcEngine.leaveChannel();
      this.resetShareWindow();
      window.rtcWhiteboard && window.rtcWhiteboard.leaveChannel();
      if (this.userMe.videoAnnotationOpen) {
        // 关闭视频标注
        RtsService.getInstance()
          .getAnnotation(this.userMe.userId, 'video')
          ?.stop();
      }
      this.resetMeetingStore();
      this.resetUserStore();
      window.onbeforeunload = null;
      goLogin && this.$router.replace({ name: 'Login' });
    },
    resetAnonotation() {
      // TODO 关闭标注功能
    },
    resetRemoteControlState() {
      // 重置远程控制和共享控制窗口
      this.remoteControl?.stop();
      this.sendmsgToShareWindow({
        command: 'resetState'
      });
    },
    // share 窗口显示提示
    showToastInShareWindow(msg) {
      this.sendmsgToShareWindow({
        command: 'showIndication',
        payload: { text: msg }
      });
    },
    sendmsgToShareWindow(msaage) {
      electron.remote.app.sendToShareWindow(msaage);
    },
    onRemoteControlStateChange(control) {
      this.isControlledByOthers = control;
      if (!control && this.userMe.screenShareType === 'screen') {
        console.log('结束远程控制，关闭优化模式');
        window.rtcEngine.cancelUserControl(this.userMe.userId);
      }
      this.sendmsgToShareWindow({
        command: 'syncSettings',
        payload: {
          videoMuted: this.userMe.videoMuted,
          audioMuted: this.userMe.audioMuted,
          isControl: this.isControlledByOthers
        }
      });
    },
    // 当桌面共享的屏幕id，或者共享的app所处的屏幕变化时触发，首次共享时也会触发
    // 用来确定当前正在共享哪个窗口
    onScreenCaptureDisplayChanged(displayId, info) {
      console.log('捕获屏幕/APP所处的屏幕变化', displayId, info);
      const { left, top, right, bottom } = info;
      this.sharedScreenOrAppDispay = {
        displayId,
        x: left,
        y: top,
        width: right - left,
        height: bottom - top
      };
      // 设置桌面共享控制窗口转移到对应的屏幕
      electron.remote.app.setShareWindow(this.sharedScreenOrAppDispay);
    },
    // 屏幕捕捉分辨率变化
    // 一般桌面的分辨率是不会变化的，共享app时app可以由用户手动拖拽放大缩小
    onScreenCaptureRegionChanged(info) {
      const { left, top, right, bottom } = info;
      const payload = {
        x: left,
        y: top,
        width: right - left,
        height: bottom - top,
        shareType: this.userMe.screenShareType
      };
      console.log('共享窗口/app 的位置变化: ', payload);
      this.sendmsgToShareWindow({ command: 'setSharePosition', payload });
      // 远程控制部分代码
      this.captureRect = payload;
      this.remoteControl?.setShareRect(this.captureRect);
    },
    // 其他用户屏幕共享分辨率变化的回调
    onUserScreenResolutionChanged(userId, width, height) {
      this.updateUser({
        userId,
        shareResolution: { x: 0, y: 0, width, height }
      });
    },
    onConnectDisconnected() {
      if (this.isControlledByOthers) {
        this.showToastInShareWindow('远程控制连接已断开');
        this.onRemoteControlStateChange(false);
      }
    },
    onControlCancelled() {
      this.showToastInShareWindow('对方已取消远程控制');
      this.onRemoteControlStateChange(false);
    },
    // 远程控制回复
    onControlResponse(result) {
      // 长时间没有响应请求，Hide 提示框
      if (result === -16) {
        this.sendmsgToShareWindow({ command: 'resetState' });
      }
    },
    // 打开或关闭远程控制
    onToggleRemoteControl(isRemoteControling) {
      this.setIsRemoteControling(isRemoteControling);
      if (isRemoteControling) {
        electron.remote.getCurrentWindow().setFullScreen(true);
      } else {
        electron.remote.getCurrentWindow().setFullScreen(false);
      }
    },
    // 清除事件监听
    removeObservers() {
      this.remoteControl?.off(
        RemoteControl.Events.onControlRequest,
        this.showRequestModal
      );
      this.remoteControl?.off(
        RemoteControl.Events.onConnectDisconnected,
        this.onConnectDisconnected
      );
      this.remoteControl?.off(
        RemoteControl.Events.onControlCancelled,
        this.onControlCancelled
      );
    },
    showRequestModal(msg) {
      const targetUserId = msg.from;
      const user = this.getUserById(targetUserId);
      if (!user) {
        return;
      }
      this.sendmsgToShareWindow({
        command: 'showRemoteControlConfirmDialog',
        payload: {
          userName: user.userName,
          userId: user.userId
        }
      });
    },
    // 远程控制连接建立
    onRemoteControlConnected(remoteControl) {
      this.removeObservers();
      this.remoteControl = remoteControl;
      this.remoteControl.setRobot(robotjs);
      this.remoteControl.setShareRect(this.captureRect);
      this.remoteControl.on(
        RemoteControl.Events.onConnectDisconnected,
        this.onConnectDisconnected
      );
      this.remoteControl.on(
        RemoteControl.Events.onControlRequest,
        this.showRequestModal
      );
      this.remoteControl.on(
        RemoteControl.Events.onControlCancelled,
        this.onControlCancelled
      );
      this.remoteControl.on(
        RemoteControl.Events.onControlResponse,
        this.onControlResponse
      );
    },
    resetShareWindow() {
      this.resetAnonotation();
      this.resetRemoteControlState();
      this.sendmsgToShareWindow({ command: 'hideShareCtrlWindow' });
    },
    handleCommand(_, data) {
      switch (data.command) {
        case 'stopShare':
          this.onClickScreen();
          break;
        case 'exit':
          // 退出前取消共享
          this.userMe.screenOpen && this.onClickScreen();
          this.resetRemoteControlState();
          this.resetAnonotation();
          this.onExit();
          break;
        case 'toggleMic':
          this.onClickMicMute();
          break;
        case 'toggleCamera':
          this.onClickCamMute();
          break;
        case 'replyForRemoteControl':
          if (data.payload.confirm) {
            this.remoteControl?.acceptControl(data.payload.userId);
            const user = this.getUserById(data.payload.userId);
            if (user) {
              this.showToastInShareWindow(
                `您的屏幕正在被 ${user.userName} 远程控制`
              );
            }
            this.onRemoteControlStateChange(true);
            if (this.userMe.screenShareType === 'screen') {
              console.log('接受远程控制，开启优化模式');
              window.rtcEngine.acceptUserControl(`${data.payload.userId}`);
            }
          } else {
            this.remoteControl?.rejectControl(data.payload.userId);
          }
          break;
        case 'stopRemoteCtrl':
          this.remoteControl?.remoteUserId &&
            this.remoteControl?.cancelControl(this.remoteControl.remoteUserId);
          break;
        default:
          break;
      }
    }
  },
  mounted() {
    if (this.meetingStatus !== 'connected') {
      this.$router.replace({ name: 'Login' });
      return;
    }
    window.onbeforeunload = e => {
      const evt = e || window.event;
      if (evt) {
        evt.returnValue = '确定离开？';
      }
      return '确定离开？';
    };
    // 监听远程控制连接请求
    RtsService.getInstance().on(
      RtsService.Events.remoteControlConnected,
      this.onRemoteControlConnected
    );
    // 其他窗口的IPC消息
    electron.ipcRenderer.on('msgToMainWindow', this.handleCommand);
    // 捕获桌面共享时，屏幕/APP所处的屏幕变化
    window.rtcEngine.on(
      'screenCaptureDisplayChanged',
      this.onScreenCaptureDisplayChanged
    );
    window.rtcEngine.on(
      'screenCaptureRegionChanged',
      this.onScreenCaptureRegionChanged
    );
    window.rtcEngine.on(
      'userScreenResolutionChanged',
      this.onUserScreenResolutionChanged
    );
  },
  beforeDestroy() {
    this.leaveChannel(false);
    this.resetShareWindow();
    window.onbeforeunload = null;
    // 取消监听远程控制连接请求
    RtsService.getInstance().off(
      RtsService.Events.remoteControlConnected,
      this.onRemoteControlConnected
    );
    // 其他窗口的IPC消息
    electron.ipcRenderer.off('msgToMainWindow', this.handleCommand);
    window.rtcEngine.off(
      'screenCaptureDisplayChanged',
      this.onScreenCaptureDisplayChanged
    );
    window.rtcEngine.off(
      'screenCaptureRegionChanged',
      this.onScreenCaptureRegionChanged
    );
    window.rtcEngine.off(
      'userScreenResolutionChanged',
      this.onUserScreenResolutionChanged
    );
  }
};
</script>
<style lang="scss" scoped>
$playerLeftOffset: 0;
$playerWidth: 100%;

.playerContainer {
  position: relative;
  width: $playerWidth;
  left: $playerLeftOffset;
  top: 0px;
  height: 100vh;
  background-color: #222;
  display: flex;
  flex-direction: column;
}

.areaUpTools {
  width: 100%;
  flex: 1;
  overflow: hidden;
}

.toolWrapper {
  position: absolute;
  z-index: 30;
  width: 100%;
  // height:60px;
  bottom: 0px;
  text-align: center;
  transition: all 0.5s ease-in-out;
  opacity: 1;
}

.toolWrapperHidden {
  bottom: -30px;
  opacity: 0;
}

.toolBar {
  position: absolute;
  bottom: 0px;
  left: 50%;
  transform: translate(-50%);
  // position: absolute;
  display: inline-block;
  // z-index: 2;
  height: 60px;
  padding: 0 20px;
  border-radius: 5px 5px 0 0;
  // left: $playerLeftOffset;
  // display: block;
  // bottom: 0px;
  // position: relative;
  // bottom:0;
  background-color: rgba(82, 83, 85, 0.6);
  color: #aaaaaa;
  text-align: center;
  display: flex;
}
.btnClass:hover,
.btnClass:active,
.btnClass:focus {
  background-color: transparent;
  color: white;
}
.btnClass {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  outline: none;
  // width:100%;
  height: 100%;
  background-color: transparent;
  border: none;
  color: #ddd;
  border-radius: 0px;
  font-size: 12px;
  max-width: 74px;
  position: relative;
  padding: 4px 12px;
  i {
    font-size: 22px;
  }
  &__active {
    color: #68d56a !important;
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
</style>
