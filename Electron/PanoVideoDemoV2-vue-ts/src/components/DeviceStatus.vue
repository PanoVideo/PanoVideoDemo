<template>
  <div class="web-preview">
    <div class="page-header">
      <a
        href="https://www.pano.video"
        target="_blank"
        :style="{ opacity: 1 }"
        rel="noreferrer"
      >
        <img alt="logo" src="../assets/img/logo.png" />
      </a>
    </div>
    <div class="device-container">
      <div class="video-wrapper">
        <div
          ref="view"
          :class="
            videoOpen
              ? 'video-mirror'
              : 'video-not-open iconfont icon-video-off'
          "
        ></div>
        <span v-if="notAllowedDeviceText"
          >请在浏览器设置中开启{{ notAllowedDeviceText }}权限</span
        >
        <ul>
          <li>
            <a-dropdown placement="topLeft" transitionName="">
              <div class="device-item microphone">
                <i class="iconfont icon-audio-outlined" />
                <span>{{ micMap[recordDeviceId] || '请选择麦克风' }}</span>
                <i class="iconfont icon-downArrow" />
              </div>
              <div slot="overlay" class="device-item-list">
                <ul v-if="micAllowed === false">
                  <li>请打开麦克风权限</li>
                </ul>
                <template v-else>
                  <label class="mic">
                    <i class="iconfont icon-audio-outlined" />
                    麦克风
                  </label>
                  <ul>
                    <li
                      v-for="mic in micList"
                      :key="mic.deviceId"
                      :class="{
                        selected: mic.deviceId === recordDeviceId,
                      }"
                      @click="selectMic(mic.deviceId)"
                    >
                      {{ mic.label }}
                    </li>
                  </ul>
                </template>
              </div>
            </a-dropdown>
          </li>
          <li>
            <a-dropdown placement="topLeft" transitionName="">
              <div class="device-item speaker">
                <i class="iconfont icon-speaker-outlined" />
                <span>{{ speakerMap[playoutDeviceId] || '请选择扬声器' }}</span>
                <i class="iconfont icon-downArrow" />
              </div>
              <div slot="overlay" class="device-item-list">
                <ul v-if="micAllowed === false">
                  <li>请打开麦克风权限</li>
                </ul>
                <template v-else>
                  <label class="speaker">
                    <i class="iconfont icon-speaker-outlined" />
                    扬声器
                  </label>
                  <ul v-if="speakerList.length === 0 && !isChrome">
                    <li>不支持扬声器选择</li>
                  </ul>
                  <ul v-else>
                    <li
                      v-for="speaker in speakerList"
                      :key="speaker.deviceId"
                      :class="{
                        selected: speaker.deviceId === playoutDeviceId,
                      }"
                      @click="selectSpeaker(speaker.deviceId)"
                    >
                      {{ speaker.label }}
                    </li>
                  </ul>
                </template>
              </div>
            </a-dropdown>
          </li>
          <li>
            <a-dropdown placement="topLeft" transitionName="">
              <div class="device-item camera">
                <i class="iconfont icon-video-outlined" />
                <span>{{ cameraMap[captureDeviceId] || '请选择摄像头' }}</span>
                <i class="iconfont icon-downArrow" />
              </div>
              <div slot="overlay" class="device-item-list">
                <ul v-if="cameraAllowed === false">
                  <li>请打开摄像头权限</li>
                </ul>
                <template v-else>
                  <label class="camera">
                    <i class="iconfont icon-video-outlined" />
                    摄像头
                  </label>
                  <ul>
                    <li
                      v-for="camera in cameraList"
                      :key="camera.deviceId"
                      :class="{
                        selected: camera.deviceId === captureDeviceId,
                      }"
                      @click="selectCamera(camera.deviceId)"
                    >
                      {{ camera.label }}
                    </li>
                  </ul>
                </template>
              </div>
            </a-dropdown>
          </li>
        </ul>
      </div>
      <div class="device-wrapper">
        <div
          :class="[
            'device-status',
            'audio-status',
            {
              'not-allowed': micAllowed === false,
              closed: !audioOpen,
            },
          ]"
        >
          <div @click="toggleAudio">
            <div class="device-icon-wrapper">
              <AudioLevel
                :audioMuted="!audioOpen"
                :level="micAudioLevel"
                :fontSize="22"
              />
              <i v-if="!micAllowed" class="iconfont icon-important" />
            </div>
            <span>
              {{ audioOpen ? '静音' : '取消静音' }}
            </span>
          </div>
          <a-dropdown placement="topRight" transitionName="">
            <i class="iconfont icon-downArrow" />
            <div
              slot="overlay"
              :class="[
                'device-status-list',
                { 'not-allowed': micAllowed === false },
              ]"
            >
              <ul v-if="micAllowed === false">
                <li>请打开麦克风权限</li>
              </ul>
              <template v-else>
                <label class="mic">
                  <i class="iconfont icon-audio-outlined" />
                  麦克风
                </label>
                <ul>
                  <li
                    v-for="mic in micList"
                    :key="mic.deviceId"
                    :class="{ selected: mic.deviceId === recordDeviceId }"
                    @click="selectMic(mic.deviceId)"
                  >
                    {{ mic.label }}
                  </li>
                </ul>
                <i></i>
                <label class="speaker">
                  <i class="iconfont icon-speaker-outlined" />
                  扬声器
                </label>
                <ul v-if="speakerList.length === 0 && !isChrome">
                  <li>不支持扬声器选择</li>
                </ul>
                <ul v-else>
                  <li
                    v-for="speaker in speakerList"
                    :key="speaker.deviceId"
                    :class="{ selected: speaker.deviceId === playoutDeviceId }"
                    @click="selectSpeaker(speaker.deviceId)"
                  >
                    {{ speaker.label }}
                  </li>
                </ul>
              </template>
            </div>
          </a-dropdown>
        </div>
        <div
          :class="[
            'device-status',
            'video-status',
            {
              'not-allowed': cameraAllowed === false,
              closed: !videoOpen,
            },
          ]"
        >
          <div @click="toggleVideo">
            <div class="device-icon-wrapper">
              <i
                :class="{
                  iconfont: true,
                  'icon-video': videoOpen,
                  'icon-video-off': !videoOpen,
                }"
              />
              <i v-if="!cameraAllowed" class="iconfont icon-important" />
            </div>
            <span> {{ videoOpen ? '关闭' : '打开' }}视频 </span>
          </div>
          <a-dropdown placement="topRight" transitionName="">
            <i class="iconfont icon-downArrow" />
            <div
              slot="overlay"
              :class="[
                'device-status-list',
                { 'not-allowed': cameraAllowed === false },
              ]"
            >
              <ul v-if="cameraAllowed === false">
                <li>请打开摄像头权限</li>
              </ul>
              <template v-else>
                <label class="camera">
                  <i class="iconfont icon-video-outlined" />
                  摄像头
                </label>
                <ul>
                  <li
                    v-for="camera in cameraList"
                    :key="camera.deviceId"
                    :class="{ selected: camera.deviceId === captureDeviceId }"
                    @click="selectCamera(camera.deviceId)"
                  >
                    {{ camera.label }}
                  </li>
                </ul>
              </template>
            </div>
          </a-dropdown>
        </div>
        <a-button
          type="primary"
          class="join-btn"
          :loading="joinLoading"
          @click="join"
          >加入会议</a-button
        >
        <!-- <div ></div> -->
        <a href="#/device-test" class="device-test" target="_blank"
          >使用有问题？去检测</a
        >
      </div>
    </div>
    <div
      class="not-allowed-mask"
      v-if="notAllowedMaskClosed === false && notAllowedDeviceText"
    >
      <div>
        <img src="../assets/img/browser-permission.png" alt="浏览器权限" />
        <h4>无法访问{{ notAllowedDeviceText }}</h4>
        <p>
          demo.pano.video需要使用你的麦克风和摄像头，<br />请点击浏览器地址栏中的锁图标更改设置，并重新加载网页
        </p>
        <a href="javascript:;" @click="notAllowedMaskClosed = true">关闭</a>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { UPDATE_SETTINGS } from '@/store/mutations';
import { Dropdown, Button } from 'ant-design-vue';
import PanoRtc from '@pano.video/panortc';
import UAParser from 'ua-parser-js';
import AudioLevel from '@/components/audio/AudioLevel.vue';
import { SAFARI_15_1, SAFARI_15_1_MESSAGE } from '@/utils/common';

const browser = new UAParser().getBrowser();

export default {
  components: {
    'a-dropdown': Dropdown,
    'a-button': Button,
    AudioLevel,
  },
  props: {
    audioOpen: Boolean,
    videoOpen: Boolean,
    joinLoading: Boolean,
  },
  data() {
    return {
      isChrome: browser.name === 'Chrome',
      micList: [],
      micMap: {},
      micAllowed: true,
      micAudioLevel: 0,
      speakerList: false,
      speakerMap: {},
      speakerAllow: true,
      cameraList: [],
      cameraMap: {},
      cameraAllowed: true,
      notAllowedMaskClosed: false,
    };
  },
  computed: {
    ...mapGetters(['captureDeviceId', 'playoutDeviceId', 'recordDeviceId']),
    notAllowedDeviceText() {
      return [
        this.micAllowed === false ? '麦克风' : null,
        this.cameraAllowed === false ? '摄像头' : null,
      ]
        .filter((text) => text !== null)
        .join('和');
    },
  },
  watch: {
    recordDeviceId() {
      if (this.audioOpen) {
        this.stopTestMic();
        this.testMic();
      }
    },
    captureDeviceId() {
      if (this.videoOpen) {
        this.stopPreview();
        this.startPreview();
      }
    },
  },
  mounted() {
    this.previewCode = 0;
    if (!window.rtcEngine.checkEnvRequirement()) {
      this.$notification.open({
        message: '您使用的浏览器不兼容，点击这里查看',
        description: (h) =>
          h(
            'a',
            {
              attrs: {
                href: 'https://www.pano.video/',
                target: '_blank',
              },
            },
            '浏览器兼容性'
          ),
        duration: 0,
      });
      return;
    }
    this.getMicList(() => {
      if (this.audioOpen) {
        this.testMic();
      }
    });
    this.getSpeakerList();
    this.getCameraList(() => {
      if (this.videoOpen) {
        this.startPreview();
      }
    });
    window.rtcEngine.on(
      PanoRtc.RtcEngine.Events.audioDeviceChange,
      this.audioDeviceChange
    );
    window.rtcEngine.on(
      PanoRtc.RtcEngine.Events.videoDeviceChange,
      this.getCameraList
    );
  },
  beforeDestroy() {
    this.$notification.destroy();
    this.stopTestMic();
    this.stopPreview();
    window.rtcEngine.off(
      PanoRtc.RtcEngine.Events.audioDeviceChange,
      this.audioDeviceChange
    );
    window.rtcEngine.off(
      PanoRtc.RtcEngine.Events.videoDeviceChange,
      this.getCameraList
    );
  },
  methods: {
    ...mapMutations([UPDATE_SETTINGS]),
    getMicList(fn) {
      window.rtcEngine.getMics(
        (devices) => {
          if (!this.micAllowed) {
            this.micAllowed = true;
          }
          this.micList = devices;
          if (devices.length === 0) {
            this.micMap = {};
            this.stopTestMic();
            this.$store.commit(UPDATE_SETTINGS, {
              recordDeviceId: '',
            });
            return;
          }
          this.micMap = devices.reduce((result, item) => {
            result[item.deviceId] = item.label;
            return result;
          }, {});
          let mic = devices.find(
            (item) => item.deviceId === this.recordDeviceId
          );
          if (!mic) {
            mic = devices.find((item) => item.selected);
            if (!mic) {
              mic = devices[0];
              rtcEngine.selectMic(mic.deviceId);
            }
            this.$store.commit(UPDATE_SETTINGS, {
              recordDeviceId: mic.deviceId,
            });
          } else {
            if (!mic.selected) {
              rtcEngine.selectMic(mic.deviceId);
            }
            if (typeof fn === 'function') {
              fn();
            }
          }
        },
        (error) => {
          if (error.errorName === 'NotAllowedError') {
            this.micAllowed = false;
          }
        }
      );
    },
    getSpeakerList() {
      window.rtcEngine.getSpeakers(
        (devices) => {
          this.speakerList = devices;
          if (devices.length === 0) {
            this.speakerMap = {};
            this.$store.commit(UPDATE_SETTINGS, {
              playoutDeviceId: '',
            });
            return;
          }
          this.speakerMap = devices.reduce((result, item) => {
            result[item.deviceId] = item.label;
            return result;
          }, {});
          let speaker = devices.find(
            (item) => item.deviceId === this.recordDeviceId
          );
          if (!speaker) {
            speaker = devices.find((item) => item.selected);
            if (!speaker) {
              speaker = devices[0];
              rtcEngine.selectSpeaker(speaker.deviceId);
            }
            this.$store.commit(UPDATE_SETTINGS, {
              playoutDeviceId: speaker.deviceId,
            });
          } else if (!speaker.selected) {
            rtcEngine.selectSpeaker(speaker.deviceId);
          }
        },
        (error) => {
          if (error.errorName === 'NotAllowedError') {
            this.micAllowed = false;
          }
        }
      );
    },
    getCameraList(fn) {
      window.rtcEngine.getCams(
        (devices) => {
          if (!this.cameraAllowed) {
            this.cameraAllowed = true;
          }
          this.cameraList = devices;
          if (devices.length === 0) {
            this.cameraMap = {};
            this.stopPreview();
            this.$store.commit(UPDATE_SETTINGS, {
              captureDeviceId: '',
            });
            return;
          }
          this.cameraMap = devices.reduce((result, item) => {
            result[item.deviceId] = item.label;
            return result;
          }, {});
          let camera = devices.find(
            (item) => item.deviceId === this.captureDeviceId
          );
          if (!camera) {
            camera = devices.find((item) => item.selected);
            if (!camera) {
              camera = devices[0];
              rtcEngine.selectCam(camera.deviceId);
            }
            this.$store.commit(UPDATE_SETTINGS, {
              captureDeviceId: camera.deviceId,
            });
          } else {
            if (!camera.selected) {
              rtcEngine.selectCam(camera.deviceId);
            }
            if (typeof fn === 'function') {
              fn();
            }
          }
        },
        (error) => {
          if (error.errorName === 'NotAllowedError') {
            this.cameraAllowed = false;
          }
        }
      );
    },
    audioDeviceChange() {
      this.getMicList();
      this.getSpeakerList();
    },
    startPreview() {
      // 视频关闭进入，摄像头列表没有返回前点击打开视频，会触发两次startPreview，
      if (this.currentPreviewCode === this.previewCode) {
        return;
      }
      this.currentPreviewCode = this.previewCode;
      const previewCode = this.previewCode;
      window.rtcEngine.startPreview(
        this.captureDeviceId,
        (videoTag) => {
          this.onPreviewSuccess(videoTag, previewCode);
        },
        (error) => {
          if (error.name === 'NotAllowedError') {
            this.cameraAllowed = false;
          }
        }
      );
    },
    onPreviewSuccess(videoTag, previewCode) {
      if (previewCode !== this.previewCode) {
        window.rtcEngine.stopPreview(videoTag);
        return;
      }
      videoTag.setAttribute(
        'style',
        'width: 100%; height: 100%; margin: auto; display: block; object-fit: cover;'
      );
      videoTag.autoplay = true;
      this.$refs.view.appendChild(videoTag);
      videoTag.play && videoTag.play();
    },
    stopPreview() {
      this.previewCode++;
      if (this.$refs.view) {
        const elem = this.$refs.view.getElementsByTagName('video')[0];
        if (elem) {
          window.rtcEngine.stopPreview(elem);
          elem.parentNode.removeChild(elem);
        }
      }
    },
    testMic() {
      window.rtcEngine.startRecordDeviceTest(
        this.recordDeviceId,
        ({ audioLevel, micTest }) => {
          this.micAudioLevel = audioLevel;
          this.micTest = micTest;
          if (this._isDestroyed) {
            this.stopTestMic();
          }
        },
        (error) => {
          if (error.name === 'NotAllowedError') {
            this.micAllowed = false;
          }
        }
      );
    },
    stopTestMic() {
      if (this.micTest !== undefined) {
        window.rtcEngine.stopRecordDeviceTest(this.micTest);
        this.micTest = undefined;
      }
    },
    selectMic(deviceId) {
      if (deviceId === this.recordDeviceId) {
        return;
      }
      this.$store.commit(UPDATE_SETTINGS, {
        recordDeviceId: deviceId,
      });
      rtcEngine.selectMic(deviceId);
    },
    selectSpeaker(deviceId) {
      if (deviceId === this.playoutDeviceId) {
        return;
      }
      this.$store.commit(UPDATE_SETTINGS, {
        playoutDeviceId: deviceId,
      });
      rtcEngine.selectSpeaker(deviceId);
    },
    selectCamera(deviceId) {
      if (deviceId === this.captureDeviceId) {
        return;
      }
      this.$store.commit(UPDATE_SETTINGS, {
        captureDeviceId: deviceId,
      });
      rtcEngine.selectCam(deviceId);
    },
    toggleAudio() {
      if (this.audioOpen) {
        this.stopTestMic();
        this.$emit('setAudioOpen', false);
      } else {
        this.testMic();
        this.$emit('setAudioOpen', true);
      }
    },
    toggleVideo() {
      if (SAFARI_15_1) {
        this.$message.info(SAFARI_15_1_MESSAGE);
        return;
      }
      if (this.videoOpen) {
        this.stopPreview();
        this.$emit('setVideoOpen', false);
      } else {
        this.startPreview();
        this.$emit('setVideoOpen', true);
      }
    },
    join() {
      this.$emit('join');
    },
  },
};
</script>

<style lang="less" scoped>
@import '~ant-design-vue/es/style/themes/default.less';

.web-preview {
  width: 100%;
  height: 100%;

  .page-header {
    height: 64px;
    padding-left: 40px;
    border-bottom: 2px solid #ddd;
    display: flex;
    align-items: center;
    a {
      display: inline-block;
    }
    img {
      width: 196px;
    }
  }

  .device-container {
    padding: 30px 0;
    height: calc(100% - 64px);
    display: flex;
    flex-direction: column;

    .video-wrapper,
    .device-wrapper {
      max-width: 960px;
      max-height: 540px;
      width: 90vw;
    }

    .video-wrapper {
      max-height: 540px;
      flex: 1;
      margin: 0 auto;
      background: rgba(63, 64, 65, 0.9);
      border-radius: 16px;
      position: relative;
      > div {
        height: 100%;
        border-radius: 16px;
        overflow: hidden;
        &.video-not-open::before {
          position: absolute;
          left: 50%;
          top: 50%;
          font-size: 126px;
          line-height: 1;
          color: #ccc;
          transform: translate3d(-50%, -50%, 0);
          margin-top: -48px;
        }
      }
      > span {
        position: absolute;
        left: 50%;
        top: 24px;
        transform: translateX(-50%);
        padding: 12px 16px;
        background-color: #fff;
        border-radius: 3px;
        box-shadow: 1px 3px rgba(4, 0, 0, 0.35);
      }
      > ul {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 48px;
        margin: 0;
        padding: 0;
        background: rgba(255, 255, 255, 0.7);
        display: flex;
        > li {
          width: 320px;
          list-style: none;
          padding: 0 24px;
          display: flex;
          justify-content: center;
          align-items: center;
        }
      }
    }

    .device-wrapper {
      margin: 32px auto 0;
      position: relative;
      display: flex;
      justify-content: center;

      .device-status,
      .audio-status,
      .join-btn {
        width: 140px;
        height: 40px;
        border-radius: 20px;
      }

      .join-btn {
        background-color: #0899f9;
        border-color: #0899f9;
      }

      .device-test {
        position: absolute;
        right: 0;
        top: 50%;
        transform: translateY(-50%);
      }

      .device-status {
        margin-right: 16px;
        background: rgba(82, 83, 84, 0.9);
        color: #fff;
        display: flex;
        line-height: 40px;
        cursor: pointer;
        transition: background-color ease-in-out 0.3s;

        > div {
          display: flex;
          flex: 1;
          > label {
            margin: 0 0 0 12px;
            position: relative;
            font-size: 24px;
          }
          > span {
            flex: 1;
            cursor: pointer;
            padding-left: 6px;
            height: 100%;
            align-items: center;
            display: flex;
          }
        }

        .device-icon-wrapper {
          margin-left: 15px;
          height: 100%;
          display: flex;
          align-items: center;
          position: relative;
          .audio-level,
          .icon-video,
          .icon-video-off {
            font-size: 22px;
          }
          .icon-important {
            position: absolute;
            top: 2px;
            right: -4px;
            font-size: 16px;
            line-height: 1;
            color: #f5b52e;
          }
        }
        .icon-video-off {
          color: red;
          position: relative;
        }
        &:hover {
          background-color: #434445;
        }
        .icon-downArrow {
          padding: 0 8px 0 2px;
          border-radius: 0 20px 20px 0;
          transform-origin: center center;
          cursor: pointer;
          transition: all ease-in-out 0.3s;
          padding: 0 10px;
          &.ant-dropdown-open,
          &:hover {
            transform: rotate(-180deg);
          }
        }
      }

      .audio-status {
        > label {
          > i {
            position: absolute;
            left: 0;
            right: 0;
            top: 50%;
            height: 24px;
            transform: translateY(-50%);
            > i {
              display: block;
              width: (40 / 128) * 1em;
              height: (68 / 128) * 1em;
              margin: (12 / 128) * 1em 0 0 (44 / 128) * 1em;
              border-radius: (20 / 128) * 1em;
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
      }
    }
  }
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.device-item {
  display: flex;
  width: 100%;
  padding: 0 6px 0 8px;
  line-height: 36px;
  color: #666;
  justify-content: center;
  border-radius: 18px;
  cursor: pointer;
  transition: background-color ease-in-out 0.3s;
  .iconfont {
    font-size: 18px;
    padding-right: 10px;
  }
  .icon-downArrow {
    padding-right: 0;
  }
  &:hover,
  &.ant-dropdown-open {
    background-color: #fff;
    .icon-downArrow {
      transition: transform ease-in-out 0.3s;
      padding-right: 0;
      transform: rotate(-180deg);
    }
  }
  > span {
    max-width: 226px;
    padding-right: 12px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}

.device-item-list {
  margin-bottom: 10px;
  padding: 6px 0;
  background-color: #000;
  border-radius: 8px;
  color: #fff;
  > label {
    display: block;
    line-height: 32px;
    font-size: 12px;
    .iconfont {
      font-size: 14px;
      color: #fff;
      padding: 0 5px 0 8px;
    }
  }
  > ul {
    list-style: none;
    margin: 0;
    font-size: 13px;
    line-height: 17px;
    > li {
      display: block;
      padding: 0 12px 0 31px;
      font-size: 13px;
      line-height: 28px;
      cursor: pointer;
      position: relative;
      &.selected::before {
        content: '';
        position: absolute;
        left: 15px;
        bottom: 10px;
        width: 10px;
        height: 6px;
        border-color: #0899f9;
        border-style: solid;
        border-width: 0 0 2px 2px;
        transform-origin: left bottom;
        transform: rotate(-45deg);
      }
    }
  }
}

.device-status-list {
  padding-bottom: 12px;
  background-color: #fff;
  box-shadow: 0px 1px 8px 0px rgba(4, 0, 0, 0.22);
  border-radius: 4px;
  .iconfont {
    font-size: 14px;
    padding: 0 5px 0 8px;
  }
  &:hover ~ .icon-downArrow {
    transform: rotate(0);
  }
  > label {
    display: block;
    padding-right: 24px;
    line-height: 32px;
    font-size: 12px;
  }
  > ul {
    list-style: none;
    margin: 0;
    > li {
      padding: 0 12px 0 31px;
      font-size: 13px;
      line-height: 28px;
      cursor: pointer;
      position: relative;
      &:hover {
        background-color: #f0f0f0;
      }
      &.selected::before {
        content: '';
        position: absolute;
        left: 15px;
        bottom: 9px;
        width: 10px;
        height: 6px;
        border-color: #0899f9;
        border-style: solid;
        border-width: 0 0 1px 1px;
        transform-origin: left bottom;
        transform: rotate(-45deg);
      }
    }
  }
  > i {
    display: block;
    border-top: 1px solid #ddd;
    margin: 12px 8px 4px;
  }
  &.not-allowed {
    padding: 6px 0;
    > ul > li {
      padding: 0 48px 0 18px;
    }
  }
}
.not-allowed-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 199;
  color: #fff;
  > div {
    position: absolute;
    top: 12px;
    left: (116 /1920) * 100%;
    text-align: center;
    > img {
      vertical-align: top;
    }
    > h4 {
      margin: 12px 0 0;
      font-size: 30px;
      line-height: 2;
      color: #fff;
    }
    > p {
      margin: 0;
      font-size: 20px;
      line-height: 2;
    }
    > a {
      display: inline-block;
      width: 120px;
      margin-top: 20px;
      border: 2px solid #d5d5d5;
      border-radius: 4px 2px 2px 2px;
      font-size: 20px;
      line-height: 46px;
      text-align: center;
      color: #fff;
    }
  }
}
</style>
