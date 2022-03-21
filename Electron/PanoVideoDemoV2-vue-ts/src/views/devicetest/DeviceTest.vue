<template>
  <div class="container">
    <div v-if="step === 0">
      <h3>音视频检测</h3>
      <ul>
        <li>浏览器兼容检测</li>
        <li>麦克风检测</li>
        <li>扬声器检测</li>
        <li>摄像头检测</li>
      </ul>
      <span>
        <a href="javascript:;" @click="goTestCompatibility">开始检测</a>
      </span>
    </div>
    <div v-else-if="step === 1">
      <h3>音视频检测</h3>
      <div>
        <h4>浏览器兼容检测（1/4）</h4>
        <p>正在检测当前浏览器是否支持音视频</p>
        <div class="progress-bar">
          <i>
            <i :style="{ width: compatibilityProgress }"></i>
          </i>
          <span>{{ compatibilityProgress }}</span>
        </div>
        <em>检测中...</em>
      </div>
    </div>
    <div v-else-if="step === 2">
      <h3>音视频检测</h3>
      <div>
        <h4>
          <span>麦克风检测（2/4）</span>
          <em>预计剩余：{{ testMicRemainingTime }}s</em>
        </h4>
        <p>
          检查您的麦克风是否正常工作。（请对麦克风讲话，看看下面的进度栏是否发生变化）
        </p>
        <div class="mic-level">
          <label>输入等级</label>
          <i><i :style="{ width: micAudioLevel }"></i></i>
        </div>
        <a-dropdown v-if="micList.length > 0">
          <div class="device-select">
            <i class="iconfont icon-speaker-outlined" />
            <span>{{ micMap[micDeviceId] || '请选择麦克风' }}</span>
            <i class="iconfont icon-downArrow" />
          </div>
          <ul slot="overlay" class="device-list">
            <li
              v-for="mic in micList"
              :key="mic.deviceId"
              :class="{ selected: mic.deviceId === micDeviceId }"
              @click="selectMic(mic.deviceId)"
            >
              {{ mic.label }}
            </li>
          </ul>
        </a-dropdown>
      </div>
    </div>
    <div v-else-if="step === 3">
      <h3>音视频检测（3/4）</h3>
      <div>
        <h4>扬声器检测</h4>
        <p>
          检测扬声器以确保您可以听到他人的声音（请点击播放示例音乐，看看是否可以听到示例音乐）
        </p>
        <audio
          src="../../assets/audio/Ukulele-Song.mp3"
          controls="controls"
        ></audio>
        <a-dropdown v-if="speakerChangeable && micAllowed">
          <div class="device-select">
            <i class="iconfont icon-speaker-outlined" />
            <span>{{ speakerMap[speakerDeviceId] || '请选择扬声器' }}</span>
            <i class="iconfont icon-downArrow" />
          </div>
          <ul slot="overlay" class="device-list">
            <li
              v-for="speaker in speakerList"
              :key="speaker.deviceId"
              :class="{ selected: speaker.deviceId === speakerDeviceId }"
              @click="selectSpeaker(speaker.deviceId)"
            >
              {{ speaker.label }}
            </li>
          </ul>
        </a-dropdown>
        <div class="result-choose">
          <p>是否可以听到示例音乐</p>
          <span>
            <a href="javascript:;" @click="chooseSpeakerYes">是</a>
            <a href="javascript:;" @click="chooseSpeakerNo">否</a>
          </span>
        </div>
      </div>
    </div>
    <div v-else-if="step === 4">
      <h3>音视频检测</h3>
      <div>
        <h4>摄像头检测（4/4）</h4>
        <p>检查您的摄像头是否正常工作，画面是否正常显示</p>
        <div ref="view" class="video-wrapper video-mirror"></div>
        <a-dropdown>
          <div class="device-select">
            <i class="iconfont icon-video-outlined" />
            <span>{{ cameraMap[cameraDeviceId] || '请选择摄像头' }}</span>
            <i class="iconfont icon-downArrow" />
          </div>
          <ul slot="overlay" class="device-list">
            <li
              v-for="camera in cameraList"
              :key="camera.deviceId"
              :class="{ selected: camera.deviceId === cameraDeviceId }"
              @click="selectCamera(camera.deviceId)"
            >
              {{ camera.label }}
            </li>
          </ul>
        </a-dropdown>
        <div class="result-choose">
          <p>摄像头是否正常工作，画面是否正常显示</p>
          <span>
            <a href="javascript:;" @click="chooseCameraYes">是</a>
            <a href="javascript:;" @click="chooseCameraNo">否</a>
          </span>
        </div>
      </div>
    </div>
    <div v-else>
      <h3>音视频检测</h3>
      <div class="test-result-wrapper">
        <h4>检测结果</h4>
        <ul>
          <li>
            <h5>浏览器兼容检测</h5>
            <div v-if="compatibilityResult" class="test-result">
              <label class="compatible"></label>
              <div>
                <p>完全支持</p>
              </div>
            </div>
            <div v-else class="test-result">
              <label class="warning"></label>
              <div>
                <p>浏览器：{{ browserStr }}</p>
                <p>未经过完整测试的浏览器</p>
              </div>
            </div>
          </li>
          <li>
            <h5>麦克风检测</h5>
            <div v-if="micAllowed === false" class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>麦克风权限没有打开</p>
              </div>
            </div>
            <div v-else-if="micList.length === 0" class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>没有可用的麦克风</p>
              </div>
            </div>
            <div v-else-if="micResult" class="test-result">
              <label class="compatible"></label>
              <div>
                <p>{{ micMap[micDeviceId] }} 麦克风正常工作</p>
              </div>
            </div>
            <div v-else class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>{{ micMap[micDeviceId] }} 麦克风异常</p>
              </div>
            </div>
          </li>
          <li>
            <h5>扬声器检测</h5>
            <div v-if="speakerResult" class="test-result">
              <label class="compatible"></label>
              <div>
                <p>{{ speakerMap[speakerDeviceId] }} 扬声器正常工作</p>
              </div>
            </div>
            <div v-else class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>{{ speakerMap[speakerDeviceId] }} 扬声器异常</p>
              </div>
            </div>
          </li>
          <li>
            <h5>摄像头检测</h5>
            <div v-if="cameraAllowed === false" class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>摄像头权限没有打开</p>
              </div>
            </div>
            <div v-else-if="cameraResult" class="test-result">
              <label class="compatible"></label>
              <div>
                <p>{{ cameraMap[cameraDeviceId] }} 摄像头正常工作</p>
              </div>
            </div>
            <div v-else class="test-result">
              <label class="incompatible"></label>
              <div>
                <p>{{ cameraMap[cameraDeviceId] }} 摄像头异常</p>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <span>
        <a href="javascript:;" @click="retest">重新检测</a>
      </span>
    </div>
  </div>
</template>

<script>
import { Dropdown } from 'ant-design-vue';
import UAParser from 'ua-parser-js';

const browser = new UAParser().getBrowser();

const testMicTime = 12000;

export default {
  components: {
    'a-dropdown': Dropdown,
  },
  data() {
    return {
      step: 0,
      compatibilityProgress: '0%',
      compatibilityResult: null,
      browserStr: `${browser.name} ${browser.version}`,
      testMicRemainingTime: null,
      micAudioLevel: '0%',
      micResult: null,
      micAllowed: null,
      micDeviceId: 'default',
      micList: [],
      micMap: {},
      speakerChangeable:
        typeof HTMLMediaElement.prototype.setSinkId === 'function'
          ? true
          : false,
      speakerResult: null,
      speakerDeviceId: 'default',
      speakerList: [],
      speakerMap: {},
      cameraAllowed: null,
      cameraResult: null,
      cameraDeviceId: 'default',
      cameraList: [],
      cameraMap: {},
    };
  },
  created() {
    document.body.style.backgroundColor = '#f5f5f5';
    document.body.style.padding = '40px 0';
  },
  beforeDestroy() {
    document.body.style.backgroundColor = '';
    document.body.style.padding = '';
    cancelAnimationFrame(this.testCompatibilityTimer);
    if (this.micTest !== undefined) {
      window.rtcEngine.stopRecordDeviceTest(this.micTest);
      this.micTest = undefined;
    }
    this.stopPreview();
  },
  methods: {
    goTestCompatibility() {
      this.step = 1;
      this.testCompatibility();
    },
    testCompatibility() {
      const startTime = Date.now();
      const totalTime = 2000;
      const setProgress = () => {
        const time = Date.now() - startTime;
        if (time >= totalTime) {
          this.compatibilityProgress = '100%';
          if (!window.rtcEngine.checkEnvRequirement()) {
            this.compatibilityResult = false;
          } else {
            this.compatibilityResult = true;
          }
          this.goTestMic();
        } else {
          this.compatibilityProgress = `${Math.round(
            (time / totalTime) * 100
          )}%`;
          this.testCompatibilityTimer = requestAnimationFrame(setProgress);
        }
      };
      this.$nextTick(setProgress);
    },
    goTestMic() {
      this.step = 2;
      this.testMicRemainingTime = Math.ceil(testMicTime / 1000);
      this.testMic();
    },
    testMic() {
      const totalCount = Math.round((testMicTime / 200) * 0.5);
      const endTime = Date.now() + testMicTime;
      let count = 0;
      window.rtcEngine.startRecordDeviceTest(
        this.micDeviceId,
        ({ audioLevel, micTest }) => {
          if (this.micAllowed === null) {
            this.micAllowed = true;
            this.getMicList();
            this.getSpeakerList();
          }
          this.micTest = micTest;
          this.micAudioLevel = `${audioLevel * 300}%`;
          if (audioLevel > 0.002) {
            count++;
          }
          const time = endTime - Date.now();
          this.testMicRemainingTime = Math.ceil((time < 0 ? 0 : time) / 1000);
          if (count >= totalCount) {
            this.micResult = true;
            window.rtcEngine.stopRecordDeviceTest(micTest);
            this.micTest = undefined;
            this.step = 3;
            return;
          }
          if (time <= 0) {
            this.micResult = false;
            window.rtcEngine.stopRecordDeviceTest(micTest);
            this.micTest = undefined;
            this.step = 3;
          }
        },
        (error) => {
          if (
            error.message.includes('NotAllowedError') ||
            error.name === 'NotAllowedError'
          ) {
            this.micAllowed = false;
          }
          this.step = 3;
        }
      );
    },
    getMicList() {
      window.rtcEngine.getMics((devices) => {
        this.micList = devices;
        this.micMap = devices.reduce((result, device) => {
          if (device.selected) {
            this.micDeviceId = device.deviceId;
          }
          result[device.deviceId] = device.label;
          return result;
        }, {});
        if (devices.length === 0) {
          this.step = 3;
        }
      });
    },
    selectMic(deviceId) {
      this.micDeviceId = deviceId;
      this.retestMic();
    },
    retestMic() {
      this.micAudioLevel = '0%';
      this.micResult = null;
      if (this.micTest !== undefined) {
        window.rtcEngine.stopRecordDeviceTest(this.micTest);
        this.micTest = undefined;
      }
      this.testMic();
    },
    getSpeakerList() {
      window.rtcEngine.getSpeakers((devices) => {
        this.speakerList = devices;
        this.speakerMap = devices.reduce((result, device) => {
          if (device.selected) {
            this.speakerDeviceId = device.deviceId;
          }
          result[device.deviceId] = device.label;
          return result;
        }, {});
      });
    },
    selectSpeaker(deviceId) {
      this.speakerDeviceId = deviceId;
      const elem = document.getElementsByTagName('audio')[0];
      elem.setSinkId(deviceId === 'default' ? '' : deviceId);
    },
    chooseSpeakerYes() {
      this.goTestCamera(true);
    },
    chooseSpeakerNo() {
      this.goTestCamera(false);
    },
    goTestCamera(speakerResult) {
      this.speakerResult = speakerResult;
      this.step = 4;
      this.$nextTick(this.testCamera);
    },
    testCamera() {
      navigator.mediaDevices
        .getUserMedia({
          audio: false,
          video: {
            deviceId: this.cameraDeviceId || undefined,
          },
        })
        .then(this.onPreviewSuccess)
        .catch((error) => {
          if (error.name === 'NotAllowedError') {
            this.cameraAllowed = false;
            this.step = 5;
          }
        });
    },
    onPreviewSuccess(stream) {
      if (this.cameraAllowed === null) {
        this.cameraAllowed = true;
        this.getCameraList();
      }
      const videoTag = document.createElement('video');
      videoTag.setAttribute(
        'style',
        'width: 100%; height: 100%; margin: auto; display: block; object-fit: cover;'
      );
      videoTag.srcObject = stream;
      videoTag.autoplay = true;
      this.$refs.view.appendChild(videoTag);
      videoTag.play && videoTag.play();
    },
    stopPreview() {
      const video = this.$refs.view.getElementsByTagName('video')[0];
      if (video && video.srcObject) {
        video.srcObject.getTracks().forEach((track) => {
          track.stop();
        });
        video.parentNode.removeChild(video);
      }
    },
    getCameraList() {
      window.rtcEngine.getCams((devices) => {
        this.cameraList = devices;
        this.cameraMap = devices.reduce((result, device) => {
          result[device.deviceId] = device.label;
          if (device.selected) {
            this.cameraDeviceId = device.deviceId;
          }
          return result;
        }, {});
      });
    },
    selectCamera(deviceId) {
      if (this.cameraDeviceId !== deviceId) {
        this.cameraDeviceId = deviceId;
        this.retestCamera();
      }
    },
    retestCamera() {
      this.cameraResult = null;
      this.stopPreview();
      this.$nextTick(this.testCamera);
    },
    chooseCameraYes() {
      this.goResult(true);
      this.stopPreview();
    },
    chooseCameraNo() {
      this.goResult(false);
      this.stopPreview();
    },
    goResult(cameraResult) {
      this.cameraResult = cameraResult;
      this.step = 5;
    },
    retest() {
      this.step = 0;
      this.compatibilityProgress = '0%';
      this.compatibilityResult = null;
      this.micAudioLevel = '0%';
      this.micResult = null;
      this.micAllowed = null;
      this.speakerResult = null;
      this.cameraAllowed = null;
      this.cameraResult = null;
    },
  },
};
</script>

<style lang="less" scoped>
.container {
  max-width: 1200px;
  min-height: 100%;
  margin: 0 auto;
  background-color: #fff;
  > div {
    padding: 100px 200px;
    line-height: 1.5;
    > h3 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
    > ul,
    > div {
      margin: 24px 0 0;
      padding: 37px 47px;
      list-style: none;
      box-shadow: 0px 0px 12px 0px rgba(4, 0, 0, 0.11);
    }
    > ul > li,
    > div > h4 {
      font-size: 20px;
      padding: 10px 0;
    }
    > div {
      > h4 {
        margin: 0;
        display: flex;
        > span {
          flex: 1;
        }
        > em {
          font-style: normal;
          font-size: 15px;
          line-height: 30px;
          color: #999;
        }
      }
      > p {
        margin: 4px 0 15px;
        font-size: 16px;
      }
      > em {
        display: block;
        margin-top: 45px;
        font-style: normal;
        text-align: center;
        margin-bottom: 6px;
        font-size: 16px;
        color: #999;
      }
    }
    > span {
      display: block;
      margin-top: 40px;
      text-align: center;
      > a {
        display: inline-block;
        vertical-align: top;
        width: 104px;
        height: 38px;
        margin-right: 16px;
        background: #0899f9;
        border: 1px solid #0899f9;
        border-radius: 4px;
        font-size: 18px;
        line-height: 36px;
        font-weight: 500;
        color: #fff;
        &:last-child {
          margin-right: 0;
        }
      }
    }
  }
}
.progress-bar {
  display: flex;
  align-items: center;
  > i {
    flex: 1;
    height: 8px;
    background: #dadada;
    border-radius: 4px;
    position: relative;
    > i {
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      border-radius: 4px;
      background: #0899f9;
    }
  }
  > span {
    width: 48px;
    font-size: 16px;
    line-height: 1;
    text-align: right;
  }
}
.test-result {
  margin-top: 12px;
  display: flex;
  align-items: flex-start;
  > label {
    flex-shrink: 0;
    display: inline-block;
    width: 16px;
    height: 16px;
    margin: 7px 8px 0 0;
    border-radius: 50%;
    position: relative;
    text-align: center;
    &.compatible {
      background-color: #52c41a;
      &::before {
        content: '';
        position: absolute;
        left: 6px;
        bottom: 4px;
        width: 9px;
        height: 5px;
        border-style: solid;
        border-color: #fff;
        border-width: 0 0 1px 1px;
        transform-origin: left bottom;
        transform: rotate(-45deg);
      }
    }
    &.incompatible {
      background-color: #f74340;
      &::before {
        content: 'X';
        font-size: 13px;
        line-height: 16px;
        vertical-align: top;
        color: #fff;
      }
    }
    &.warning {
      background-color: #f5b52e;
      &::before {
        content: '!';
        line-height: 16px;
        vertical-align: top;
        color: #fff;
      }
    }
  }
  > div {
    font-size: 16px;
    font-weight: 500;
    color: #666;
    > p {
      margin: 0;
      padding: 3px 0;
    }
  }
}
.mic-level {
  display: flex;
  align-items: center;
  padding: 5px 0;
  line-height: 1;
  > label {
    padding-right: 10px;
    font-size: 16px;
    font-weight: 500;
    line-height: 1;
  }
  > i {
    flex: 1;
    height: 8px;
    background: #dadada;
    border-radius: 4px;
    position: relative;
    overflow: hidden;
    > i {
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      background-color: #0899f9;
      border-radius: 4px;
    }
  }
}
.device-select {
  width: 270px;
  margin: 23px auto 3px;
  background-color: #ffffff;
  border: 1px solid #636465;
  border-radius: 20px;
  line-height: 38px;
  display: flex;
  padding: 0 8px 0 10px;
  .iconfont {
    font-size: 18px;
    padding-right: 10px;
  }
  .icon-downArrow {
    transition: transform ease-in-out 0.3s;
    padding-right: 0;
  }
  &:hover,
  &.ant-dropdown-open {
    background-color: #fff;
    .icon-downArrow {
      padding-right: 0;
      transform: rotate(-180deg);
    }
  }
  &.ant-dropdown-open {
    cursor: pointer;
  }
  > label {
    margin-right: 6px;
  }
  > span {
    flex: 1;
    padding-right: 9px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.device-list {
  padding: 6px 0;
  background-color: #fff;
  box-shadow: 0px 1px 8px 0px rgba(4, 0, 0, 0.22);
  border-radius: 8px;
  font-size: 13px;
  line-height: 17px;
  > li {
    min-height: 32px;
    display: flex;
    align-items: center;
    padding: 0 12px;
    cursor: pointer;
    &:hover {
      color: #0899f9;
    }
    &.selected {
      color: #0899f9;
      background-color: #f0f0f0;
    }
  }
}
.result-choose {
  margin-top: 40px;
  padding: 28px 0 3px;
  border-top: 1px solid #ddd;
  text-align: center;
  > p {
    margin: 0 0 15px;
    font-size: 16px;
  }
  > span {
    > a {
      display: inline-block;
      width: 48px;
      margin-right: 16px;
      line-height: 32px;
      color: #fff;
      border-radius: 4px;
      background-color: #0899f9;
      &:last-child {
        margin-right: 0;
        background-color: #f74340;
      }
    }
  }
}
.video-wrapper {
  width: 480px;
  height: 270px;
  margin: 19px auto 32px;
  background: #333;
}
.container > div > .test-result-wrapper {
  padding: 0;
  > h4 {
    padding: 0 40px;
    background-color: #ddd;
    font-size: 20px;
    line-height: 60px;
  }
  > ul {
    padding: 0 20px;
    list-style: none;
    margin: 0;
    > li {
      padding: 34px 20px 33px;
      border-bottom: 2px solid #ddd;
      &:last-child {
        border-bottom: none;
      }
      > h5 {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
      }
      > .test-result {
        margin-top: 10px;
      }
    }
  }
}
</style>
