<template>
  <div class="login-wrapper">
    <div v-if="antiFraudTipShow" class="anti-fraud-tip">
      <label></label>
      <div>本产品仅用于拍乐云产品功能演示。<br />注意个人信息保密，谨防诈骗。</div>
      <i @click="closeAntiFraudTip"></i>
    </div>
    <h1>
      <img :src="'./logo.png'" alt="logo">
    </h1>
    <ul>
      <li class="fill-item">
        <label>房间号</label>
        <div :class="{ error: channelIdMsg }">
          <input placeholder="字母和数字组成，长度不超过20" v-model="joinInfo.channelId" maxlength="20" />
          <i v-if="channelIdMsg">{{ channelIdMsg }}</i>
        </div>
      </li>
      <li class="fill-item">
        <label>用户名</label>
        <div :class="{ error: userNameMsg }">
          <input placeholder="长度不超过20" v-model="joinInfo.userName" maxlength="20" />
          <i v-if="userNameMsg">{{ userNameMsg }}</i>
        </div>
      </li>
    </ul>
    <div class="switch-wrapper">
      <label><input type="checkbox" v-model="joinInfo.audioStatus" />开启音频</label>
      <label><template>
        <input v-if="iOS_15_1" type="checkbox" :checked="joinInfo.videoStatus"
          @click.prevent="toggleVideoStatus" />
        <input v-else type="checkbox" v-model="joinInfo.videoStatus" />
      </template>开启视频</label>
    </div>
    <button class="join-btn" @click="join">加入通话</button>
    <Dialog ref="availabilityDialog" width="70%">
      <div class="dialog-content-text">{{ iOS_15_1 ? 'iOS 15.1 不支持打开视频' : '当前浏览器可能不支持' }}</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">知道了</a>
      </div>
    </Dialog>
  </div>
</template>

<script>
/* eslint-disable camelcase */
import { iOS_15_1 } from '@/utils';
import Dialog from '../../components/Dialog/index.vue';

const joinInfoStorageName = 'PVC_JOIN_INFO';

export default {
  components: {
    Dialog,
  },
  name: 'Join',
  data() {
    return {
      antiFraudTipShow: true,
      joinInfo: {
        channelId: '',
        userName: '',
        audioStatus: null,
        videoStatus: null,
      },
      channelIdMsg: '',
      userNameMsg: '',
      iOS_15_1,
    };
  },
  created() {
    const storage = localStorage.getItem(joinInfoStorageName);
    if (!storage) {
      this.setJoinInfoDefault();
    } else {
      try {
        this.joinInfo = JSON.parse(storage);
      } catch (e) {
        this.setJoinInfoDefault();
      }
    }
    if (this.iOS_15_1) {
      this.joinInfo.videoStatus = false;
    }
    if (this.$route.query.channelId) {
      // 需要截取长度20，防止恶意输入
      this.joinInfo.channelId = this.$route.query.channelId.substring(0, 20);
    }
    this.$watch('joinInfo.channelId', this.validateChannelId);
    this.$watch('joinInfo.userName', this.validateUserName);
  },
  mounted() {
    if (this.iOS_15_1 || !window.panoSDK.checkEnvRequirement()) {
      this.$refs.availabilityDialog.open();
    }
  },
  methods: {
    closeAntiFraudTip() {
      this.antiFraudTipShow = false;
    },
    setJoinInfoDefault() {
      this.joinInfo = {
        channelId: `${Math.round(Math.random() * 100000)}`,
        userName: '',
        audioStatus: true,
        videoStatus: true,
      };
    },
    validateChannelId() {
      const value = this.joinInfo.channelId;
      if (!value) {
        this.channelIdMsg = '请输入房间号';
      } else if (!/^[0-9A-Za-z]+$/.test(value)) {
        this.channelIdMsg = '请输入字母和数字组成的房间号';
      } else if (value.length > 20) {
        this.channelIdMsg = '房间号长度不超过20';
      } else {
        this.channelIdMsg = '';
      }
    },
    validateUserName() {
      const value = this.joinInfo.userName;
      if (!value) {
        this.userNameMsg = '请输入用户名';
      } else if (value.length > 20) {
        this.userNameMsg = '用户名长度不超过20';
      } else {
        this.userNameMsg = '';
      }
    },
    toggleVideoStatus() {
      this.$toast('iOS 15.1 不支持打开视频');
    },
    join() {
      if (this.channelIdMsg || this.userNameMsg) {
        return;
      }
      this.validateChannelId();
      this.validateUserName();
      if (this.channelIdMsg || this.userNameMsg) {
        return;
      }
      localStorage.setItem(joinInfoStorageName, JSON.stringify(this.joinInfo));

      this.$store.commit('updateUser', {
        ...this.joinInfo,
        audioStatus: this.joinInfo.audioStatus ? 'open' : 'close',
        videoStatus: this.joinInfo.videoStatus ? 'open' : 'close',
      });
      this.$router.replace({
        path: 'call',
        query: {
          channelId: this.joinInfo.channelId,
        },
      });
    },
  },
};
</script>

<style scoped lang="less">
@base-path: "../../";
@import "@{base-path}less/variables.less";

.login-wrapper {
  padding: 15px 15px 0;
  position: relative;
}

.anti-fraud-tip {
  position: absolute;
  left: 15px;
  right: 15px;
  top: 82px;
  border-radius: 3px;
  background: #fff;
  box-shadow: 0px 0px 10px 0px rgba(169, 169, 169, 0.3);
  opacity: 0.96;
  padding: 0 0 0 8px;
  display: flex;
  align-items: flex-start;
  > label {
    padding: 6px 6px 0 0;
    line-height: 1;
    &::before {
      content: '\e834';
      font-family: "pvc icon";
      font-size: 16px;
      color: #EF1D1D;
    }
  }
  > div {
    flex: 1;
    padding: 5px 0;
    font-size: 13px;
    line-height: 19px;
  }
  > i {
    padding: 7px;
    font-style: normal;
    line-height: 1;
    &::before {
      content: '\e833';
      font-family: "pvc icon";
      font-size: 16px;
      color: #666;
    }
  }
}

h1 {
  margin: 0;
  text-align: center;
  > img {
    vertical-align: top;
    width: 240px;
    margin: 17px 0 60px;
  }
}

.fill-item {
  margin-bottom: 18px;
  > label {
    line-height: 32px;
    color: rgba(0, 0, 0, 0.85);
    &::before {
      content: '*';
      margin-right: 4px;
      line-height: 1;
      color: #ff4d4f;
    }
  }
  > div {
    background-color: #fff;
    height: 40px;
    border: 1px solid #d9d9d9;
    border-radius: 3px;
    position: relative;
    &::before {
      position: absolute;
      left: 6px;
      top: 50%;
      font-family: "pvc icon";
      font-size: 24px;
      line-height: 1;
      transform: translate3d(0, -50%, 0);
      color: #333;
    }
    > input {
      display: block;
      position: relative;
      padding: 0 12px 0 36px;
      width: 100%;
      height: 100%;
    }
    &.error {
      border-color: #ff4d4f;
      &::before {
        color: #ff4d4f;
      }
      > i {
        font-style: normal;
        line-height: 24px;
        color: #ff4d4f;
      }
    }
  }
  &:nth-child(1) > div::before {
    content: "\e759";
  }
  &:nth-child(2) > div::before {
    content: "\e76e";
  }
}

.switch-wrapper {
  display: flex;
  margin-top: 22px;
  > label {
    margin-right: 15px;
    line-height: 32px;
    display: flex;
    align-items: center;
    &:last-child {
      margin-right: 0;
    }
    > input {
      margin-right: 5px;
    }
  }
}

.join-btn {
  -webkit-appearance: none;
  display: block;
  width: 100%;
  height: 40px;
  margin-top: 10px;
  background: @primary-color;
  border: 1px solid @primary-color;
  border-radius: 3px;
  text-shadow: 0 -1px 0 rgba(0, 0, 0, .12);
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.05);
  font-size: 16px;
  line-height: 1;
  font-family: inherit;
  color: #fff;
  @loadingWidth: 7px;
  &.loading::before {
    content: "";
    display: inline-block;
    width: @loadingWidth;
    height: @loadingWidth;
    border-top: 1px solid #fff;
    border-right: 1px solid #fff;
    border-top-right-radius: 10px;
    margin: 0 12px @loadingWidth + 1px 0;
    vertical-align: top;
    line-height: 0;
    transform-origin: left bottom;
    animation: loadingRotate 0.86s infinite linear;
  }
}
</style>
