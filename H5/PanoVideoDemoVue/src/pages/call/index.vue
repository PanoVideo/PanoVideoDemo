<template>
  <div>
    <Whiteboard v-if="whiteboardVisible" />
    <Call v-else @leaveChannel="goToLogin" />
    <Dialog ref="endDialog" width="70%">
      <div class="dialog-content-text">会议倒计时结束，已离会</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;" @click="goToLogin">知道了</a>
      </div>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapMutations, mapState } from 'vuex';
import Call from '@/components/Call.vue';
import Whiteboard from '@/components/Whiteboard/index.vue';
import Dialog from '@/components/Dialog/index.vue';
import {
  RtcEngine, Constants, RtcWhiteboard, RtcMessage,
} from '@pano.video/panortc';
import axios from 'axios';
import { PROP_HOST_ID } from '@/vars';
import { setHostId } from '@/utils/sdk';
import { formatTime, isOpen } from '../../utils';
import { genToken } from '../../utils/genToken';
import { Logger } from '../../utils/logger';

const logger = new Logger('call');

export default {
  name: 'App',
  components: {
    Call,
    Whiteboard,
    Dialog,
  },
  data() {
    return {
      remainCountDown: null,
    };
  },
  computed: {
    ...mapState(['whiteboardVisible', 'user', 'userMap', 'hostId']),
    ...mapGetters(['screenUserId', 'videoSubMap']),
  },
  watch: {
    screenUserId(val, oldVal) {
      if (oldVal) {
        this.unsubscribeScreen(oldVal);
      }
      if (val) {
        this.subscribeScreen(val);
      } else {
        this.$store.commit('updateScreenVideo', null);
      }
    },
    videoSubMap(val, oldVal) {
      Object.keys(oldVal).forEach((userId) => {
        if (val[userId] === undefined) {
          this.unsubscribeVideo(userId);
        }
      });
      Object.keys(val).forEach((userId) => {
        if (oldVal[userId] === undefined || oldVal[userId] !== val[userId]) {
          this.subscribeVideo(userId, val[userId]);
        }
      });
    },
    hostId(val) {
      if (!val) {
        return;
      }
      if (val === this.user.userId) {
        window.panoSDK.getWhiteboard().setRoleType(RtcWhiteboard.WBRoleType.Admin);
        this.$toast('您已经成为主持人');
      } else {
        const user = this.userMap[val];
        if (user) {
          this.$toast(`当前主持人是${user.userName || user.userId}`);
        }
      }
    },
  },
  mounted() {
    logger.info('component call mounted');
    window.addEventListener('beforeunload', this.beforeUnloadCallback);
    // 主动调离会，否者要服务端判断是不是已经离会
    window.addEventListener('hidepage', this.destroySDK);

    if (!this.user.channelId || !this.user.userName) {
      this.goToLogin();
      return;
    }
    this.initAndJoin();
  },
  beforeDestroy() {
    logger.info('component call destroyed');

    this.destroySDK();
    window.removeEventListener('beforeunload', this.beforeUnloadCallback);
    window.removeEventListener('hidepage', this.destroySDK);
  },
  methods: {
    ...mapMutations([
      'updateUserStatus',
    ]),
    initAndJoin() {
      this.initSDK();
      this.genTokenJoinChannel(this.user);
      // 服务端生成token
      // this.requestTokenJoinChannel(this.user);
    },
    initSDK() {
      const { panoSDK } = window;
      const { Events } = RtcEngine;
      RtcMessage.getInstance().on(
        RtcMessage.Events.propertyChanged,
        (type, propName, propValue) => {
          if (propName === PROP_HOST_ID) {
            this.$store.commit('setHostId', propValue.id);
          }
        },
      );
      RtcMessage.getInstance().on(RtcMessage.Events.sessionReady, () => {
        setTimeout(() => {
          if (!this.hostId) {
            setHostId(this.user.userId);
          }
        }, 1000);
      });
      panoSDK.on(Events.networkStatus, (data) => {
        this.$store.commit('updateNetworkStatusMap', data.networkRatings);
      });
      // channel事件
      panoSDK.on(Events.joinChannelConfirm, (data) => {
        this.$store.commit('endJoinLoading');
        if (data.result !== 'success') {
          this.$toast('加入通话失败，请重试');
          this.goToLogin();
        } else {
          const { user } = this.$store.state;
          if (user.audioStatus === 'open') {
            panoSDK.startAudio();
          }
          if (user.videoStatus === 'open') {
            panoSDK.startVideo(this.$store.state.setting.videoProfile);
          }
          // 微信中无交互音频不能播放，必须是join之后才有audio元素可以播放
          window.panoSDK.playAudio();
        }
      });
      panoSDK.on(Events.channelCountDown, this.channelCountDownCallback);
      panoSDK.on(Events.channelFailover, this.onFailoverNotify);

      // audio 事件
      panoSDK.on(Events.userAudioStart, this.updateUserStatus);
      panoSDK.on(Events.userAudioStop, this.updateUserStatus);
      panoSDK.on(Events.userAudioMute, this.updateUserStatus);
      panoSDK.on(Events.userAudioUnmute, this.updateUserStatus);

      this.setAudioIndication();

      // video 事件
      panoSDK.on(Events.userVideoStart, (data) => {
        this.updateUserStatus(data);
      });
      panoSDK.on(Events.userVideoStop, (data) => {
        this.updateUserStatus({
          ...data,
        });
        this.$store.commit('removeUserVideo', data.userId);
      });
      panoSDK.on(Events.userVideoUnmute, this.updateUserStatus);
      panoSDK.on(Events.userVideoMute, this.updateUserStatus);

      panoSDK.on(Events.userVideoReceived, (e) => {
        const { data } = e;
        if (data.videoTag) {
          this.$store.commit('addUserVideo', {
            userId: data.userId,
            videoTag: data.videoTag,
          });
        }
      });

      // screen 事件
      panoSDK.on(Events.userScreenStart, (e) => {
        this.$store.commit('addScreenUserId', e.userId);
      });
      panoSDK.on(Events.userScreenStop, (e) => {
        this.$store.commit('removeScreenUserId', e.userId);
      });

      panoSDK.on(Events.userScreenReceived, (e) => {
        if (e.data) {
          this.$store.commit('updateScreenVideo', e.data.videoTag);
        }
      });

      // video 事件
      panoSDK.on(Events.getLocalVideo, (e) => {
        if (e.data.videoTag) {
          // videoTag
          this.$store.commit('updateUser', {
            videoTag: e.data.videoTag,
          });
        }
      });

      panoSDK.on(Events.userJoin, (e) => {
        this.$store.commit('addUser', e.user);
      });
      panoSDK.on(Events.userLeave, (e) => {
        this.$store.commit('removeUser', e);
      });

      // 白板
      panoSDK.on(Events.whiteboardAvailable, (data) => {
        this.$store.commit('setWhiteboardAvailable', ['type', 'message'].reduce((result, name) => {
          result[name] = data[name];
          return result;
        }, {}));
        const rtcWhiteboard = panoSDK.getWhiteboard();

        rtcWhiteboard.enableCursorSync();

        rtcWhiteboard.on(RtcWhiteboard.Events.openStateChanged, () => {
          this.$store.commit('openWhiteboard');
        });

        rtcWhiteboard.on(RtcWhiteboard.Events.userVisionShareStart, () => {
          rtcWhiteboard.startFollowVision();
        });
      });
    },
    destroySDK() {
      clearTimeout(this.remainCountDown);
      this.$store.commit('resetStore');
      window.panoSDK.leaveChannel();
      // 离开后销毁重建，因为iOS微信中离会后设备列表会变
      window.panoSDK.destroy();
      window.panoSDK = new RtcEngine();
    },
    beforeUnloadCallback(e) {
      e.returnValue = '您还在会议中，确定离开？';
    },
    genTokenJoinChannel(info) {
      this.$store.commit('startJoinLoading');
      // 2.1 设置自己的appId
      const appId = '';
      // 2.1 加入会议需要传递userId，需要频道内唯一，这里为了演示，采用了随机生成
      const userId = `${Math.round(Math.random() * 100000)}`;
      // 2.2 生成token
      const appSecret = '';
      const token = genToken(appId, appSecret, info.channelId, userId);
      window.panoSDK.joinChannel({
        appId,
        channelId: info.channelId,
        userId,
        token,
        userName: info.userName,
        channelMode: Constants.ChannelMode.TYPE_MEETING, // 会议模式
        subscribeAudioAll: true, // 自动订阅音频
      });
      this.$store.commit('updateUser', {
        ...info,
        userId,
      });
    },
    requestTokenJoinChannel(info) {
      this.$store.commit('startJoinLoading');
      // 2.1 设置自己的appId
      const appId = '';
      // 2.1 加入会议需要传递userId，需要频道内唯一，这里为了演示，采用了随机生成
      const userId = `${Math.round(Math.random() * 100000)}`;
      // 2.2 获取token，需要开发接口，参考文档：https://developer.pano.video/restful/authtoken/
      axios.post('/get-token', {
        appId,
        channelId: info.channelId,
        userId,
      }).then((res) => {
        window.panoSDK.joinChannel({
          appId,
          channelId: info.channelId,
          userId,
          token: res.data.token,
          userName: info.userName,
          channelMode: Constants.ChannelMode.TYPE_MEETING, // 会议模式
          subscribeAudioAll: true, // 自动订阅音频
        });
        this.$store.commit('updateUser', {
          ...info,
          userId,
          features: res.data.features || {},
        });
      });
    },
    channelCountDownCallback(data) {
      clearTimeout(this.remainCountDown);
      const end = Date.now() + data.remainsec * 1000;
      const countDownCallback = () => {
        const remain = end - Date.now();
        if (remain <= 0) {
          // 结束了
          this.$refs.endDialog.open();
          window.panoSDK.leaveChannel();
          return;
        }
        this.$store.commit('updateRemainTime', formatTime(remain));
        this.remainCountDown = setTimeout(countDownCallback, remain > 1000 ? 1000 : remain);
      };
      countDownCallback();
    },
    onFailoverNotify(event) {
      if (event.state === 'Success') {
        this.$toast({ message: '重连成功', duration: 3000 });
      } else if (event.state === 'Reconnecting') {
        this.$toast({ message: '连接已经断开， 正在重连...', duration: 3000 });
      } else if (event.state === 'Failed') {
        this.$toast({ message: '连接已断开， 重连失败', duration: 0 });
      }
    },
    setAudioIndication() {
      const ms = 200;
      let lowRecordingCount = 0;
      window.panoSDK.setRecordingAudioIndication((audioLevel) => {
        if (audioLevel.userId !== this.user.userId) {
          return;
        }
        if (isOpen(this.user.audioStatus) && audioLevel.active && audioLevel.level < 0.00001) {
          lowRecordingCount++;
        } else {
          lowRecordingCount = 0;
        }
        if (lowRecordingCount === Math.floor(10000 / ms)) {
          this.$toast('检测到您的音频输入音量持续较低，如果您正在讲话，请检查您的麦克风是否正常');
        }
        // 更新user音量
        this.$store.commit('updateUserAudioLevel', audioLevel);
      }, ms);
      window.panoSDK.setUserAudioIndication((audioLevelList) => {
        this.$store.commit('updateUserAudioLevel', audioLevelList);
      }, ms);
    },
    subscribeVideo(userId, quality) {
      logger.info(`subscribeVideo ${userId} ${quality}`);
      window.panoSDK.subscribeVideo({
        userId,
        quality,
      });
    },
    unsubscribeVideo(userId) {
      logger.info(`unSubscribeVideo ${userId}`);
      window.panoSDK.unsubscribeVideo({
        userId,
      });
    },
    subscribeScreen(userId) {
      logger.info(`subscribeScreen ${userId}`);
      window.panoSDK.subscribeScreen({
        userId,
      });
    },
    unsubscribeScreen(userId) {
      logger.info(`unsubscribeScreen ${userId}`);
      window.panoSDK.unsubscribeScreen({
        userId,
      });
    },
    goToLogin() {
      this.$router.replace({
        path: '/',
        query: {
          channelId: this.$route.query.channelId,
        },
      });
    },
  },
};
</script>
