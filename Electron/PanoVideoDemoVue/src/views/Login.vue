<template>
  <div class="main">
    <div class="logo">
      <img alt="logo" :src="logopng" />
    </div>
    <el-form class="tableListForm" :model="form" v-loading="loading">
      <el-form-item label="AppId" key="appid" required>
        <el-input v-model="form.appId" />
      </el-form-item>
      <el-form-item label="Token" key="token" required>
        <el-input v-model="form.token" />
      </el-form-item>
      <el-form-item label="房间号" key="channelId" required>
        <el-input v-model="form.channelId" />
      </el-form-item>
      <el-form-item label="用户名" key="userName">
        <el-input v-model="form.userName" />
      </el-form-item>
      <el-form-item label="用户Id" key="userId" required>
        <el-input v-model="form.userId" />
      </el-form-item>
      <el-row>
        <el-col :span="9">
          <el-form-item label="开启麦克风" key="audioOn">
            <el-checkbox v-model="form.audioOn" />
          </el-form-item>
        </el-col>
        <el-col :span="9">
          <el-form-item label="开启摄像头" key="videoOn">
            <el-checkbox v-model="form.videoOn" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <i class="el-icon-setting" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button
          style="width: 100%;"
          type="primary"
          @click="joinChannel"
          round
        >
          加入通话
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import logopng from '@/assets/img/logo.png';
import { mapMutations, mapGetters } from 'vuex';
import { RtcEngineEvents, getLeaveChannelReason } from '@/setup-panortc';
import * as Constants from '@/constants';
import {
  ChannelMode,
  kChannelServiceMedia,
  AudioAecType,
  QResult,
  VideoScalingMode
} from '@pano.video/panortc-electron-sdk';

export default {
  data() {
    return {
      logopng,
      loading: false,
      form: {
        channelId: '',
        userName: '',
        audioOn: true,
        videoOn: true,
        appId: '',
        userId: '',
        token: ''
      }
    };
  },
  computed: {
    ...mapGetters(['userMe', 'myVideoProfileType'])
  },
  methods: {
    ...mapMutations([
      'updateChannelId',
      'updateUserMe',
      'setMeetingStatus',
      'resetMeetingStore'
    ]),
    initWhiteboard() {},
    /**
     * 加会逻辑
     */
    async joinChannel() {
      const { channelId, userName, appId, userId, token } = this.form;
      if (!channelId || !appId || !userId || !token) {
        this.$message.warning('请填写必要的参数再加入会议');
        return;
      }
      localStorage.setItem(Constants.localCacheKeyUserName, userName);
      localStorage.setItem(Constants.localCacheKeyChannelId, channelId);
      this.loading = true;
      this.updateChannelId(channelId);
      this.updateUserMe({
        userName,
        userId
      });
      console.log('joinChannel...');
      window.rtcEngine.initialize(appId, {
        videoHwAccel: false,
        audioAecType: AudioAecType.Default,
        audioScenario: 0
      });
      window.rtcEngine.joinChannel(token, channelId, userId, {
        channelMode: ChannelMode.Mode_Meeting,
        serviceFlags: kChannelServiceMedia,
        subscribeAudioAll: true, // subscribe user audio automatically
        userName
      });
      window.rtcWhiteboard.joinChannel(
        {
          appId,
          token,
          channelId,
          name: userName,
          userId
        },
        () => {},
        () => {}
      );
      this.resetMeetingStore();
    },
    /**
     * 入会成功回调
     */
    onJoinChannelConfirm(result) {
      this.loading = false;
      const { audioOn, videoOn } = this.form;
      if (result === QResult.OK) {
        if (audioOn) {
          window.rtcEngine.startAudio();
        }
        if (videoOn) {
          window.rtcEngine.startVideo(this.userMe.videoDomRef, {
            profile: this.myVideoProfileType,
            scaling: VideoScalingMode.Fit,
            mirror: true
          });
        }
        localStorage.setItem(
          Constants.localCacheKeyMuteMicAtStart,
          audioOn ? 'no' : 'yes'
        );
        localStorage.setItem(
          Constants.localCacheKeyMuteCamAtStart,
          videoOn ? 'no' : 'yes'
        );
        this.updateUserMe({ audioMuted: !audioOn, videoMuted: !videoOn });
        this.setMeetingStatus('connected');
        this.$router.replace({ name: 'Meeting' });
      } else {
        this.$message.error(
          'joinChannel 失败, 失败原因：' + getLeaveChannelReason(result)
        );
      }
    }
  },
  created() {
    this.form.audioOn = !this.userMe.audioMuted;
    this.form.videoOn = !this.userMe.videoMuted;
    window.rtcEngine.on(
      RtcEngineEvents.channelJoinConfirm,
      this.onJoinChannelConfirm
    );
  },
  beforeDestroy() {
    window.rtcEngine.off(
      RtcEngineEvents.channelJoinConfirm,
      this.onJoinChannelConfirm
    );
  }
};
</script>

<style lang="scss">
.main {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 368px;
  margin: auto;
  padding-top: 30px;
  user-select: none;
  @media screen and (max-width: 720px) {
    width: 95%;
  }

  .el-form-item {
    margin-bottom: 0;
  }

  .icon {
    margin-left: 16px;
    color: rgba(0, 0, 0, 0.2);
    font-size: 24px;
    vertical-align: middle;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: #409eff;
    }
  }
  .submit {
    width: 100%;
  }
  .other {
    margin-top: 24px;
    line-height: 22px;
    text-align: left;

    .register {
      float: right;
    }
  }

  :global {
    .antd-pro-login-submit {
      width: 100%;
      margin-top: 24px;
    }
  }
}

.main input {
  user-select: all;
}

.logo {
  text-align: center;
  margin-bottom: 20px;
}

.tableListForm {
  :global {
    .ant-form-item {
      display: flex;
      // flex-direction: row;
      margin-right: 0;
      margin-bottom: 5px;
      > .ant-form-item-label {
        width: auto;
        padding: 0 8px 0 0;
        line-height: 32px;
        // text-align: right;
      }
      .ant-form-item-control {
        line-height: 32px;
      }
    }
    .ant-form-item-control-wrapper {
      flex: 1;
    }
  }
}

.download {
  display: flex;
  margin-top: 25px;
  color: rgba(0, 0, 0, 0.85);
  .downloadImg {
    width: 100px;
    height: 100px;
  }
  .downloadlinks {
    flex: 1;
    padding: 0 10px;
    .downloadlinkTitle {
      h4 {
        font-size: 14px;
        text-align: center;
        font-weight: normal;
      }
    }
    .downloadlinkBlocks {
      margin-top: 20px;
      display: flex;
      a {
        flex: 1;
        .downloadlinkWrapper {
          display: flex;
          justify-content: center;
          flex-direction: column;
          align-items: center;
          color: rgba(0, 0, 0, 0.85);
          .downloadIcon {
            font-size: 26px;
          }
          .downloadlinkLabels {
            margin-left: 5px;
          }
          .downloadlinkPlatform {
            font-size: 14px;
          }
        }
      }
      .downloadlinkWithQrcode {
        position: relative;
        &::after {
          content: '';
          position: absolute;
          top: 100%;
          left: 50%;
          transform: translate(-50%, -5px);
          background-image: url('~@/assets/img/qrcode.png');
          width: 70px;
          height: 70px;
          background-size: contain;
          background-repeat: no-repeat;
          opacity: 0;
          transition: all 0.3s ease-in-out;
        }
        &:hover {
          &::after {
            opacity: 1;
            transform: translate(-50%, 5px);
          }
        }
      }
    }
  }
}
</style>
