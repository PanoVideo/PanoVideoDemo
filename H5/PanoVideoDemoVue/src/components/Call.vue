<template>
  <div>
    <div class="call-panel">
      <div class="call-header">
        <ul class="call-header-left">
          <li @click="switchCamera"></li>
          <li @click="openFeedbackDialog"></li>
        </ul>
        <div class="call-header-title">
          <strong>房间号：{{ user.channelId }}</strong>
          <span>{{ remainTime || "&nbsp;" }}</span>
        </div>
        <ul class="call-header-right">
          <li @click="openLeaveConfirm"></li>
        </ul>
      </div>
      <div class="call-body">
        <div class="main-video-view">
          <PvcVideo
            v-if="isOpen(mainUser.videoStatus) && mainUser.videoTag"
            :key="mainUser.userId"
            :videoTag="mainUser.videoTag"
          />
          <div v-else class="no-video"></div>
          <div class="user-bar">
            <span>{{ mainUser.userName || mainUser.userId }}</span>
            <em v-if="userList.length === 0">(Me)</em>
            <i :class="{ closed: !isOpen(mainUser.audioStatus) }"></i>
          </div>
        </div>
        <div v-if="userList.length > 0" class="small-video-view">
          <PvcVideo
            v-if="isOpen(user.videoStatus) && user.videoTag"
            :videoTag="user.videoTag"
          />
          <div v-else class="no-video"></div>
          <div class="user-bar">
            <span>{{ user.userName || user.userId }}</span>
            <em>(Me)</em>
            <i :class="{ closed: !isOpen(user.audioStatus) }"></i>
          </div>
        </div>
      </div>
      <ul class="call-footer">
        <li :class="{ closed: !isOpen(user.audioStatus) }" @click="switchAudio">
          <i></i>
          <span>{{ isOpen(user.audioStatus) ? "静音" : "开启音频" }}</span>
        </li>
        <li :class="{ closed: !isOpen(user.videoStatus) }" @click="switchVideo">
          <i></i>
          <span>{{ isOpen(user.videoStatus) ? "关闭视频" : "开启视频" }}</span>
        </li>
        <!-- 测试中
        <li @click="openWhiteboard">
          <i></i>
          <span>共享</span>
        </li>
        <li @click="openUserList">
          <i></i>
          <span>用户</span>
        </li>
        -->
      </ul>
    </div>
    <Dialog ref="leaveConfirm" width="70%">
      <div class="dialog-content-text">确定离开通话吗？</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">取消</a>
        <a class="confirm" href="javascript:;" @click="leave">确定</a>
      </div>
    </Dialog>
    <Dialog ref="feedbackDialog" width="80%">
      <h3>反馈与报障</h3>
      <div class="version">version: {{ version }}(web-sdk {{ sdkVersion }})</div>
      <ul class="field-wrapper">
        <li class="field-block">
          <label>问题描述</label>
          <div><textarea rows="4" placeholder="请输入(必填)，长度不超过200"
            v-model="feedbackInfo.description" maxlength="200" /></div>
        </li>
        <li class="field-block">
          <label>联系人/联系方式</label>
          <div><input placeholder="请输入"  v-model="feedbackInfo.contact" maxlength="100"/></div>
        </li>
        <li class="field-line">
          <label>上传日志</label>
          <div><input type="checkbox" v-model="feedbackInfo.uploadLogs" /></div>
        </li>
      </ul>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">取消</a>
        <a class="confirm" href="javascript:;" @click="feedback">提交</a>
      </div>
    </Dialog>
    <UserList v-if="userListVisible" />
  </div>
</template>

<script>
import { mapState } from 'vuex';
import UserList from '@/components/UserList.vue';
import pkg from '@/../package.json';
import PvcVideo from './PvcVideo.vue';
import Dialog from './Dialog/index.vue';

const openStatusList = ['open', 'unmute'];

export default {
  name: 'Call',
  components: {
    PvcVideo,
    Dialog,
    UserList,
  },
  computed: {
    ...mapState({
      mainUser(state) {
        return state.userList.length > 0 ? state.userList[0] : state.user;
      },
    }),
    ...mapState(['user', 'remainTime', 'userList', 'userListVisible']),
  },
  data() {
    return {
      version: pkg.version,
      sdkVersion: '',
      feedbackInfo: {
        description: '',
        contact: '',
        uploadLogs: true,
      },
    };
  },
  created() {
    this.sdkVersion = window.panoSDK.getSdkVersion();
  },
  mounted() {
    if (this.user.audioStatus === 'open') {
      window.panoSDK.startAudio();
    }
    if (this.user.videoStatus === 'open') {
      window.panoSDK.startVideo();
    }
  },
  methods: {
    isOpen(status) {
      return openStatusList.includes(status);
    },
    openLeaveConfirm() {
      this.$refs.leaveConfirm.open();
    },
    leave() {
      this.$refs.leaveConfirm.close();
      this.$emit('leaveChannel');
    },
    switchCamera() {
      window.panoSDK.getCams((cameraList) => {
        if (cameraList.length <= 1) {
          return;
        }
        let index = 0;
        if (cameraList[0].selected) {
          index = cameraList.length - 1;
        }
        window.panoSDK.selectCam(cameraList[index].deviceId);
      });
    },
    openFeedbackDialog() {
      this.$refs.feedbackDialog.open();
    },
    feedback() {
      if (!this.feedbackInfo.description) {
        this.$toast.show('请输入问题描述');
        return;
      }
      window.panoSDK.sendFeedback({
        type: 0,
        product: 'pvch5',
        ...this.feedbackInfo,
        extraInfo: '',
      });
      this.$refs.feedbackDialog.close();
    },
    switchAudio() {
      if (this.isOpen(this.user.audioStatus)) {
        this.$store.commit('updateUser', {
          audioStatus: 'mute',
        });
        window.panoSDK.muteMic();
      } else {
        // 视频开启后只切换麦克风
        if (this.user.audioStatus === 'mute') {
          this.$store.commit('updateUser', {
            audioStatus: 'unmute',
          });
          window.panoSDK.unmuteMic();
        } else {
          this.$store.commit('updateUser', {
            audioStatus: 'open',
          });
          window.panoSDK.startAudio();
        }
      }
    },
    switchVideo() {
      if (this.user.videoStatus === 'open') {
        this.$store.commit('updateUser', {
          videoStatus: 'close',
        });
        window.panoSDK.stopVideo();
      } else {
        this.$store.commit('updateUser', {
          videoStatus: 'open',
        });
        window.panoSDK.startVideo();
      }
    },
    openUserList() {
      this.$store.commit('openUserList');
    },
    openWhiteboard() {
      const { whiteboardAvailable } = this.$store.state;
      if (whiteboardAvailable === null) {
        this.$toast.show('白板不可用');
        return;
      }
      if (whiteboardAvailable.type !== 'join') {
        this.$toast.show(whiteboardAvailable.message);
        return;
      }
      this.$store.commit('openWhiteboard');
    },
  },
};
</script>

<style scoped lang="less">
.call-panel {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background-color: #000;
  color: #fff;
}
.call-header {
  height: 60px;
  position: relative;
  background-color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
}
.call-header-left,
.call-header-right {
  position: absolute;
  top: 0;
  height: 100%;
  display: flex;
  > li {
    width: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after {
      font-family: "pvc icon";
      font-size: 28px;
      line-height: 1;
    }
  }
}

.call-header-left {
  left: 0;
  padding-left: 10px;
  > li {
    &:nth-child(1)::after {
      content: "\e76c";
    }
    &:nth-child(2)::after {
      content: "\e610";
    }
  }
}

.call-header-right {
  right: 0;
  padding-right: 10px;
  > li {
    &:nth-child(1)::after {
      content: "\e770";
    }
  }
}

.call-header-title {
  text-align: center;
  > strong {
    font-weight: normal;
    display: block;
  }
  > span {
    display: block;
  }
}

.call-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}
.user-bar {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  line-height: 24px;
  > em {
    font-style: normal;
  }
  > i {
    font-style: normal;
    position: relative;
    &::before,
    &::after {
      font-family: "pvc icon";
      vertical-align: top;
      line-height: inherit;
    }
    &::before {
      content: "\e776";
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: "\e7c4";
    }
  }
}
.no-video::after {
  font-family: "pvc icon";
  line-height: 1;
  content: "\e76e";
}
.main-video-view, .small-video-view {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}
.main-video-view {
  flex: 1;
  > .no-video {
    font-size: 100px;
  }
  > .user-bar {
    padding: 0 6px 0 10px;
    background-color: rgba(51, 51, 51, 0.72);
    border-top-left-radius: 3px;
    line-height: 24px;
    > i {
      margin-left: 2px;
      font-size: 18px;
    }
  }
}
.small-video-view {
  position: absolute;
  right: 15px;
  top: 20px;
  width: 100px;
  height: 178px;
  border-radius: 3px;
  background-color: #333;
  overflow: hidden;
  > .no-video {
    font-size: 44px;
  }
  > .user-bar {
    left: 0;
    padding: 0 2px 0 6px;
    background-color: rgba(0, 0, 0, 0.56);
    justify-content: flex-end;
    font-size: 13px;
    line-height: 19px;
    > span {
      flex: 1;
      text-align: right;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    > i {
      margin-left: 1px;
      font-size: 17px;
    }
  }
}

.call-footer {
  height: 60px;
  display: flex;
  background-color: #333;
  > li {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    > i {
      font-style: normal;
      position: relative;
      &::before,
      &::after {
        font-family: "pvc icon";
        font-size: 26px;
        line-height: 1;
      }
      &::after {
        position: absolute;
        left: 0;
        top: 0;
      }
    }
    > span {
      font-size: 12px;
    }
    &:nth-child(1) > i::before {
      content: "\e776";
    }
    &:nth-child(2) > i::before {
      content: "\e774";
    }
    &:nth-child(3) > i::before {
      content: "\e772";
    }
    &:nth-child(4) > i::before {
      content: "\e79c";
    }
    &.closed > i::after {
      color: #d51c18;
      content: "\e7c4";
    }
  }
}
.version {
  margin-top: 5px;
  font-size: 12px;
  color: #999;
  text-align: center;
}
.field-wrapper {
  padding: 15px;
}
.field-block {
  margin-bottom: 15px;
  > label {
    display: block;
    margin-bottom: 5px;
  }
  > div {
    > textarea {
      resize: none;
      display: block;
      width: 100%;
      outline: none;
      padding: 7px 6px;
      border: 1px solid #dcdcdc;
      border-radius: 3px;
      font-size: inherit;
      line-height: 20px;
      color: inherit;
    }
    > input:not([type="checkbox"]) {
      display: block;
      width: 100%;
      height: 37px;
      padding: 0 6px;
      border: 1px solid #dcdcdc;
      border-radius: 3px;
    }
  }
}
.field-line {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  > label {
    flex: 1;
  }
}
</style>
