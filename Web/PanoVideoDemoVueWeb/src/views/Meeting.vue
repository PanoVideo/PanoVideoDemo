<template>
  <div class="playerContainer" @mousemove="onMouseMove">
    <!-- 上方小图列表 -->
    <SmallVideoList />

    <!-- 大图/白板 -->
    <div class="areaUpTools">
      <PanoWhiteboard v-show="isWhiteboardOpen" />
      <MainView v-if="!isWhiteboardOpen" />
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
          <AudioLevel
            :userId="userMe.userId"
            :audioMuted="userMe.audioMuted"
            :fontSize="22"
          />
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
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { RtsService } from '@pano.video/panortc';
import MainView from '../components/userVideo/MainView';
import SmallVideoList from '../components/SmallVideoList';
import PanoWhiteboard from '../components/whiteboard/PanoWhiteboard';
import MeetingInfo from '../components/MeetingInfo';
import AudioLevel from '@/components/AudioLevel';
import { applyForWbAdmin } from '../utils/panorts';
import {
  enableOpenVideo,
  enableOpenAudio,
  updateCaptureDeviceId
} from '../utils/panortc';
import * as Constants from '../constants';

export default {
  name: 'Meeting',
  data() {
    return {
      showToolbar: false,
      isMouseOnToolbar: false,
      hideToolbarTimer: '',
      closeConfirmVisible: false
    };
  },
  components: {
    SmallVideoList,
    PanoWhiteboard,
    MainView,
    MeetingInfo,
    AudioLevel
  },
  computed: {
    ...mapGetters([
      'isWhiteboardOpen',
      'userMe',
      'whiteboardUpdated',
      'wbAdminUser',
      'meetingStatus',
      'meetingEndReason',
      'videoPorfile',
      'whiteboardAvailable'
    ])
  },
  watch: {
    videoPorfile() {
      if (!this.userMe.videoMuted) {
        const r = window.rtcEngine.startVideo(this.videoPorfile);
        console.log('startVideo with new profile type', this.videoPorfile, r);
      }
    },
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
      'setSettingVisible'
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
    async checkEnableOpenAudio() {
      const enable = await enableOpenAudio();
      if (!enable) {
        this.$message.info('没有检测到麦克风');
        return false;
      }
      return true;
    },
    async onClickMicMute() {
      if (!(await this.checkEnableOpenAudio())) {
        return;
      }
      if (this.userMe.audioMuted) {
        window.rtcEngine.unmuteMic();
      } else {
        window.rtcEngine.muteMic();
      }
      this.updateUser({
        userId: this.userMe.userId,
        audioMuted: !this.userMe.audioMuted
      });
    },
    async onClickCamMute() {
      const enable = await enableOpenVideo();
      if (!enable) {
        this.$message.info('没有检测到摄像头');
        return;
      }
      if (this.userMe.videoMuted) {
        await updateCaptureDeviceId();
        window.rtcEngine.startVideo(this.videoPorfile);
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
        const result = window.rtcEngine.startScreen();
        // eslint-disable-next-line prettier/prettier
        if (result && result.code === 'NOT_SUPPORTED') {
          this.$message.info(
            'Safari浏览器从版本13开始支持屏幕共享，当前浏览器不支持'
          );
          window.rtcEngine.stopScreen();
        } else {
          this.updateUser({
            userId: this.userMe.userId,
            screenOpen: !this.userMe.screenOpen
          });
        }
      } else {
        window.rtcEngine.stopScreen();
        this.updateUser({
          userId: this.userMe.userId,
          screenOpen: !this.userMe.screenOpen
        });
      }
    },
    openSetting() {
      this.setSettingVisible(true);
    },
    closeSetting() {
      this.setSettingVisible(false);
    },
    onExit() {
      this.$msgbox
        .confirm('确认退出会议？', '提示')
        .then(this.leaveChannel)
        .catch(() => console.log('取消退出'));
    },
    leaveChannel(goLogin = true) {
      window.rtcEngine.leaveChannel();
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
    }
  },
  async mounted() {
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
    const autoMuteAudio =
      localStorage.getItem(Constants.localCacheKeyMuteMicAtStart) == 'yes';
    const autoOpenVideo =
      localStorage.getItem(Constants.localCacheKeyMuteCamAtStart) == 'no';

    console.log('autoOpenVideo', autoOpenVideo, 'autoMuteAudio', autoMuteAudio);
    if (autoOpenVideo) {
      this.onClickCamMute();
    }
    if (await this.checkEnableOpenAudio()) {
      window.rtcEngine.startAudio();
      this.updateUser({
        userId: this.userMe.userId,
        audioMuted: false
      });
      if (autoMuteAudio) {
        this.onClickMicMute();
      }
    }
  },
  beforeDestroy() {
    this.leaveChannel(false);
    window.onbeforeunload = null;
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

@media all and (max-width: 768px) {
  .toolBar {
    height: 42px;
    border-radius: 5px;
  }
  .btnClass {
    span {
      display: none;
    }
  }
}
</style>
