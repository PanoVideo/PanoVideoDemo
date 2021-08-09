<template>
  <div>
    <Whiteboard v-if="whiteboardVisible" />
    <Call v-else-if="inCall" @leaveChannel="leaveChannel" />
    <Join v-else @join="joinChannel" />
    <Dialog ref="endDialog">
      <div class="dialog-content-text">会议倒计时结束，已离会</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">知道了</a>
      </div>
    </Dialog>
  </div>
</template>

<script>
import { mapMutations, mapState } from 'vuex';
import axios from 'axios';
import { RtcEngine, Constants } from '@pano.video/panortc';
import Join from '@/components/Join.vue';
import Call from '@/components/Call.vue';
import Whiteboard from '@/components/Whiteboard/index.vue';
import Dialog from '@/components/Dialog/index.vue';
import { formatTime } from './utils';

export default {
  name: 'App',
  components: {
    Join,
    Call,
    Whiteboard,
    Dialog,
  },
  data() {
    return {
      inCall: false,
      remainCountDown: null,
    };
  },
  computed: {
    ...mapState(['userList', 'whiteboardVisible']),
  },
  mounted() {
    this.initSDK();
    window.addEventListener('beforeunload', this.beforeUnloadCallback);
    window.addEventListener('hidepage', this.destroySDK);
  },
  destroyed() {
    this.destroySDK();
    window.removeEventListener('beforeunload', this.beforeUnloadCallback);
    window.removeEventListener('hidepage', this.destroySDK);
  },
  methods: {
    ...mapMutations([
      'updateUserStatus',
    ]),
    initSDK() {
      // 1. 创建SDK实例，可以传递appId，也可以在join传递，join时传递的会覆盖实例化时传递的
      window.panoSDK = new RtcEngine();
      const { panoSDK } = window;
      const { Events } = RtcEngine;
      // channel事件
      panoSDK.on(Events.joinChannelConfirm, (data) => {
        this.$store.commit('endJoinLoading');
        if (data.result !== 'success') {
          this.$toast.show('加入通话失败，请重试');
        } else {
          this.inCall = true;
        }
      });
      panoSDK.on(Events.channelCountDown, this.channelCountDownCallback);

      // audio 事件
      panoSDK.on(Events.userAudioStart, this.updateUserStatus);
      panoSDK.on(Events.userAudioStop, this.updateUserStatus);
      panoSDK.on(Events.userAudioMute, this.updateUserStatus);
      panoSDK.on(Events.userAudioUnmute, this.updateUserStatus);

      // video 事件
      panoSDK.on(Events.userVideoStart, (data) => {
        this.updateUserStatus(data);
        if (this.userList.length > 0 && this.userList[0].userId === data.userId) {
          panoSDK.subscribeVideo({
            userId: data.userId,
            quality: Constants.VideoProfileType.Standard,
          });
        }
      });
      panoSDK.on(Events.userVideoStop, (data) => {
        this.updateUserStatus({
          ...data,
          options: {
            videoTag: null,
          },
        });
      });
      panoSDK.on(Events.userVideoUnmute, this.updateUserStatus);
      panoSDK.on(Events.userVideoMute, this.updateUserStatus);

      panoSDK.on(Events.userVideoReceived, (e) => {
        const { data } = e;
        if (data.videoTag) {
          this.$store.commit('updateUserList', {
            userId: data.userId,
            videoTag: data.videoTag,
          });
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
        if (this.userList.some((user) => e.user.userId === user.userId)) {
          this.$store.commit('updateUserList', e.user);
        } else {
          this.$store.commit('addUser', e.user);
        }
      });
      panoSDK.on(Events.userLeave, (e) => {
        const firstUser = this.userList[0];
        if (firstUser && firstUser.userId === e.userId && ['open', 'unmute'].includes(firstUser.videoStatus)) {
          panoSDK.unsubscribeVideo({ userId: e.userId });
          const nextUser = this.userList[1];
          if (nextUser && ['open', 'unmute'].includes(nextUser.videoStatus)) {
            panoSDK.subscribeVideo({
              userId: nextUser.userId,
              quality: '16', // Constants.VideoProfileType.Standard,
            });
          }
        }
        this.$store.commit('removeUser', e);
      });

      // 白板
      panoSDK.on(Events.whiteboardAvailable, (data) => {
        this.$store.commit('setWhiteboardAvailable', ['type', 'message'].reduce((result, name) => {
          result[name] = data[name];
          return result;
        }, {}));
      });
    },
    destroySDK() {
      clearTimeout(this.remainCountDown);
      this.$store.commit('resetStore');
      if (this.inCall) {
        window.panoSDK.leaveChannel();
      }
      window.panoSDK.destroy();
    },
    beforeUnloadCallback(e) {
      if (this.inCall) {
        e.returnValue = '您还在会议中，确定离开？';
      }
    },
    joinChannel(info) {
      this.$store.commit('startJoinLoading');
      // 2.1 设置自己的appId
      const appId = '';
      // 2.1 加入会议需要传递userId，需要频道内唯一，这里为了演示，采用了随机生成
      const userId = `${Math.round(Math.random() * 100000)}`;
      // 2.2 获取token，需要开发接口，参考文档：https://developer.pano.video/restful/authtoken/
      const tokenUrl = '/rtc/get-token';
      axios.post(tokenUrl, {
        appId,
        channelId: `token=version:02;userId:${userId};channelId:${info.channelId}`,
        userId,
      }).then((res) => {
        window.panoSDK.joinChannel({
          appId,
          channelId: info.channelId,
          userId,
          token: res.data,
          userName: info.userName,
          channelMode: Constants.ChannelMode.TYPE_MEETING, // 会议模式
          subscribeAudioAll: true, // 自动订阅音频
        });
        this.$store.commit('updateUser', {
          ...info,
          userId,
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
          this.leaveChannel();
          return;
        }
        this.$store.commit('updateRemainTime', formatTime(remain));
        this.remainCountDown = setTimeout(countDownCallback, remain > 1000 ? 1000 : remain);
      };
      countDownCallback();
    },
    leaveChannel() {
      this.destroySDK();
      this.inCall = false;
      this.initSDK();
    },
  },
};
</script>

<style lang="less">
</style>
