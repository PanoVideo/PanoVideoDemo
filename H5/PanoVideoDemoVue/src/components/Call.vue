<template>
  <div>
    <div class="call-panel">
      <div class="call-header">
        <ul class="call-header-left">
          <li @click="switchCamera"></li>
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
          <PvcVideo :videoTag="mainUser.videoTag" alias="mainVideo"
            :shouldShow="!!mainUser.videoTag"
            :mirror="userIdList.length === 0" />
          <div v-show="!mainUser.videoTag" class="no-video"></div>
          <div class="user-bar">
            <span>{{ mainUser.userName || mainUser.userId }}</span>
            <em v-if="userIdList.length === 0">(Me)</em>
            <i v-if="isOpen(mainUser.audioStatus)">
              <i>
                <i>
                  <i :style="{
                    height: audioLevelMap[mainUser.userId] !== undefined
                      ? `${audioLevelMap[mainUser.userId] * 1000}%` : '0',
                  }"></i>
                </i>
              </i>
            </i>
            <i v-else class="closed"></i>
            <strong :class="networkStatusMap[mainUser.userId] || 'good'"></strong>
          </div>
        </div>
        <SmallVideoView :shouldShow="userIdList.length > 0"
          :user="userScreen ? activeUser : user" :isMe="userScreen ? false : true" />
      </div>
      <ul class="call-footer">
        <li :class="{ closed: !isOpen(user.audioStatus) }" @click="switchAudio">
          <i>
            <i v-if="isOpen(user.audioStatus)">
              <i>
                <i :style="{
                  height: audioLevelMap[user.userId] !== undefined
                    ? `${audioLevelMap[user.userId] * 1000}%` : '0',
                }"></i>
              </i>
            </i>
          </i>
          <span>{{ isOpen(user.audioStatus) ? "静音" : "开启音频" }}</span>
        </li>
        <li :class="{ closed: !isOpen(user.videoStatus) }" @click="switchVideo">
          <i></i>
          <span>{{ isOpen(user.videoStatus) ? "关闭视频" : "开启视频" }}</span>
        </li>
        <li @click="openWhiteboard">
          <i></i>
          <span>打开白板</span>
        </li>
        <li @click="openUserList">
          <i></i>
          <span>用户</span>
        </li>
        <li @click="openBottomMore">
          <i></i>
          <span>更多</span>
        </li>
      </ul>
    </div>
    <ActionSheet v-model="bottomMoreShow" :actions="bottomMoreActionList" cancel-text="取消"
      @select="bottomMoreActionSelect" @cancel="closeBottomMore" />
    <Dialog ref="leaveConfirm" width="70%">
      <div class="dialog-content-text">确定离开通话吗？</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">取消</a>
        <a class="confirm" href="javascript:;" @click="leave">确定</a>
      </div>
    </Dialog>
    <Setting v-if="settingVisible" />
    <UserList v-if="userListVisible" />
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import ActionSheet from 'vant/lib/action-sheet';
import 'vant/lib/action-sheet/style';
/* eslint-disable camelcase */
import { isOpen, iOS_15_1 } from '@/utils';
import UserList from '@/components/UserList.vue';
import Setting from '@/components/Setting.vue';
import PvcVideo from './PvcVideo.vue';
import Dialog from './Dialog/index.vue';
import SmallVideoView from './SmallVideoView.vue';

export default {
  name: 'Call',
  components: {
    PvcVideo,
    Dialog,
    ActionSheet,
    UserList,
    Setting,
    SmallVideoView,
  },
  data() {
    return {
      bottomMoreShow: false,
      bottomMoreActionList: [
        { name: '设置' },
      ],
    };
  },
  computed: {
    mainUser() {
      return this.$store.getters.userScreen || this.$store.getters.activeUser ||
        this.$store.state.user;
    },
    ...mapState(['user', 'audioLevelMap', 'remainTime', 'userIdList', 'settingVisible', 'userListVisible', 'networkStatusMap']),
    ...mapGetters(['userScreen', 'activeUser']),
  },
  methods: {
    isOpen,
    openLeaveConfirm() {
      this.$refs.leaveConfirm.open();
    },
    leave() {
      this.$refs.leaveConfirm.close();
      this.$emit('leaveChannel');
    },
    switchCamera() {
      // 切换摄像头比较耗资源，增加一下节流，提升性能
      if (this.cameraSwitching) {
        return;
      }
      this.cameraSwitching = true;
      setTimeout(() => {
        this.cameraSwitching = false;
      }, 2000);
      window.panoSDK.getCams((cameraList) => {
        if (cameraList.length <= 1) {
          return;
        }
        let index = 0;
        if (cameraList[0].selected) {
          index = cameraList.length - 1;
        }
        this.$toast(cameraList[index].label || '正在切换摄像头');
        window.panoSDK.selectCam(cameraList[index].deviceId);
      });
    },
    openSetting() {
      this.$store.commit('openSetting');
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
      if (iOS_15_1) {
        this.$toast('iOS 15.1 不支持打开视频');
        return;
      }
      if (this.user.videoStatus === 'open') {
        this.$store.commit('updateUser', {
          videoStatus: 'close',
          videoTag: null,
        });
        window.panoSDK.stopVideo();
      } else {
        this.$store.commit('updateUser', {
          videoStatus: 'open',
        });
        window.panoSDK.startVideo(this.$store.state.setting.videoProfile);
      }
    },
    openUserList() {
      this.$store.commit('openUserList');
    },
    openWhiteboard() {
      const { whiteboardAvailable } = this.$store.state;
      if (whiteboardAvailable === null) {
        this.$toast('白板不可用');
        return;
      }
      if (whiteboardAvailable.type !== 'join') {
        this.$toast(whiteboardAvailable.message);
        return;
      }
      this.$store.commit('openWhiteboard');
    },
    openBottomMore() {
      this.bottomMoreShow = true;
    },
    closeBottomMore() {
      this.bottomMoreShow = false;
    },
    bottomMoreActionSelect(action) {
      this.bottomMoreShow = false;
      if (action.name === '设置') {
        this.openSetting();
      }
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
  bottom: constant(safe-area-inset-bottom);
  bottom: env(safe-area-inset-bottom);
  &::after {
    content: "";
    position: absolute;
    left: 0;
    right: 0;
    top: 100%;
    height: 0;
    height: constant(safe-area-inset-bottom);
    height: env(safe-area-inset-bottom);
    background-color: #333;
  }
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
    > i {
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      > i {
        width: 0.25em;
        height: 54 / 128 * 1em;
        border-radius: 0.125em;
        margin-bottom: 26 / 128 * 1em;
        position: relative;
        overflow: hidden;
        > i {
          position: absolute;
          left: 0;
          right: 0;
          bottom: 0;
          background-color: #0f0;
        }
      }
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: "\e7c4";
    }
  }
  > strong {
    font-weight: normal;
    position: relative;
    &::before,
    &::after {
      font-family: "pvc icon";
      vertical-align: top;
      line-height: inherit;
    }
    &::before {
      content: "\e81d";
    }
    &::after {
      position: absolute;
      left: 0;
      top: 0;
    }
    &.good::after {
      content: "\e81e";
      color: rgb(52, 199, 88);
    }
    &.poor::after {
      content: "\e820";
      color: rgb(255, 163, 16);;
    }
    &.bad::after {
      content: "\e821";
      color: rgb(247, 67, 64);;
    }
  }
}
.no-video::after {
  font-family: "pvc icon";
  line-height: 1;
  content: "\e76e";
}
.main-video-view {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
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
      font-size: 26px;
      font-style: normal;
      position: relative;
      &::before,
      &::after {
        font-family: "pvc icon";
        line-height: 1;
      }
      &::after {
        position: absolute;
        left: 0;
        top: 0;
      }
    }
    &:nth-child(1) > i {
      > i {
        position: absolute;
        left: 0;
        right: 0;
        top: 0;
        bottom: 0;
        display: flex;
        justify-content: center;
        align-items: center;
        > i {
          width: 0.25em;
          height: 54 / 128 * 1em;
          border-radius: 0.125em;
          margin-bottom: 26 / 128 * 1em;
          position: relative;
          overflow: hidden;
          > i {
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #0f0;
          }
        }
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
    &:nth-child(5) > i::before {
      content: "\e76f";
    }
    &.closed > i::after {
      color: #d51c18;
      content: "\e7c4";
    }
  }
}
</style>
