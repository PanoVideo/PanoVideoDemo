<template>
  <div class="setting-panel">
    <div class="setting-header">
      <h2>设置</h2>
      <div class="back" @click="back"></div>
    </div>
    <ul class="setting-list">
      <li>
        <label>单次通话时长</label>
        <div>90分钟</div>
      </li>
      <li>
        <label>通话人数上限</label>
        <div>25人</div>
      </li>
    </ul>
    <ul class="setting-list">
      <li>
        <label>我的分辨率</label>
        <div>
          <ul class="video-profile">
            <li v-for="profile in videoProfileList" :key="profile.value"
              :class="{active: profile.value === setting.videoProfile}"
              @click="changeVideoProfile(profile.value)">{{ profile.text }}</li>
          </ul>
        </div>
      </li>
    </ul>
    <ul class="setting-list">
      <li @click="openFeedback">
        <label>反馈与报障</label>
        <div class="enter-arrow"></div>
      </li>
      <li>
        <label>版本</label>
        <div>{{ version }}(web-sdk {{ sdkVersion }})</div>
      </li>
    </ul>
    <Feedback ref="feedback" />
  </div>
</template>

<script>
import { mapState } from 'vuex';
import { Constants } from '@pano.video/panortc';
import pkg from '@/../package.json';
import Feedback from './Feedback.vue';

const { VideoProfileType } = Constants;

export default {
  name: 'Setting',
  components: {
    Feedback,
  },
  data() {
    return {
      videoProfileList: [
        { value: VideoProfileType.Low, text: '180P' },
        { value: VideoProfileType.Standard, text: '360P' },
        { value: VideoProfileType.HD720P, text: '720P' },
      ],
      version: pkg.version,
      sdkVersion: '',
    };
  },
  computed: mapState(['setting']),
  created() {
    this.sdkVersion = window.panoSDK.getSdkVersion();
  },
  methods: {
    back() {
      this.$store.commit('closeSetting');
    },
    changeVideoProfile(videoProfile) {
      if (this.setting.videoProfile !== videoProfile) {
        this.$store.commit('updateSetting', {
          videoProfile,
        });
        const { panoSDK } = window;
        panoSDK.stopVideo();
        /**
         * NOTE
         * 虽然videostop是同步方法，但是实际观察到
         * stopVideo和startVideo之间还有一段重叠时间
         * ios导致重新打开的video是黑屏
         */
        setTimeout(() => {
          panoSDK.startVideo(videoProfile);
          this.$store.commit('updateUser', {
            videoStatus: 'open',
          });
        }, 100);
      }
    },
    openFeedback() {
      this.$refs.feedback.open();
    },
  },
};
</script>

<style lang="less" scoped>
@import url("../less/variables.less");

.setting-panel {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: #F5F7FF;
  z-index: 99;
}

.setting-header {
  line-height: 48px;
  background-color: #fff;
  position: relative;
  > h2 {
    margin: 0;
    font-size: 18px;
    font-weight: normal;
    text-align: center;
  }
  > .back {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 48px;
    text-align: center;
    &::after {
      content: "\e75b";
      font-family: "pvc icon";
      font-size: 24px;
    }
  }
}

.setting-list {
  margin-top: 10px;
  background-color: #fff;
  padding-left: 15px;
  > li {
    border-bottom: 0.5px solid #ddd;
    padding-right: 15px;
    display: flex;
    align-items: center;
    min-height: 44px;
    &:last-child {
      border-bottom: none;
    }
    > div {
      flex: 1;
      display: flex;
      justify-content: flex-end;
      color: #999;
    }
  }
}

.video-profile {
  display: flex;
  border: 0.5px solid @primary-color;
  border-radius: 2px;
  color: @primary-color;
  line-height: 24px;
  > li {
    width: 50px;
    text-align: center;
    &.active {
      background-color: @primary-color;
      color: #fff;
    }
  }
}

.enter-arrow::after {
  content: "\e797";
  font-family: "pvc icon";
  font-size: 20px;
}
</style>
