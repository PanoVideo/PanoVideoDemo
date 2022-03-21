<template>
  <Modal
    destroyOnClose
    :visible="shareSelectVisible"
    :closable="false"
    :width="640"
    :footer="null"
    :style="{ userSelect: 'none' }"
  >
    <div>
      <Tabs v-model="screenSourceType">
        <TabPane tab="桌面" key="screen">
          <div class="previewList">
            <div
              v-for="(item, index) of optionScreens.screens"
              :key="item.ssid"
              :class="{
                previewListItem: true,
                previewListItem__selected: selectdScreenSid === item.ssid,
              }"
              @click="onShareSelected(item.ssid)"
              @dblclick="startShare(item.ssid)"
            >
              <div class="previewImgWrapper">
                <img alt="" :src="getPreviewVideo(item, index, 'screen')" />
              </div>
              <div class="previewListItemTitle">{{ item.name }}</div>
            </div>
          </div>
        </TabPane>
        <TabPane tab="应用" key="app">
          <div class="previewList">
            <div
              v-for="(item, index) of optionScreens.application"
              :key="item.ssid"
              :class="{
                previewListItem: true,
                previewListItem__selected: selectdScreenSid === item.ssid,
              }"
              @click="onShareSelected(item.ssid)"
              @dblclick="startShare(item.ssid)"
            >
              <div class="previewImgWrapper">
                <img alt="" :src="getPreviewVideo(item, index, 'screen')" />
              </div>
              <div class="previewListItemTitle">
                {{ item.name }}
              </div>
            </div>
          </div>
        </TabPane>
      </Tabs>
      <div
        :style="{
          paddingTop: '16px',
          marginTop: '10px',
          display: 'flex',
          justifyContent: 'space-between',
          borderTop: '1px solid #f0f0f0',
        }"
      >
        <div>
          <Tooltip
            title="共享画面的同时，会议成员将听到您电脑上的声音（须打开音频）"
            placement="bottom"
          >
            <Checkbox v-model="shareSound"> 共享电脑声音 </Checkbox>
          </Tooltip>
          <Tooltip
            title="视频流畅度优先以获得更好的视频观看体验"
            placement="bottom"
          >
            <Checkbox :style="{ marginLeft: '10px' }" v-model="optimized">
              视频流畅度优先
            </Checkbox>
          </Tooltip>
        </div>
        <div>
          <Button @click="cancel"> 取消 </Button>
          <Button
            :style="{ marginLeft: '10px' }"
            type="primary"
            :disabled="enableShare"
            @click="startShare(selectdScreenSid)"
          >
            分享
          </Button>
        </div>
      </div>
    </div>
  </Modal>
</template>

<script>
import { RtcMessage } from '@pano.video/panorts';
import { Button, Tabs, Modal, Checkbox, Tooltip } from 'ant-design-vue';
import { mapGetters } from 'vuex';
import { UPDATE_USER_ME, SET_SCREEN_PREVIEW_LIST } from '@/store/mutations';
import { get, find } from 'lodash-es';
import { APP_NAME } from '@/constants/app';
import * as actions from '@/store/actions';
const ScreenSourceType = {
  /** @~english Display. @~chinese 桌面。 */
  Display: 0,
  /** @~english Application. @~chinese 应用。 */
  Application: 1,
  /** @~english Window. @~chinese 窗口。 */
  Window: 2,
};

export default {
  props: {
    shareSelectVisible: Boolean,
  },
  data() {
    return {
      shareSound: false,
      optimized: false,
      screenSourceType: 'screen',
      optionScreens: { application: [], screens: [] },
      selectdScreenSid: '',
      stopGetPreview: false,
    };
  },
  components: {
    Modal,
    Checkbox,
    Button,
    Tooltip,
    Tabs,
    TabPane: Tabs.TabPane,
  },
  watch: {
    screenSourceType(newValue) {
      this.selectdScreenSid =
        newValue === 'screen'
          ? this.optionScreens.screens[0].ssid
          : this.optionScreens.application[0].ssid;
    },
  },
  computed: {
    ...mapGetters(['screenPreviewList', 'userMe']),
    enableShare() {
      return !this.selectdScreenSid;
    },
  },
  methods: {
    startShare(selectdScreenSid) {
      this.$emit('update:shareSelectVisible', false);
      if (selectdScreenSid) {
        window.rtcEngine.screenSourceMgr().beginConfiguration(true);
        // 不捕捉本app
        window.rtcEngine
          .screenSourceMgr()
          .addUnsharedScreenSource(
            ScreenSourceType.Application,
            `${process.ppid}`
          );
        if (this.screenSourceType === 'app') {
          window.rtcEngine
            .screenSourceMgr()
            .addSharedScreenSource(1, selectdScreenSid);
          this.$store.commit(UPDATE_USER_ME, {
            screenShareType: 'application',
          });
        } else {
          window.rtcEngine
            .screenSourceMgr()
            .selectSharedDisplay(selectdScreenSid);
          this.$store.commit(UPDATE_USER_ME, { screenShareType: 'screen' });
        }
        this.$store.commit(UPDATE_USER_ME, { screenOpen: true });
        window.rtcEngine
          .screenSourceMgr()
          .setScreenOptimization(this.optimized);
        window.rtcEngine.screenSourceMgr().commitConfiguration();
        // if (this.userMe.screenShareType === 'screen') {
        //   window.rtcEngine.screenSourceMgr().enableFilterWindows(false);
        // }
        window.rtcEngine.startScreen();
        if (this.shareSound) {
          window.rtcEngine.startSoundCardShare();
        }
        window.ipc.sendToMainProcess({
          command: 'showShareCtrlWindow',
        });
        setTimeout(() => {
          window.ipc.sendToShareCtrlWindow({
            command: 'syncSettings',
            payload: {
              videoMuted: this.userMe.videoMuted,
              audioMuted: this.userMe.audioMuted,
            },
          });
        }, 500);
        RtcMessage.getInstance().broadcastMessage(
          {
            type: 'screenSourceType',
            payload: this.userMe.screenShareType,
          },
          false
        );
        // 关闭白板
        this.$store.dispatch(actions.SET_IS_WHITEBOARD_OPEN, false);
      } else if (this.shareSound) {
        window.rtcEngine.startSoundCardShare();
      } else {
        this.$store.commit(UPDATE_USER_ME, { screenOpen: false });
      }
    },
    getPreviewVideo(item, index, type) {
      let preview = find(this.screenPreviewList, (p) => {
        return (
          item.name.toLowerCase().includes(p.name.toLowerCase()) ||
          p.display_id === item.ssid
        );
      });
      if (!preview && type === 'screen') {
        preview = get(
          this.screenPreviewList.filter((i) => i.display_id),
          index
        );
      }
      if (preview && preview) {
        return preview.thumbnail;
      }
      return '';
    },
    onShareSelected(ssid) {
      this.selectdScreenSid = ssid;
    },
    cancel() {
      window.ipc.sendToMainProcess({
        command: 'destoryShareCtrlWin',
      });
      this.$store.commit(UPDATE_USER_ME, { screenOpen: false });
      this.$emit('update:shareSelectVisible', false);
    },
  },
  async mounted() {
    this.optionScreens = {
      application: window.rtcEngine
        .screenSourceMgr()
        .getApplicationList()
        .filter((source) => !source.name.includes(APP_NAME)),
      screens: window.rtcEngine.screenSourceMgr().getDisplayList(),
    };
    this.selectdScreenSid = this.optionScreens.screens[0]?.ssid || '';
    window.ipc.sendToMainProcess({ command: 'createShareCtrlWin' });
    window.electron.desktopCapturer
      .getSources({
        types: ['window', 'screen'],
        thumbnailSize: { width: 300, height: 300 },
      })
      .then(async (sources) => {
        this.$store.commit(
          SET_SCREEN_PREVIEW_LIST,
          sources.map((source) => ({
            name: source.name,
            id: source.id,
            display_id: source.display_id,
            appIcon: source.appIcon,
            thumbnail: source.thumbnail.toDataURL(),
          }))
        );
      });
  },
};
</script>

<style lang="less" scoped>
.previewList {
  display: flex;
  flex-wrap: wrap;
  max-height: 400px;
  overflow-y: auto;
  .previewListItem {
    border: 2px solid transparent;
    border-radius: 3px;
    &__selected {
      border: 2px solid RGBA(167, 196, 247, 1);
    }
    width: 160px;
    height: 160px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 5px;
    margin: 0 15px;
    cursor: pointer;
    .previewImgWrapper {
      flex: 1;
      max-height: 120px;
      overflow: hidden;
      img {
        width: 150px;
        height: auto;
      }
    }
    .previewListItemTitle {
      max-width: 100%;
      margin-top: 5px;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.shareSettings {
  position: absolute;
  top: 35px;
  right: 20px;
  display: flex;
  .settingItem {
    display: flex;
    align-items: center;
    &__title {
      margin: 0 5px 0 15px;
    }
  }
}
</style>
