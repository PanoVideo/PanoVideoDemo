<template>
  <div class="video-setting">
    <ul>
      <li class="fill-item">
        <label>选择设备</label>
        <div>
          <Select
            :value="captureDeviceId"
            @change="onCaptureDeviceChange"
            style="width: 100%"
          >
            <SelectOption
              v-for="item of cameraList"
              :key="item.deviceId"
              :value="item.deviceId"
            >
              {{ item.label }}
            </SelectOption>
          </Select>
        </div>
      </li>
      <li class="fill-item">
        <label>选择分辨率</label>
        <div class="raido-wrapper">
          <radio-group :value="videoProfileType" @change="resolutionChange">
            <radio
              v-for="item in videoProfiles"
              :key="item.value"
              :value="item.value"
              >{{ item.text }}</radio
            >
          </radio-group>
        </div>
      </li>
      <li v-if="$IS_ELECTRON" class="fill-item">
        <label>选择帧率</label>
        <div class="raido-wrapper">
          <radio-group :value="videoFrameRate" @change="videoFrameRateChange">
            <radio
              v-for="item in videoFrameRates"
              :key="item.value"
              :value="item.value"
              >{{ item.text }}</radio
            >
          </radio-group>
        </div>
      </li>
    </ul>
    <div class="video-mirror" ref="videoView"></div>
  </div>
</template>

<script>
import { Select, Radio } from 'ant-design-vue';
import { mapMutations, mapState } from 'vuex';
import { initVideoDeviceList } from '@/pano/index';
import { UPDATE_SETTINGS } from '../../store/mutations';

export default {
  components: {
    Select,
    SelectOption: Select.Option,
    Radio,
    RadioGroup: Radio.Group,
  },
  data() {
    return {
      videoProfiles: [
        {
          name: 'Low(180P)',
          text: '180P',
          value: VideoProfileType.Low,
        },
        {
          name: 'Standard(360P)',
          text: '360P',
          value: VideoProfileType.Standard,
        },
        {
          name: 'HD720P',
          text: '720P',
          value: VideoProfileType.HD720P,
        },
        {
          name: 'HD1080P',
          text: '1080P',
          value: VideoProfileType.HD1080P,
        },
      ],
      videoFrameRates: [
        {
          name: 'Low(15fps)',
          text: '15fps',
          value: 0,
        },
        {
          name: 'Standard(30fps)',
          text: '30fps',
          value: 1,
        },
      ],
      previewStarted: false,
    };
  },
  computed: {
    ...mapState({
      videoDeviceReady: (state) => state.settingStore.videoDeviceReady,
      captureDeviceId: (state) => state.settingStore.captureDeviceId,
      cameraList: (state) => state.settingStore.cameraList,
      videoProfileType: (state) => state.settingStore.videoProfileType,
      videoFrameRate: (state) => state.settingStore.videoFrameRate,
    }),
  },
  watch: {
    captureDeviceId(val, oldVal) {
      this.stopPreview(oldVal);
      this.startPreview();
      this.previewStarted = true;
    },
    videoDeviceReady() {
      // 本地存储有效，不会触发captureDeviceId的watch，做个补偿
      if (!this.previewStarted) {
        this.startPreview();
      }
    },
    videoProfileType() {
      if (!this.$IS_ELECTRON) {
        this.stopPreview(this.captureDeviceId);
        this.startPreview();
      }
    },
  },
  mounted() {
    if (this.videoDeviceReady) {
      this.startPreview();
    } else {
      initVideoDeviceList();
    }
  },
  beforeDestroy() {
    if (this.captureDeviceId) {
      this.stopPreview(this.captureDeviceId);
    }
  },
  methods: {
    ...mapMutations([UPDATE_SETTINGS]),
    onCaptureDeviceChange(deviceId) {
      window.rtcEngine.selectCam(deviceId);
      this.$store.commit(UPDATE_SETTINGS, { captureDeviceId: deviceId });
    },
    resolutionChange(e) {
      this.$store.commit(UPDATE_SETTINGS, { videoProfileType: e.target.value });
    },
    videoFrameRateChange(e) {
      window.rtcEngine.setVideoFrameRate(e.target.value);
      this.$store.commit(UPDATE_SETTINGS, { videoFrameRate: e.target.value });
    },
    startPreview() {
      if (!this.captureDeviceId) {
        this.$message.info('没有检测到摄像头');
        return;
      }
      window.rtcEngine.startPreview(
        this.captureDeviceId,
        this.onPreviewSuccess,
        this.onPreviewFailed,
        this.videoProfileType,
        this.$refs.videoView
      );
    },
    onPreviewSuccess(videoTag) {
      videoTag.setAttribute(
        'style',
        'width: 100%; height: 100%; margin: auto; display: block; object-fit: cover;'
      );
      videoTag.autoplay = true;
      this.$refs.videoView.appendChild(videoTag);
      videoTag.play && videoTag.play();
    },
    onPreviewFailed(e) {
      console.error('startPreview failed', e);
    },
    stopPreview(deviceId) {
      if (this.$IS_ELECTRON) {
        window.rtcEngine.stopPreview(undefined, deviceId);
        return;
      }
      const elem = this.$refs.videoView.getElementsByTagName('video')[0];
      if (elem) {
        window.rtcEngine.stopPreview(elem, deviceId);
        elem.parentNode.removeChild(elem);
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
.fill-item {
  overflow: hidden;
  &:first-child {
    margin-bottom: 4px;
  }
  > label {
    float: left;
    width: 87px;
    padding-right: 8px;
    line-height: 32px;
    &::after {
      content: ':';
    }
  }
  > div {
    margin-left: 87px;
  }
}
.video-setting {
  display: flex;
  flex-direction: column;
  padding: 16px 24px;
  height: 100%;
  > ul {
    padding-bottom: 6px;
    margin-bottom: 10px;
  }
  > div {
    flex: 1;
    max-height: 271px;
    overflow: hidden;
    border-radius: 2px;
    background-color: rgba(63, 64, 65, 0.9);
  }
}
.raido-wrapper {
  padding: 5px 0 6px;
}
</style>
