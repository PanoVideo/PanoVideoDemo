<template>
  <el-dialog
    :visible="visible"
    center
    class="pano-setting-modal"
    @close="onClose"
  >
    <div class="setting_wrapper">
      <div class="video-view-wrapper">
        <div class="video_view" ref="view" />
      </div>
      <el-form>
        <el-form-item label="分辨率" label-position="left">
          <el-radio-group size="mini" v-model="selectedVideoProfile">
            <el-radio-button :label="2">360P</el-radio-button>
            <el-radio-button :label="3">720P</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="摄像头" label-position="left">
          <el-select size="small" v-model="selectedCamera" placeholder="请选择">
            <el-option
              v-for="item in cameras"
              :key="item.deviceId"
              :label="item.deviceName"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="麦克风" label-position="left">
          <el-select size="small" v-model="selectedMic" placeholder="请选择">
            <el-option
              v-for="item in mics"
              :key="item.deviceId"
              :label="item.deviceName"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="扬声器" label-position="left">
          <el-select size="small" v-model="selecteSpeaker" placeholder="请选择">
            <el-option
              v-for="item in speakers"
              :key="item.deviceId"
              :label="item.deviceName"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时长上限" label-position="left">
          <el-input size="small" style="width: 245px" value="90分钟" disabled />
        </el-form-item>

        <el-form-item label="人数上限" label-position="left">
          <el-input size="small" style="width: 245px" value="25人" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="mini" @click="showFeedback"
            >反馈与报障</el-button
          >
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import {
  VideoProfileType,
  VideoScalingMode
} from '@pano.video/panortc-electron-sdk';

export default {
  data() {
    return {
      selectedVideoProfile: VideoProfileType.HD720P,
      selectedCamera: '',
      cameras: [],
      selectedMic: 'default',
      mics: [],
      selecteSpeaker: 'default',
      speakers: []
    };
  },
  props: {
    visible: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    ...mapGetters([
      'myVideoProfileType',
      'micId',
      'userMe',
      'speakerId',
      'cameraId'
    ])
  },
  watch: {
    visible() {
      if (this.visible) {
        this.init();
      } else {
        window.rtcEngine.video.stopPreview(this.cameraId);
      }
    },
    selectedVideoProfile() {
      this.onCameraChange();
    },
    selectedCamera() {
      this.onCameraChange();
    },
    selectedMic() {
      window.rtcEngine.audio.setRecordDevice(this.selectedMic);
      this.setMic(this.selectedMic);
    },
    selecteSpeaker() {
      window.rtcEngine.audio.setPlayoutDevice(this.selecteSpeaker);
      this.setSpeaker(this.selecteSpeaker);
    }
  },
  methods: {
    ...mapMutations([
      'setMic',
      'setSpeaker',
      'setCamera',
      'setVideoProfileType'
    ]),
    onCameraChange() {
      window.rtcEngine.video.stopPreview(this.cameraId);
      window.rtcEngine.video.startPreview(
        this.selectedCamera,
        this.$refs.view,
        {
          profile: this.selectedVideoProfile,
          mirror: true
        }
      );
      this.setCamera(this.selectedCamera);
      this.setVideoProfileType(this.selectedVideoProfile);
      window.rtcEngine.video.setCaptureDevice(this.selectedCamera);
      if (!this.userMe.videoMuted) {
        window.rtcEngine.startVideo(this.userMe.videoDomRef, {
          profile: this.selectedVideoProfile,
          scaling: VideoScalingMode.Fit,
          mirror: true
        });
      }
    },
    onClose() {
      this.$emit('update:visible', false);
    },
    async init() {
      await this.$nextTick();
      this.selectedVideoProfile = this.myVideoProfileType;
      this.selectedCamera = this.cameraId;
      this.selecteSpeaker = this.speakerId;
      this.selectedMic = this.micId;
      this.cameras = window.rtcEngine.video.getCaptureDeviceList();
      window.rtcEngine.video.startPreview(
        this.selectedCamera,
        this.$refs.view,
        {
          profile: this.selectedVideoProfile,
          mirror: true
        }
      );
      this.mics = window.rtcEngine.audio.getRecordDeviceList();
      this.speakers = window.rtcEngine.audio.getPlayoutDeviceList();
    },
    showFeedback() {
      // TODO
    }
  },
  beforeDestroy() {
    window.rtcEngine.video.stopPreview(this.cameraId);
  }
};
</script>

<style lang="scss">
.pano-setting-modal {
  .el-dialog,
  .el-dialog__body,
  .setting_wrapper {
    min-width: 720px;
    max-width: 720px;
    background-color: #fff;
    border-radius: 5px;
  }
  .video-view-wrapper {
    border-top-left-radius: 5px;
    border-bottom-left-radius: 5px;
  }
  .el-dialog__body {
    padding: 0 !important;
  }
  .el-dialog__header {
    padding: 0;
  }
  .el-select {
    width: 260px !important;
  }
  .el-dialog__headerbtn {
    z-index: 100;
  }
  .el-form-item {
    margin-bottom: 10px;
  }
}
.setting_wrapper {
  display: flex;
  margin: 0 auto;
  user-select: none;
  > div {
    width: 50%;
    height: 380px;
    position: relative;
    overflow: hidden;
  }
  > form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 50%;
    padding: 0 16px;
    :global {
      .ant-form-item {
        margin-bottom: 10px;
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
.video_view {
  position: absolute;
  left: -50%;
  width: 680px;
  height: 100%;
  // margin-left: -340px;
  background-color: #f5f5f5;
  video {
    width: 100%;
    height: 100%;
    margin: auto;
    display: block;
    object-fit: cover;
  }
}
</style>
