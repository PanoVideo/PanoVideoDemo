<template>
  <div
    @mouseenter="onMouseEnterMenu"
    @mouseleave="onMouseLeaveMenu"
    :class="{
      toolbar: true,
      'toolbar--hidden': toolWrapperHidden,
    }"
  >
    <button class="toolbar__btn" @click="$emit('onClickMicMute')">
      <div class="toolbar__btn__icon">
        <UserAudio
          :fontSize="26"
          :userId="userMe.userId"
          :audioMuted="userMe.audioMuted"
          color="#ddd"
        />
      </div>
      <span class="toolbar__btn__label">
        {{ userMe.audioMuted ? '取消静音' : '静音' }}
      </span>
      <a-dropdown
        :trigger="['click']"
        placement="topLeft"
        v-model="micListVisible"
      >
        <template slot="overlay">
          <div class="toolbar__btn__more-panel-wrapper">
            <div
              :class="[
                'toolbar__btn__more-panel',
                {
                  'not-allowed': micAllowed === false,
                },
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
                <div class="list-divider" />
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
          </div>
        </template>
        <div class="toolbar__btn__more-trigger" @click.stop="">
          <i class="iconfont icon-arrowup" />
        </div>
      </a-dropdown>
    </button>
    <button class="toolbar__btn" @click="$emit('onClickCamMute')">
      <i
        :class="[
          'iconfont toolbar__btn__icon',
          userMe.videoMuted ? 'icon-video-off' : 'icon-video',
        ]"
        :style="userMe.videoMuted ? { color: 'red' } : {}"
      />
      <span class="toolbar__btn__label">
        {{ userMe.videoMuted ? '打开视频' : '关闭视频' }}
      </span>
      <a-dropdown
        :trigger="['click']"
        placement="topLeft"
        v-model="cameraListVisible"
      >
        <template slot="overlay">
          <div class="toolbar__btn__more-panel-wrapper">
            <div
              :class="[
                'toolbar__btn__more-panel',
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
          </div>
        </template>
        <div class="toolbar__btn__more-trigger" @click.stop="">
          <i class="iconfont icon-arrowup" />
        </div>
      </a-dropdown>
    </button>
    <button
      v-if="isHost"
      :class="{
        toolbar__btn: true,
        'toolbar__btn--active': isWhiteboardOpen,
      }"
      @click="toggleWhiteboard"
    >
      <i class="iconfont toolbar__btn__icon icon-whiteboard" />
      <span class="toolbar__btn__label">
        {{ isWhiteboardOpen ? '关闭白板' : '打开白板' }}
      </span>
    </button>
    <button
      :class="{
        toolbar__btn: true,
        'toolbar__btn--active': userMe.screenOpen,
      }"
      @click="toggleShareScreen"
    >
      <i class="iconfont toolbar__btn__icon icon-share-screen" />
      <span class="toolbar__btn__label">
        {{ userMe.screenOpen ? '正在共享' : '共享屏幕' }}
      </span>
      <a-dropdown
        :trigger="['click']"
        placement="topLeft"
        v-if="isHost"
        v-model="screenShareSettingVisible"
      >
        <template slot="overlay">
          <div class="toolbar__btn__more-panel-wrapper">
            <div class="toolbar__btn__more-panel">
              <label class="camera">
                <i class="iconfont icon-share-setting" />
                共享设置
              </label>
              <ul>
                <li
                  v-for="mode in shareModes"
                  :key="mode.value"
                  :class="{ selected: screenShareMode === mode.value }"
                  @click="setScreenShareMode(mode.value)"
                >
                  {{ mode.label }}
                </li>
              </ul>
            </div>
          </div>
        </template>
        <div class="toolbar__btn__more-trigger" @click.stop="">
          <i class="iconfont icon-arrowup" />
        </div>
      </a-dropdown>
    </button>
    <button class="toolbar__btn" @click="$emit('onClickRoster')">
      <i class="iconfont toolbar__btn__icon icon-user-list" />
      <span class="toolbar__btn__label">用户列表</span>
      <!-- <a-dropdown
        :trigger="['click']"
        placement="topLeft"
        v-model="invitePanelVisible"
      >
        <div slot="overlay" class="toolbar__btn__more-panel-wrapper">
          <div class="toolbar__btn__more-panel">
            <div class="invte-user">
              <div @click.stop="$emit('onClickInvite')" class="invte-user__btn">
                <i class="iconfont icon-invite" />
                <span>邀请</span>
              </div>
            </div>
          </div>
        </div>
        <div class="toolbar__btn__more-trigger" @click.stop="">
          <i class="iconfont icon-arrowup" />
        </div>
      </a-dropdown> -->
    </button>
    <button class="toolbar__btn" @click="$emit('openSetting')">
      <i class="iconfont toolbar__btn__icon icon-setting-fill" />
      <span class="toolbar__btn__label">设置</span>
    </button>
    <button class="toolbar__btn" @click="$emit('onClickExit')">
      <i class="iconfont toolbar__btn__icon icon-exit" />
      <span class="toolbar__btn__label">退出</span>
    </button>
  </div>
</template>

<script>
import { Dropdown, Modal } from 'ant-design-vue';
import { mapGetters, mapState } from 'vuex';
import UAParser from 'ua-parser-js';
import UserAudio from '@/components/audio/UserAudio';
import { initAudioDeviceList, initVideoDeviceList } from '@/pano';
import * as mutations from '@/store/mutations';
import * as actions from '@/store/actions';
import { RtcMessage } from '@pano.video/panorts';
import { PROP_SETTING } from '@/constants';

const browser = new UAParser().getBrowser();

export default {
  props: {
    showToolbar: Boolean,
  },
  data() {
    return {
      isMouseOnToolbar: false,
      isChrome: browser.name === 'Chrome',
      micListVisible: false,
      cameraListVisible: false,
      screenShareSettingVisible: false,
      invitePanelVisible: false,
      hostPanelVisible: false,
    };
  },
  components: {
    'a-dropdown': Dropdown,
    UserAudio,
  },
  computed: {
    ...mapState({
      audioDeviceReady: (state) => state.settingStore.audioDeviceReady,
      videoDeviceReady: (state) => state.settingStore.videoDeviceReady,
      cameraList: (state) => state.settingStore.cameraList,
      recordDeviceId: (state) => state.settingStore.recordDeviceId,
      playoutDeviceId: (state) => state.settingStore.playoutDeviceId,
      captureDeviceId: (state) => state.settingStore.captureDeviceId,
      micAllowed: (state) => state.settingStore.micAllowed,
      cameraAllowed: (state) => state.settingStore.cameraAllowed,
    }),
    ...mapGetters([
      'userMe',
      'isWhiteboardOpen',
      'micList',
      'speakerList',
      'isHost',
      'screenShareMode',
      'userList',
      'hostUser',
      'isHost',
      'screenSharingUser',
      'videoAnnotationUser',
    ]),
    toolWrapperHidden() {
      return !(this.showToolbar || this.isMouseOnToolbar);
    },
    shareModes() {
      return [
        {
          value: 1,
          label: '仅主持人可以共享',
          selected: this.screenShareMode === 1,
        },
        {
          value: 0,
          label: '每位参会者都可共享',
          selected: this.screenShareMode === 0,
        },
      ];
    },
  },
  methods: {
    hidePopover() {
      this.micListVisible = false;
      this.cameraListVisible = false;
      this.screenShareSettingVisible = false;
      this.invitePanelVisible = false;
      this.hostPanelVisible = false;
    },
    toggleWhiteboard() {
      if (this.videoAnnotationUser) {
        Modal.confirm({
          title: '打开白板',
          content: `这将中断对${this.videoAnnotationUser.userName}的视频标注，是否继续`,
          onOk: () => {
            this.$store.dispatch(
              actions.STOP_VIDEO_ANNOTATION,
              this.videoAnnotationUser.userId
            );
            this.$emit('onClickWhiteboard');
          },
          okText: '确定',
          cancelText: '取消',
        });
      } else if (this.screenSharingUser) {
        Modal.confirm({
          title: '打开白板',
          content:
            this.screenSharingUser === this.userMe
              ? '打开白板将中断您的屏幕共享，是否继续'
              : `这将中断 ${this.screenSharingUser.userName} 的共享，是否继续`,
          onOk: () => {
            this.$emit('onClickWhiteboard');
          },
          okText: '确定',
          cancelText: '取消',
        });
      } else {
        this.$emit('onClickWhiteboard');
      }
    },
    toggleShareScreen() {
      if (!this.userMe.screenOpen) {
        if (
          this.isHost &&
          (this.screenSharingUser ||
            this.videoAnnotationUser ||
            this.isWhiteboardOpen)
        ) {
          const content = this.screenSharingUser
            ? `这将中断 ${this.screenSharingUser.userName} 的共享，是否继续`
            : this.videoAnnotationUser
            ? `这将中断 ${this.videoAnnotationUser.userName} 的视频标注，是否继续`
            : `打开屏幕共享将关闭白板，是否继续`;
          Modal.confirm({
            title: '开始共享',
            content,
            onOk: () => {
              if (this.videoAnnotationUser) {
                this.$store.dispatch(
                  actions.STOP_VIDEO_ANNOTATION,
                  this.videoAnnotationUser.userId
                );
              }
              this.$emit('onClickScreen');
            },
            okText: '确定',
            cancelText: '取消',
          });
          return;
        }
        if (this.screenShareMode === 0) {
          if (this.isWhiteboardOpen) {
            this.$message.info('主持人已打开白板，此时您无法进行屏幕共享');
          } else if (this.videoAnnotationUser) {
            this.$message.info(
              `主持人已打开${this.videoAnnotationUser.userName}的视频标注，此时您无法进行屏幕共享`
            );
          } else if (this.screenSharingUser) {
            this.$message.info(
              `${this.screenSharingUser.userName} 正在共享屏幕，此时您无法进行屏幕共享`
            );
          } else {
            this.$emit('onClickScreen');
          }
        } else if (this.screenShareMode === 1 && !this.isHost) {
          this.$message.info({
            content: '主持人已禁止用户共享屏幕',
            key: 'shareTip',
          });
        } else {
          this.$emit('onClickScreen');
        }
      } else {
        this.$emit('onClickScreen');
      }
    },
    setScreenShareMode(mode) {
      this.$store.commit(mutations.SET_SCREEN_SHARE_MODE, mode);
      RtcMessage.getInstance().setProperty(PROP_SETTING, { share: mode });
    },
    selectMic(deviceId) {
      window.rtcEngine.selectMic(deviceId);
      this.$store.commit(mutations.UPDATE_SETTINGS, {
        recordDeviceId: deviceId,
      });
    },
    selectSpeaker(deviceId) {
      window.rtcEngine.selectSpeaker(deviceId);
      this.$store.commit(mutations.UPDATE_SETTINGS, {
        playoutDeviceId: deviceId,
      });
    },
    selectCamera(deviceId) {
      window.rtcEngine.selectCam(deviceId);
      this.$store.commit(mutations.UPDATE_SETTINGS, {
        captureDeviceId: deviceId,
      });
    },
    onMouseEnterMenu() {
      this.isMouseOnToolbar = true;
    },
    onMouseLeaveMenu() {
      this.isMouseOnToolbar = false;
    },
  },
  mounted() {
    if (!this.audioDeviceReady) {
      initAudioDeviceList();
    }
    if (!this.videoDeviceReady) {
      initVideoDeviceList();
    }
  },
};
</script>

<style lang="less" scoped>
@bg-color: rgba(0, 0, 0, 0.95);
@hover-color: #0899f9;

.toolbar {
  position: fixed;
  z-index: 30;
  overflow: hidden;
  pointer-events: all;
  bottom: 0px;
  left: 50%;
  transform: translate(-50%);
  display: inline-block;
  height: 60px;
  padding: 0 20px;
  border-radius: 5px 5px 0 0;
  background-color: @bg-color;
  color: #fff;
  text-align: center;
  display: flex;
  transition: all 0.5s ease-in-out;
  opacity: 1;
  &--hidden {
    bottom: -60px;
    opacity: 0;
  }
  &__btn {
    position: relative;
    display: inline-flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    outline: none;
    border: none;
    width: 76px;
    height: 52px;
    margin-top: 4px;
    border-radius: 4px;
    background-color: transparent;
    color: #ddd;
    font-weight: 400;
    transition: all 0.3s ease;
    position: relative;
    padding: 8px 0;
    cursor: pointer;
    &:hover {
      background-color: #424242;
    }
    &__more-trigger {
      position: absolute;
      right: 4px;
      top: 3px;
      height: 22px;
      width: 20px;
      border-radius: 3px;
      text-align: center;
      background-color: transparent;
      padding: 0px;
      color: white;
      border: none;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      .iconfont {
        font-size: 16px;
      }
      &:hover {
        background-color: #5b5b5b;
        color: white;
      }
    }
    &--active {
      color: #68d56a !important;
      .toolbar__btn__more-trigger {
        color: #68d56a !important;
      }
    }
    &__label {
      position: absolute;
      bottom: 3px;
      left: 0;
      width: 100%;
      text-align: center;
      font-size: 12px;
    }
    &__icon {
      position: absolute;
      top: -3px;
      left: 0;
      width: 100%;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px !important;
    }
  }
}

/deep/ .ant-popover-arrow {
  border-right-color: #000 !important;
  border-bottom-color: #000 !important;
}

/deep/ .ant-popover-inner-content {
  padding: 0px;
}

.popover-btn:hover,
.popover-btn:active,
.popover-btn:focus {
  color: @hover-color;
}

.popover-btn {
  background-color: transparent;
  color: black;
  border: none;
}

.toolbar__btn__more-panel-wrapper {
  padding-bottom: 10px;
  .toolbar__btn__more-panel {
    padding-bottom: 12px;
    background-color: @bg-color;
    border-radius: 4px;
    color: #fff;
    .invte-user {
      padding-top: 12px;
      width: 120px;
      &__btn {
        padding-left: 20px;
        height: 30px;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        cursor: pointer;
        .iconfont {
          font-size: 22px;
        }
        &:hover {
          background-color: @hover-color;
        }
      }
    }
    .more-panel {
      max-width: 250px;
      padding: 10px 10px 0;
    }
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
          background-color: @hover-color;
        }
        &.selected::before {
          content: '';
          position: absolute;
          left: 15px;
          bottom: 9px;
          width: 10px;
          height: 6px;
          border-color: @hover-color;
          border-style: solid;
          border-width: 0 0 1px 1px;
          transform-origin: left bottom;
          transform: rotate(-45deg);
        }
      }
    }
    .list-divider {
      display: block;
      border-top: 1px solid rgba(255, 255, 255, 0.3);
      margin: 12px 8px 4px;
    }
    &.not-allowed {
      padding: 6px 0;
      > ul > li {
        padding: 0 48px 0 18px;
      }
    }
  }
}
</style>
