<template>
  <el-dialog
    :visible="settingVisible"
    center
    class="pano-setting-modal"
    destroy-on-close
    @close="onClose"
  >
    <div class="setting_wrapper">
      <div class="video-view-wrapper">
        <div class="video_view" ref="view" />
      </div>
      <el-form>
        <el-form-item label="分辨率" label-position="left">
          <el-radio-group v-model="selectedVideoProfile">
            <el-radio-button label="Standard">360P</el-radio-button>
            <el-radio-button label="HD720P">720P</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="摄像头" label-position="left">
          <el-select v-model="camera" placeholder="请选择">
            <el-option
              v-for="item in cameras"
              :key="item.deviceId"
              :label="item.label"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="麦克风" label-position="left">
          <el-select v-model="mic" placeholder="请选择">
            <el-option
              v-for="item in mics"
              :key="item.deviceId"
              :label="item.label"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="扬声器" label-position="left">
          <el-select v-model="speaker" placeholder="请选择">
            <el-option
              v-for="item in speakers"
              :key="item.deviceId"
              :label="item.label"
              :value="item.deviceId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时长上限" label-position="left">
          <el-input style="width: 245px" value="90分钟" disabled />
        </el-form-item>

        <el-form-item label="人数上限" label-position="left">
          <el-input style="width: 245px" value="25人" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="small" @click="showFeedback"
            >反馈与报障</el-button
          >
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import * as Constants from '../constants';

export default {
  data() {
    return {
      selectedVideoProfile: Constants.VideoProfileType.HD720P,
      camera: '',
      cameras: [],
      mic: 'default',
      mics: [],
      speaker: 'default',
      speakers: [],
      videoTag: undefined
    };
  },
  computed: {
    ...mapGetters([
      'settingVisible',
      'videoPorfile',
      'micId',
      'speakerId',
      'cameraId'
    ])
  },
  watch: {
    settingVisible() {
      if (this.settingVisible) {
        this.init();
      } else {
        this.videoTag && window.rtcEngine.stopPreview(this.videoTag);
      }
    },
    selectedVideoProfile() {
      this.onCameraChange();
    },
    camera() {
      this.onCameraChange();
    },
    mic() {
      window.rtcEngine.selectMic(this.mic);
      this.setMic(this.mic);
    },
    speaker() {
      window.rtcEngine.selectSpeaker(this.speaker);
      this.setSpeaker(this.speaker);
    }
  },
  methods: {
    ...mapMutations([
      'setSettingVisible',
      'setMic',
      'setSpeaker',
      'setCamera',
      'setVideoProfile'
    ]),
    onCameraChange() {
      this.videoTag && window.rtcEngine.stopPreview(this.videoTag);
      window.rtcEngine.startPreview(
        this.camera,
        this.onPreviewSuccess,
        this.onPreviewFailed,
        this.selectedVideoProfile
      );
      this.setCamera(this.camera);
      this.setVideoProfile(this.selectedVideoProfile);
      window.rtcEngine.selectCam(this.camera);
    },
    onPreviewSuccess(videoTag) {
      const wrapper = this.$refs.view;
      this.videoTag = videoTag;
      videoTag.autoplay = true;
      if (
        wrapper.firstChild === videoTag &&
        wrapper.firstChild.srcObject === videoTag.srcObject
      ) {
        videoTag.play && videoTag.play();
      } else {
        videoTag.play && videoTag.play();
        wrapper.innerHTML = '';
        wrapper.appendChild(videoTag);
      }
    },
    onPreviewFailed(e) {
      console.error('startPreview failed', e);
    },
    onClose() {
      this.setSettingVisible(false);
    },
    init() {
      this.selectedVideoProfile = this.videoPorfile;
      this.camera = this.cameraId;
      this.speaker = this.speakerId;
      this.mic = this.micId;
      window.rtcEngine.getCams(cameras => {
        this.cameras = cameras;
        window.rtcEngine.startPreview(
          this.camera,
          this.onPreviewSuccess,
          this.onPreviewFailed,
          this.selectedVideoProfile
        );
      });
      window.rtcEngine.getMics(mics => {
        this.mics = mics;
      });
      window.rtcEngine.getSpeakers(speakers => {
        this.speakers = speakers;
      });
    },
    showFeedback() {
      // TODO
    }
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
  // position: absolute;
  left: 50%;
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
