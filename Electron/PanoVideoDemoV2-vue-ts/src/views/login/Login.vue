<template>
  <div v-if="step === 0">
    <div class="main">
      <div v-if="$IS_ELECTRON" class="logo">
        <img alt="logo" src="../../assets/img/logo.png" />
      </div>
      <div v-else class="logo">
        <a
          href="https://www.pano.video"
          target="_blank"
          :style="{ opacity: 1 }"
          rel="noreferrer"
        >
          <img alt="logo" src="../../assets/img/logo.png" />
        </a>
      </div>
      <a-form :form="form" @submit.prevent="goJoin" class="tableListForm">
        <a-form-item label="AppId" v-if="!pocEnabled">
          <a-input
            name="appId"
            v-decorator="[
              'appId',
              {
                initialValue: lastAppId,
                rules: [
                  {
                    required: true,
                    message: '请输入用户AppId',
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-form-item label="Token" v-if="!pocEnabled">
          <a-input
            name="token"
            v-decorator="[
              'token',
              {
                initialValue: lastToken,
                rules: [
                  {
                    required: true,
                    message: '请输入用户Token',
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-form-item label="房间号">
          <a-input
            name="channelId"
            v-decorator="[
              'channelId',
              {
                initialValue: lastChannelId,
                rules: [
                  {
                    required: true,
                    message: '请输入不超过20位的房间号(只允许字母和数字)',
                  },
                  {
                    pattern: /^[0-9a-zA-Z]{1,20}$/,
                    message: '请输入不超过20位的房间号(只允许字母和数字)',
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            name="username"
            v-decorator="[
              'username',
              {
                initialValue: lastUserName,
                rules: [
                  {
                    required: true,
                    message: '请输入用户名!',
                  },
                  {
                    pattern: /^.{1,12}$/,
                    message: '用户名最多输入12个字符!',
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input
            name="userId"
            v-decorator="[
              'userId',
              {
                initialValue: lastUserId,
                rules: [
                  {
                    required: true,
                    message: '请输入用户ID!',
                  },
                ],
              },
            ]"
          />
        </a-form-item>
        <a-row v-if="$IS_ELECTRON" :style="{ marginTop: '20px' }">
          <a-col :span="8">
            <a-checkbox :checked="!autoMuteAudio" @change="setAudioOpen">
              开启麦克风
            </a-checkbox>
          </a-col>
          <a-col :span="8">
            <a-checkbox :checked="autoOpenVideo" @change="setVideoOpen">
              开启摄像头
            </a-checkbox>
          </a-col>
          <a-col span="2">
            <a-button @click="openSetting" type="link">
              <a-icon type="setting" :style="{ color: '#888' }" />
            </a-button>
          </a-col>
        </a-row>
        <div class="join-submit">
          <a-button
            class="submit"
            type="primary"
            html-type="submit"
            :loading="joinLoading"
          >
            立即加入
          </a-button>
          <Tooltip placement="top" title="私有化设置" class="poc-setting">
            <a-icon type="setting" @click="togglePoc" />
          </Tooltip>
        </div>
      </a-form>
      <Setting
        v-if="$IS_ELECTRON"
        :visible="settingShow"
        @close="closeSetting"
      />
      <PocSetting
        :visible="pocSetingVisible"
        @setPocConfig="setPocConfig"
        @cancel="togglePoc"
      />
    </div>
    <WindowFrame v-if="$IS_ELECTRON" :maxBtnEnable="false" />
  </div>
  <DeviceStatus
    v-else-if="!$IS_ELECTRON"
    :audioOpen="!autoMuteAudio"
    :videoOpen="autoOpenVideo"
    :joinLoading="joinLoading"
    @setAudioOpen="setAudioOpen"
    @setVideoOpen="setVideoOpen"
    @join="joinChannel"
  />
</template>

<script>
import {
  Button,
  Form,
  Input,
  Checkbox,
  Row,
  Col,
  Icon,
  Tooltip,
} from 'ant-design-vue';
import { mapGetters } from 'vuex';
import {
  localCacheKeyOpenMicAtStart,
  localCacheKeyOpenCamAtStart,
} from '../../constants';
import { rtcEngineJoinChannel } from '@/pano';
import { MeetingStatus } from '@/store/modules/meeting';
import Setting from '@/components/setting/index.vue';
import * as mutations from '@/store/mutations';
import LoginMixin from '../login/Login.mixin';
import DeviceStatus from '@/components/DeviceStatus.vue';
import { isMobile, SAFARI_15_1, SAFARI_15_1_MESSAGE } from '@/utils/common';
import WindowFrame from '@/components/WindowFrame.vue';
import PocSetting from '@/components/PocSetting.vue';
import { RtcMessage, RtsService } from '@pano.video/panorts';
import { genPocToken } from '@/utils';
import { LogUtil } from '@/utils';

const LS_KEY_USERNAME = 'LS_KEY_USERNAME';
const LS_KEY_CHANNELID = 'LS_KEY_CHANNELID';
const LS_KEY_APPID = 'LS_KEY_APPID';
const LS_KEY_TOKEN = 'LS_KEY_TOKEN';
const LS_KEY_USERID = 'LS_KEY_USERID';
const LS_KEY_POC = 'LS_KEY_POC';

export default {
  mixins: window.IS_ELECTRON ? [LoginMixin] : [],
  data() {
    return {
      pocEnabled: false,
      pocSetingVisible: false,
      pocConfig: { serverAddr: '', appSecret: '', appId: '' },
      joinLoading: false,
      form: this.$form.createForm(this, { name: 'login' }),
      settingShow: false,
      msJoinSuccess: false,
      channelId: null,
      userName: null,
      appId: null,
      token: null,
      userId: null,
      joinParams: null,
      step: 0,
      lastChannelId:
        this.$route.params.channelId ||
        localStorage.getItem(LS_KEY_CHANNELID) ||
        '',
      lastUserName: localStorage.getItem(LS_KEY_USERNAME) || '',
      lastAppId: localStorage.getItem(LS_KEY_APPID) || '',
      lastToken: localStorage.getItem(LS_KEY_TOKEN) || '',
      lastUserId: localStorage.getItem(LS_KEY_USERID) || '',
    };
  },
  components: {
    'a-form': Form,
    'a-input': Input,
    'a-button': Button,
    'a-form-item': Form.Item,
    'a-checkbox': Checkbox,
    'a-row': Row,
    'a-col': Col,
    'a-icon': Icon,
    Tooltip,
    Setting,
    DeviceStatus,
    WindowFrame,
    PocSetting,
  },
  computed: {
    ...mapGetters(['autoMuteAudio', 'autoOpenVideo']),
  },
  methods: {
    togglePoc() {
      this.pocSetingVisible = !this.pocSetingVisible;
    },
    setPocConfig(serverAddr, appSecret, appId) {
      console.log('setPocConfig', serverAddr, appSecret, appId);
      this.pocEnabled = true;
      this.pocSetingVisible = false;
      this.pocConfig = { serverAddr, appSecret, appId };
      this.$message.success('私有化设置配置成功');
      localStorage.setItem(LS_KEY_POC, JSON.stringify(this.pocConfig));
    },
    setAudioOpen(value) {
      value = typeof value === 'boolean' ? value : value.target.checked;
      this.$store.commit(mutations.SET_MUTE_AUDIO_JOIN, !value);
      localStorage.setItem(localCacheKeyOpenMicAtStart, value ? 'yes' : 'no');
    },
    setVideoOpen(value) {
      value = typeof value === 'boolean' ? value : value.target.checked;
      this.$store.commit(mutations.SET_OPEN_VIDEO_JOIN, value);
      localStorage.setItem(localCacheKeyOpenCamAtStart, value ? 'yes' : 'no');
    },
    goJoin() {
      this.form.validateFields((err, values) => {
        if (err) return;
        this.channelId = values.channelId;
        this.userName = values.username;
        this.userId = values.userId;
        if (this.pocEnabled) {
          this.appId = this.pocConfig.appId;
          this.token = genPocToken(
            this.pocConfig.appId,
            this.channelId,
            this.userId,
            this.pocConfig.appSecret
          );
          window.RtcEngine.setServer(this.pocConfig.serverAddr);
          RtsService.setServer(this.pocConfig.serverAddr);
        } else {
          this.appId = values.appId;
          this.token = values.token;
        }
        localStorage.setItem(LS_KEY_USERNAME, this.userName);
        localStorage.setItem(LS_KEY_CHANNELID, this.channelId);
        localStorage.setItem(LS_KEY_APPID, this.appId);
        localStorage.setItem(LS_KEY_TOKEN, this.token);
        localStorage.setItem(LS_KEY_USERID, this.userId);
        if (this.$IS_ELECTRON) {
          this.joinChannel();
        } else {
          this.step = 2;
        }
      });
    },
    async joinChannel() {
      this.joinLoading = true;
      const { userName, userId, appId, channelId, token } = this;
      this.$store.commit(mutations.UPDATE_USER_ME, { userId, userName });
      this.$store.commit(mutations.SET_CHANNELID, channelId);
      this.$store.commit(mutations.SET_MEETING_STATUS, {
        meetingStatus: MeetingStatus.connected,
      });
      this.$store.commit(mutations.UPDATE_USER_ME_DOM_ID);
      const joinParams = {
        appId,
        channelId,
        userId,
        token,
        userName,
        channelMode: 1,
        subscribeAudioAll: true,
      };
      rtcEngineJoinChannel(joinParams, {
        joinChannelType: JoinChannelType.mediaOnly,
      })
        .then(() => {
          this.joinLoading = false;
          this.$router.replace({ path: '/Meeting' });
          this.joinRtcWhiteboard(joinParams);
          console.log('joinParams', joinParams);
        })
        .catch((message) => {
          this.joinLoading = false;
          this.$Modal.destroyAll();
          this.$Modal.info({
            title: '提示',
            content: `加入会议失败，因为${message}`,
          });
          this.$store.commit(mutations.RESET_MEETING_STORE);
        });
    },
    joinRtcWhiteboard(joinParams) {
      window.rtcWhiteboard
        .joinChannel({
          ...joinParams,
          name: joinParams.userName,
          autoJoinWhiteboardSession: false,
        })
        .then(() => {
          LogUtil('rtcWhiteboard joinChannel success');
          // 主动连接 RtcMessage
          RtcMessage.getInstance().once(RtcMessage.Events.sessionReady, () => {
            LogUtil('RtcMessage sessionReady');
          });
          RtcMessage.getInstance().joinSession();
        })
        .catch((message) => {
          console.error('rtcWhiteboard joinChannel failed', message);
          window.rtcEngine.leaveChannel();
          this.$Modal.destroyAll();
          this.$Modal.info({
            title: '提示',
            content: `因为无法连接到RtsService: ${message}，点击确认返回登录`,
            onOk: () => {
              window.onbeforeunload = null;
              window.rtcEngine.leaveChannel();
              this.$router.replace({ name: 'Login' });
              this.$store.commit(mutations.RESET_MEETING_STORE);
            },
          });
          this.$store.commit(mutations.RESET_MEETING_STORE);
        });
    },
    openSetting() {
      this.settingShow = true;
    },
    closeSetting() {
      this.settingShow = false;
    },
  },
  mounted() {
    this.$message.destroy();
    this.$store.commit(mutations.RESET_USER_STORE);
    this.$store.commit(mutations.RESET_MEETING_STORE);
    this.$store.commit(mutations.RESET_WHITEBOARD_STORE);
    const pocConfig = localStorage.getItem(LS_KEY_POC);
    if (pocConfig) {
      try {
        this.pocConfig = JSON.parse(pocConfig);
        this.pocEnabled = true;
      } catch (error) {
        console.error(error);
      }
    }

    if (isMobile()) {
      this.$Modal.confirm({
        icon: null,
        title: '您正在使用移动端访问，是否跳转到移动版？',
        cancelText: '留在当前',
        okText: '跳转',
        onOk() {
          const urlPrefix =
            location.hostname === 'localhost' ||
            !/[a-zA-Z]/.test(location.hostname)
              ? '//demo.dev.pano.video'
              : '';
          const matcher = location.hash.match(/[?&]channelId=([^&#]+)/);
          location.replace(
            urlPrefix +
              '/pvch5/#/' +
              (matcher ? '?channelId=' + matcher[1] : '')
          );
        },
      });
    } else if (SAFARI_15_1) {
      this.$Modal.info({
        title: '提示',
        content: SAFARI_15_1_MESSAGE.content,
      });
    } else if (!window.rtcEngine.checkEnvRequirement()) {
      this.$Modal.info({
        title: '提示',
        content: `当前浏览器可能无法使用PanoVideoCall，建议使用Chrome浏览器（主版本号不小于69）`,
      });
      return;
    }
  },
};
</script>

<style lang="less" scoped>
@import '~ant-design-vue/es/style/themes/default.less';

.main {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 368px;
  margin: auto;
  user-select: none;
  @media screen and (max-width: @screen-xs) {
    width: 95%;
  }
  .ant-col {
    line-height: 32px !important;
    height: 32px !important;
  }
}

.main input {
  user-select: all;
}

.logo {
  text-align: center;
  margin-bottom: 20px;
  img {
    width: 260px;
  }
}

.tableListForm {
  /deep/ .ant-form-item {
    margin-right: 0;
    margin-bottom: 5px;
  }
  /deep/ .ant-form-item-control-wrapper {
    flex: 1;
  }
}
.join-submit {
  display: flex;
  margin-top: 24px;
  align-items: center;

  button {
    flex: 1;
  }
  .poc-setting {
    width: 40px;
    margin-left: 10px;
    cursor: pointer;
  }
}
</style>
