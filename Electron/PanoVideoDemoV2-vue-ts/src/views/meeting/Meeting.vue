<template>
  <div
    class="pvc"
    @mousemove="onMouseMove"
    :style="
      $IS_ELECTRON
        ? { top: fullscreen || isSharing ? '0px' : isWin ? '32px' : '28px' }
        : {}
    "
  >
    <FailoverIndicator
      v-show="meetingStatus === 'reconnecting' && !isSharing"
    />
    <MediaList />
    <Roster :visible.sync="rosterVisible" @cancel="onRosterCancel" />
    <StatusBar
      ref="statusBar"
      v-if="!isSharing"
      :showToolbar="showToolbar"
      @onClickMicMute="onClickMicMute"
      @onClickCamMute="onClickCamMute"
      @onClickWhiteboard="onClickWhiteboard"
      @onClickScreen="onClickScreen"
      @onClickLive="onClickLive"
      @onClickRoster="onClickRoster"
      @openSetting="openSetting"
      @onClickExit="onClickExit"
    />
    <div class="main-content" v-show="!isSharing">
      <MainView v-if="!isWhiteboardOpen && !isSharing" />
      <PanoWhiteboard />
    </div>
    <Setting :visible="settingShow" @close="closeSetting" />
    <LeaveConfirm :visible="closeConfirmVisible" @confirm="leaveChannel" />
    <WindowShareSelector
      v-if="shareSelectVisible && $IS_ELECTRON"
      :shareSelectVisible.sync="shareSelectVisible"
    />
    <WindowFrame v-if="$IS_ELECTRON" :maxBtnEnable="true" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import StatusBar from '@/components/Statusbar';
import {
  enableOpenAudio,
  enableOpenVideo,
  updateCaptureDeviceId,
} from '@/pano';
import * as mutations from '@/store/mutations';
import * as actions from '@/store/actions';
import FailoverIndicator from '@/components/FailoverIndicator';
import MainView from '@/components/video/MainView';
import Setting from '@/components/setting/index.vue';
import MediaList from '@/components/mediaList/MediaList';
import PanoWhiteboard from '@/components/whiteboard/PanoWhiteboard';
import LeaveConfirm from '@/components/LeaveConfirm';
import Roster from '@/components/Roster.vue';
import {
  exitFullscreen,
  info,
  SAFARI_15_1,
  SAFARI_15_1_MESSAGE,
} from '@/utils/common';
import { MeetingStatus } from '@/store/modules/meeting';
import WindowShareSelector from '@/components/WindowShareSelector';
import ShareAnnotationMixin from '@/views/meeting/MeetingShareAnnotation.mixin';
import MeetingMixin from '@/views/meeting/Meeting.mixin';
import WindowFrame from '@/components/WindowFrame.vue';
import { Modal } from 'ant-design-vue';
import { LogUtil } from '@/utils';
import find from 'lodash-es/find';
import { subscribe, unsubscribe } from '@/pano';
import { RtsService } from '@pano.video/panorts';

let hideToolbarTimer;

export default {
  props: {
    userId: String,
  },
  mixins: window.IS_ELECTRON ? [ShareAnnotationMixin, MeetingMixin] : [],
  data() {
    return {
      needHide: true,
      showToolbar: true,
      settingShow: false,
      feedbackShow: false,
      audioFeedbackShow: false,
      isSelScreenModalVisible: false,
      liveModalVisible: false,
      shareSelectVisible: false,
      rosterVisible: false,
      isWin: process.platform !== 'darwin',
    };
  },
  components: {
    StatusBar,
    FailoverIndicator,
    MainView,
    Setting,
    MediaList,
    PanoWhiteboard,
    LeaveConfirm,
    WindowShareSelector,
    WindowFrame,
    Roster,
  },
  computed: {
    ...mapGetters([
      'userMe',
      'getUserById',
      'videoProfileType',
      'whiteboardAvailable',
      'hostId',
      'hostUser',
      'meetingStatus',
      'autoMuteAudio',
      'autoOpenVideo',
      'isWhiteboardOpen',
      'meetingEndReason',
      'enableAAGC',
      'closeConfirmVisible',
      'videoSubscribeList',
      'fullscreen',
      'screens',
      'isHost',
    ]),
    isSharing() {
      return this.userMe.screenOpen && window.IS_ELECTRON;
    },
  },
  watch: {
    screens() {
      LogUtil('screens changed:', this.screens);
    },
    meetingStatus() {
      if (
        this.meetingStatus === MeetingStatus.countdownover ||
        this.meetingStatus === MeetingStatus.ended
      ) {
        Modal.destroyAll();
        this.resetShareCtrlWindow();
        Modal.info({
          title: '通知',
          content:
            this.meetingStatus === MeetingStatus.countdownover
              ? '会议时间已用完，会议已结束'
              : `您已离开会议，因为${this.meetingEndReason}`,
          okText: '确认',
          onOk: this.leaveChannel,
        });
      }
    },
    videoProfileType() {
      this.startVideo();
    },
    videoSubscribeList(newList, oldList) {
      // userId: string; videoType: MediaType; showInMainView: boolean;
      // 新的列表中，userId/videoType/showInMainView 任何一个变化的都需要重新sub
      const subList = newList.filter(
        (newUser) =>
          !find(
            oldList,
            (oldUser) =>
              oldUser.userId === newUser.userId &&
              oldUser.videoType === newUser.videoType &&
              oldUser.showInMainView === newUser.showInMainView
          )
      );
      // 旧的列表中，userId/videoType 不再被订阅的需要 unsubscribe
      // 不考虑 showInMainView 不同的情况，这种情况是大小图切换的场景
      const unsubList = oldList.filter(
        (oldUser) =>
          !find(
            newList,
            (newUser) =>
              oldUser.userId === newUser.userId &&
              oldUser.videoType === newUser.videoType
          )
      );
      // 先 unsubscribe 再 sub
      unsubList.forEach((u) => {
        unsubscribe(u.userId, u.videoType);
      });
      subList.forEach((u) => subscribe(u.userId, u.videoType));
    },
    hostId() {
      if (!this.hostUser) {
        return;
      }
      if (this.hostUser === this.userMe) {
        info('您已经成为主持人');
      } else {
        info(`当前主持人是${this.hostUser.userName || this.hostUser.userId}`);
      }
    },
    ['userMe.audioType'](type) {
      if (type === 'voip') {
        this.startAudio().then(() => {
          if (this.userMe.audioMuted) {
            window.rtcEngine.muteMic();
          }
        });
        window.rtcEngine.unmuteAudioPlayout();
      } else {
        LogUtil('pstn stopAudio');
        window.rtcEngine.stopAudio();
        window.rtcEngine.muteAudioPlayout();
        this.audioSelectionVisible = false;
      }
    },
  },
  methods: {
    onMouseMove() {
      if (!this.needHide) {
        return;
      }
      this.hideToolbar();
      this.showToolbar = true;
    },
    hideToolbar() {
      clearTimeout(hideToolbarTimer);
      hideToolbarTimer = setTimeout(() => {
        this.showToolbar = false;
        this.$refs.statusBar?.hidePopover();
      }, 2000);
    },
    /**
     * 能否打开音频
     */
    async checkEnableOpenAudio() {
      const enable = await enableOpenAudio();
      if (!enable) {
        this.$store.commit(mutations.UPDATE_USER_ME, { audioMuted: true });
        this.$message.info('没有检测到麦克风');
        return false;
      }
      return true;
    },
    async onClickMicMute() {
      const r = await this.checkEnableOpenAudio();
      if (r) {
        let res = false;
        if (this.userMe.audioMuted) {
          res = window.rtcEngine.unmuteMic().code === window.QResult.OK;
        } else {
          res = window.rtcEngine.muteMic().code === window.QResult.OK;
        }
        if (res) {
          this.$store.commit(mutations.UPDATE_USER_ME, {
            audioMuted: !this.userMe.audioMuted,
          });
        } else {
          this.$message.info(
            this.userMe.audioMuted ? '解除静音失败' : '静音失败'
          );
        }
      }
    },
    onClickCamMute() {
      if (SAFARI_15_1) {
        this.$message.info(SAFARI_15_1_MESSAGE);
        return;
      }
      if (this.userMe.videoMuted) {
        this.startVideo();
      } else {
        this.userMe.videoDomRef.innerHTML = '';
        window.rtcEngine.stopVideo();
      }
      this.$store.commit(mutations.UPDATE_USER_ME, {
        videoMuted: !this.userMe.videoMuted,
      });
    },
    async startVideo() {
      const enable = await enableOpenVideo();
      if (!enable) {
        this.$store.commit(mutations.UPDATE_USER_ME, { videoMuted: true });
        this.$message.info('没有检测到摄像头');
        return;
      }
      await updateCaptureDeviceId();
      window.rtcEngine.startVideo(this.videoProfileType);
    },
    onClickWhiteboard() {
      if (!this.whiteboardAvailable) {
        this.$message.info('白板尚未准备好，请稍后重试');
        return;
      }
      this.$store.dispatch(
        actions.SET_IS_WHITEBOARD_OPEN,
        !this.isWhiteboardOpen
      );
      if (this.userMe.screenOpen) {
        this.stopShare();
      }
    },
    onClickScreen() {
      if (!this.userMe.screenOpen) {
        this.startShare();
      } else {
        this.stopShare();
      }
    },
    startShare() {
      if (this.$IS_ELECTRON) {
        this.shareSelectVisible = true;
        return;
      }
      const result = window.rtcEngine.startScreen();
      if (result && result.code === 'NOT_SUPPORTED') {
        this.$message.info(
          'Safari浏览器从版本13开始支持屏幕共享，当前浏览器不支持'
        );
        window.rtcEngine.stopScreen();
      } else {
        this.$store.commit(mutations.UPDATE_USER_ME, {
          screenOpen: !this.userMe.screenOpen,
        });
        // 关闭白板
        if (this.isHost) {
          this.$store.dispatch(actions.SET_IS_WHITEBOARD_OPEN, false);
        }
      }
    },
    stopShare() {
      this.$store.dispatch(actions.STOP_SHARE_SCREEN);
    },
    onClickLive() {
      this.liveModalVisible = true;
    },
    onLiveModalCancel() {
      this.liveModalVisible = false;
    },
    onClickRoster() {
      this.rosterVisible = true;
    },
    onRosterCancel() {
      this.rosterVisible = false;
    },
    openSetting() {
      this.settingShow = true;
    },
    closeSetting() {
      this.settingShow = false;
    },
    onClickExit() {
      this.$store.commit(mutations.SET_CLOSE_CONFIRM_VISIBLE, true);
    },
    onWinSelectorClose() {
      this.isSelScreenModalVisible = false;
    },
    startAudio() {
      const start = async (resolve, reject) => {
        if (await this.checkEnableOpenAudio()) {
          window.rtcEngine.startAudio();
          window.IS_ELECTRON && window.rtcEngine?.enableAAGC(this.enableAAGC);
          resolve();
        }
        reject();
      };
      return new Promise(start);
    },
    leaveChannel() {
      this.$router.replace({ name: 'Login' });
    },
    resetShareCtrlWindow() {
      if (!window.IS_ELECTRON) return;
      this.stopShare();
    },
    onVideoAnnotationStart() {
      if (this.userMe.screenOpen) {
        this.stopShare();
        this.$message.info('主持人开启了视频标注，您的桌面共享已停止');
      }
    },
  },
  mounted() {
    this.videoSubscribeList.forEach((u) => subscribe(u.userId, u.videoType));
    if (this.autoOpenVideo) {
      this.startVideo();
      this.$store.commit(mutations.UPDATE_USER_ME, { videoMuted: false });
    }
    this.startAudio().then(() => {
      if (this.autoMuteAudio) {
        window.rtcEngine.muteMic();
      }
      this.$store.commit(mutations.UPDATE_USER_ME, {
        audioMuted: this.autoMuteAudio,
      });
    });
    window.onbeforeunload = (e) => {
      if (window.IS_ELECTRON) {
        this.onClickExit();
      } else {
        const evt = e || window.event;
        if (evt) {
          evt.returnValue = '确定离开？';
        }
        return '确定离开？';
      }
    };
  },
  beforeDestroy() {
    exitFullscreen();
    RtsService.getInstance().off(
      RtsService.Events.videoAnnotationStart,
      this.onVideoAnnotationStart
    );
    if (this.userMe.videoAnnotationOn || this.userMe.shareAnnotationOn) {
      RtsService.getInstance()
        .getAnnotation(
          this.userMe.userId,
          this.userMe.shareAnnotationOn ? 'share' : 'video'
        )
        .stop();
    }
    this.$store.commit(mutations.RESET_USER_STORE);
    this.$store.commit(mutations.RESET_MEETING_STORE);
    this.$store.commit(mutations.RESET_WHITEBOARD_STORE);
    this.resetShareCtrlWindow();
    try {
      window.rtcEngine.dropCall('');
      window.rtcEngine.leaveChannel();
      window.rtcWhiteboard.leaveChannel();
    } catch (error) {
      console.log(error);
    }
    window.onpopstate = null;
    window.onbeforeunload = null;
  },
};
</script>

<style lang="less" scoped>
@playerLeftOffset: 0;
@playerWidth: 100%;

.pvc {
  position: fixed;
  width: @playerWidth;
  left: @playerLeftOffset;
  top: 0px;
  left: 0;
  bottom: 0;
  right: 0;
  background-color: #333;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  width: 100%;
  flex: 1;
  overflow: hidden;
  position: relative;
}
</style>
