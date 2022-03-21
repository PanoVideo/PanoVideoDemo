<template>
  <div class="audio-setting">
    <ul>
      <li class="device-select">
        <label>麦克风</label>
        <Select
          :value="recordDeviceId"
          @change="onRecordDeviceChange"
          :style="{ width: '240px' }"
        >
          <a-select-option
            v-for="item in micList"
            :key="item.deviceId"
            :value="item.deviceId"
          >
            {{ item.label }}
          </a-select-option>
        </Select>
        <a-button type="primary" @click="toggleTestMic"
          >{{ micTesting ? '停止' : '开始' }}检测</a-button
        >
      </li>
      <li class="device-level">
        <label>输入等级</label>
        <div>
          <span
            ><i v-if="micTesting" :style="`width: ${micAudioLevel};`"></i
          ></span>
        </div>
      </li>
      <li v-if="$IS_ELECTRON" class="device-volume">
        <label>音量</label>
        <i class="iconfont icon-speaker"></i>
        <div>
          <a-slider
            style="margin-top: 10px"
            :tooltipVisible="false"
            :max="255"
            :value="micVolume"
            @change="changeMicVolume"
          />
        </div>
      </li>
      <li v-if="$IS_ELECTRON" class="aagc">
        <label>自动音量调整</label>
        <div>
          <a-switch :defaultChecked="enableAAGC" @change="switchAagc" />
        </div>
      </li>
    </ul>
    <ul>
      <li class="device-select">
        <label>扬声器</label>
        <Select
          placeholder="请选择扬声器"
          :value="playoutDeviceId"
          @change="onPlayoutDeviceChange"
          style="width: 240px"
        >
          <a-select-option
            v-for="item of speakerList"
            :key="item.deviceId"
            :value="item.deviceId"
          >
            {{ item.label }}
          </a-select-option>
        </Select>
        <a-button type="primary" @click="toggleSpeakerTest"
          >{{ speakerTesting ? '停止' : '开始' }}检测</a-button
        >
        <audio
          ref="testAudio"
          style="display: none"
          src="../../assets/audio/Ukulele-Song.mp3"
          loop
        ></audio>
      </li>
      <li class="device-level" v-if="$IS_ELECTRON">
        <label>输出等级</label>
        <div>
          <span
            ><i v-if="speakerTesting" :style="`width: ${speakerLevel};`"></i
          ></span>
        </div>
      </li>
      <li class="device-volume">
        <label>音量</label>
        <i class="iconfont icon-speaker"></i>
        <div>
          <a-slider
            :tooltipVisible="false"
            :max="$IS_ELECTRON ? 255 : 1"
            :step="$IS_ELECTRON ? 1 : 0.01"
            :value="speakerVolume"
            style="margin-top: 10px"
            @change="changeSpeakerVolume"
          />
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import { Switch, Select, Button, Slider } from 'ant-design-vue';
import { mapGetters, mapMutations, mapState } from 'vuex';
import path from 'path';
import { initAudioDeviceList } from '@/pano/index';
import { UPDATE_SETTINGS } from '../../store/mutations';
import { localCacheKeyPlayoutVolume } from '@/constants';

export default {
  components: {
    Select,
    'a-switch': Switch,
    'a-select-option': Select.Option,
    'a-button': Button,
    'a-slider': Slider,
  },
  data() {
    return {
      micTesting: false,
      micAudioLevel: '0%',
      micVolume: window.IS_ELECTRON
        ? window.rtcEngine.getAudioRecordVolume()
        : 0,
      speakerChangeable:
        typeof HTMLMediaElement.prototype.setSinkId === 'function'
          ? true
          : false,
      speakerTesting: false,
      speakerVolume: window.rtcEngine.getAudioPlayoutVolume(),
      speakerLevel: '0%',
    };
  },
  computed: {
    ...mapState({
      audioDeviceReady: (state) => state.settingStore.audioDeviceReady,
      recordDeviceId: (state) => state.settingStore.recordDeviceId,
      playoutDeviceId: (state) => state.settingStore.playoutDeviceId,
      enableAAGC: (state) => state.settingStore.enableAAGC,
      sysDefaultMicId: (state) => state.settingStore.sysDefaultMicId,
      sysDefaultSpeakerId: (state) => state.settingStore.sysDefaultSpeakerId,
    }),
    ...mapGetters(['micList', 'speakerList']),
  },
  watch: {
    recordDeviceId() {
      if (this.$IS_ELECTRON) {
        this.micVolume = window.rtcEngine.getAudioRecordVolume();
      }
      if (this.micTesting) {
        this.stopTestMic();
        this.testMic();
      }
    },
    playoutDeviceId() {
      this.speakerVolume = window.rtcEngine.getAudioPlayoutVolume();
      if (this.speakerTesting) {
        this.stopTestSpeaker();
        this.testSpeaker();
      }
    },
    sysDefaultMicId() {
      if (this.recordDeviceId === 'default') {
        this.micVolume = window.rtcEngine.getAudioRecordVolume();
      }
    },
    sysDefaultSpeakerId() {
      if (this.playoutDeviceId === 'default') {
        this.speakerVolume = window.rtcEngine.getAudioPlayoutVolume();
      }
    },
  },
  mounted() {
    if (!this.audioDeviceReady) {
      initAudioDeviceList();
    }
    // web使用
    if (!this.$IS_ELECTRON) {
      this.$refs.testAudio.volume = this.speakerVolume;
    }
  },
  beforeDestroy() {
    if (this.micTesting) {
      this.stopTestMic();
    }
    if (this.speakerTesting) {
      this.stopTestSpeaker();
    }
  },
  methods: {
    ...mapMutations([UPDATE_SETTINGS]),
    onRecordDeviceChange(deviceId) {
      window.rtcEngine.selectMic(deviceId);
      this.$store.commit(UPDATE_SETTINGS, { recordDeviceId: deviceId });
    },
    toggleTestMic() {
      this.micTesting = !this.micTesting;
      if (this.micTesting) {
        this.testMic();
      } else {
        this.stopTestMic();
      }
    },
    testMic() {
      if (!this.recordDeviceId) {
        this.$message.info('没有检测到麦克风');
        return;
      }
      window.rtcEngine.startRecordDeviceTest(
        this.recordDeviceId === 'default' && this.$IS_ELECTRON
          ? window.rtcEngine.getRecordDevice()
          : this.recordDeviceId,
        ({ audioLevel, micTest }) => {
          this.micAudioLevel = `${
            this.$IS_ELECTRON ? (audioLevel / 32767) * 100 : audioLevel * 300
          }%`;
          this.micTest = micTest;
        }
      );
    },
    stopTestMic() {
      window.rtcEngine.stopRecordDeviceTest(this.micTest);
      this.micTest = undefined;
    },
    changeMicVolume(value) {
      this.micVolume = value;
      window.rtcEngine.setAudioRecordVolume(value);
    },
    onPlayoutDeviceChange(deviceId) {
      window.rtcEngine.selectSpeaker(deviceId);
      this.$store.commit(UPDATE_SETTINGS, { playoutDeviceId: deviceId });
    },
    setSinkId() {
      if (this.speakerChangeable && this.playoutDeviceId) {
        const sinkId =
          this.playoutDeviceId === 'default' ? '' : this.playoutDeviceId;
        if (this.$refs.testAudio.sinkId !== sinkId) {
          this.$refs.testAudio.setSinkId(sinkId);
        }
      }
    },
    changeSpeakerVolume(value) {
      this.speakerVolume = value;
      window.rtcEngine.setAudioPlayoutVolume(value);
      if (!this.$IS_ELECTRON) {
        this.$refs.testAudio.volume = value;
        localStorage.setItem(localCacheKeyPlayoutVolume, value);
      }
    },
    switchAagc(checked) {
      this.$store.commit(UPDATE_SETTINGS, { enableAAGC: checked });
      window.rtcEngine.enableAAGC(checked);
    },
    toggleSpeakerTest() {
      this.speakerTesting = !this.speakerTesting;
      if (this.speakerTesting) {
        this.testSpeaker();
      } else {
        this.stopTestSpeaker();
      }
    },
    testSpeaker() {
      if (this.$IS_ELECTRON) {
        // 打包后的路径 app://./media/Ukulele-Song.42cddbe0.mp3，生成的目录是app，然后根据规律拼接而成，electron版本升级可能会失效
        const audioPath = path.join(
          window.process.resourcesPath,
          this.$refs.testAudio.getAttribute('src').replace('://', '/')
        );
        window.rtcEngine.startPlayoutDeviceTest(
          this.playoutDeviceId && this.$IS_ELECTRON
            ? window.rtcEngine.getPlayoutDevice()
            : this.playoutDeviceId,
          audioPath
        );
        const getPlayoutLevel = () => {
          this.speakerLevel = `${
            (window.rtcEngine.getTestPlayoutLevel() / 32767) * 100
          }%`;
          this.playoutTest = requestAnimationFrame(getPlayoutLevel);
        };
        getPlayoutLevel();
      } else {
        this.setSinkId();
        this.$refs.testAudio.play();
      }
    },
    stopTestSpeaker() {
      if (this.$IS_ELECTRON) {
        cancelAnimationFrame(this.playoutTest);
        this.playoutTest = undefined;
        window.rtcEngine.stopPlayoutDeviceTest();
      } else {
        this.$refs.testAudio.pause();
      }
    },
  },
};
</script>

<style lang="less" scoped>
ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.audio-setting {
  padding: 0 24px;
  > ul {
    padding-top: 30px;
    &:first-child {
      padding-bottom: 8px;
      border-bottom: 1px solid #ddd;
    }
    > li {
      display: flex;
      > label {
        width: 70px;
        padding-right: 8px;
        line-height: 32px;
        &::after {
          content: ':';
        }
      }
    }
  }
}
.device-select {
  margin-bottom: 8px;
  > div {
    flex: 1;
    overflow: hidden;
    margin-right: 10px;
  }
}

.device-level > div {
  flex: 1;
  padding-top: 13px;
  > span {
    display: block;
    height: 6px;
    border-radius: 3px;
    background-color: #dadada;
    overflow: hidden;
    position: relative;
    > i {
      position: absolute;
      left: 0;
      top: 0;
      height: 100%;
      border-radius: inherit;
      background-color: #0899f9;
    }
  }
}
.device-volume {
  > i {
    margin-right: 6px;
    font-size: 22px;
    line-height: 32px;
    color: #0899f9;
  }
  > div {
    flex: 1;
    margin-right: -5px;
  }
  /deep/ .ant-slider {
    padding: 3px 0;
  }
  /deep/ .ant-slider-handle {
    border-color: #0899f9;
  }
  /deep/ .ant-slider-rail {
    background-color: #dadada;
    height: 6px;
    border-radius: 3px;
  }
  /deep/ .ant-slider-track {
    background-color: #0899f9;
    height: 6px;
    border-radius: 3px;
  }
  /deep/ .ant-slider-step {
    height: 6px;
  }
  /deep/ .ant-slider-handle {
    margin-top: -4px;
  }
}
.audio-setting .aagc {
  > label {
    width: unset;
  }
  > div {
    padding-top: 5px;
  }
}
</style>
