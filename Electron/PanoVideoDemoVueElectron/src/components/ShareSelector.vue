<template>
  <el-dialog
    :visible="visible"
    center
    class="pano-share-select-modal"
    destroy-on-close
    @close="onClose"
    width="640px"
    :show-close="false"
  >
    <el-tabs v-model="screenSourceType">
      <el-tab-pane label="桌面" name="screen">
        <div class="preview-list-wrapper">
          <div class="preview-list">
            <div
              v-for="(screen, index) in screens"
              :key="screen.ssid"
              :class="{
                'preview-list-item': true,
                'preview-list-item--selected': selectdScreenSid === screen.ssid
              }"
              @click="onShareSelected(screen.ssid)"
              @dblclick="onShareStart('screen', screen.ssid)"
            >
              <div class="preview-img-wrapper">
                <img alt="" :src="getPreviewVideo(screen, index, 'screen')" />
              </div>
              <div class="preview-item__tittle">{{ screen.name }}</div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="应用" name="app">
        <div class="preview-list-wrapper">
          <div class="preview-list">
            <div
              v-for="(app, index) in applications"
              :key="app.ssid"
              :class="{
                'preview-list-item': true,
                'preview-list-item--selected': selectdScreenSid === app.ssid
              }"
              @click="onShareSelected(app.ssid)"
              @dblclick="onShareStart('app', app.ssid)"
            >
              <div class="preview-img-wrapper">
                <img alt="" :src="getPreviewVideo(app, index, 'application')" />
              </div>
              <div class="preview-item__tittle">{{ app.name }}</div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <div slot="footer" class="dialog-footer">
      <el-button @click="onClose">取 消</el-button>
      <el-button
        type="primary"
        @click="onShareStart(screenSourceType, selectdScreenSid)"
        :disabled="!selectdScreenSid"
      >
        确 定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { ScreenSourceType } from '@pano.video/panortc-electron-sdk';
import { get, find } from 'lodash-es';
import { mapGetters, mapMutations } from 'vuex';

const electron = window.require('electron');

export default {
  data() {
    return {
      screenSourceType: 'screen',
      screens: [],
      applications: [],
      selectdScreenSid: '',
      screenPreviewList: []
    };
  },
  props: {
    visible: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    ...mapGetters(['userMe'])
  },
  methods: {
    ...mapMutations(['updateUser']),
    onClose() {
      this.updateUser({
        userId: this.userMe.userId,
        screenOpen: false
      });
      this.$emit('update:visible', false);
      electron.remote.app.closeShareCtrlWindow();
    },
    onShareSelected(ssid) {
      this.selectdScreenSid = ssid;
    },
    onShareStart(screenSourceType, selectdScreenSid) {
      if (selectdScreenSid) {
        window.rtcEngine.screenSourceMgr().beginConfiguration(true);
        window.rtcEngine
          .screenSourceMgr()
          .addUnsharedScreenSource(
            ScreenSourceType.Application,
            `${global.process.ppid}`
          );
        if (screenSourceType === 'app') {
          window.rtcEngine
            .screenSourceMgr()
            .addSharedScreenSource(
              ScreenSourceType.Application,
              selectdScreenSid
            );
          this.updateUser({
            userId: this.userMe.userId,
            screenShareType: 'application'
          });
        } else {
          // 这行代码可以禁用窗口过滤，提示桌面共享性能，建议远程控制时开启
          // rtcEngine.screenSourceMgr().enableFilterWindows(false);
          window.rtcEngine
            .screenSourceMgr()
            .selectSharedDisplay(selectdScreenSid);
          this.updateUser({
            userId: this.userMe.userId,
            screenShareType: 'screen'
          });
        }
        window.rtcEngine.screenSourceMgr().commitConfiguration();
        window.rtcEngine.startScreen();
        electron.remote.app.showShareCtrlWindow();
        electron.remote.app.sendToShareWindow({
          command: 'syncSettings',
          payload: {
            videoMuted: this.userMe.videoMuted,
            audioMuted: this.userMe.audioMuted
          }
        });
      } else {
        this.updateUser({
          userId: this.userMe.userId,
          screenOpen: false
        });
      }
      this.$emit('update:visible', false);
    },
    getPreviewVideo(item, index, type) {
      // type  'screen' | 'application'
      let preview = find(
        this.screenPreviewList,
        p =>
          item.name.toLowerCase().includes(p.name.toLowerCase()) ||
          p.display_id === item.ssid
      );
      if (!preview && type === 'screen') {
        preview = get(
          this.screenPreviewList.filter(i => i.display_id),
          index
        );
      }
      if (item.name.includes('Google Chrome - Tempo')) {
        console.log('item', item.name, 'preview', preview);
        console.log(this.screenPreviewList);
      }
      if (preview && preview.thumbnail) {
        return preview.thumbnail.toDataURL();
      }
      return '';
    },
    getScreenPreviewImg() {
      this.screens = window.rtcEngine.screenSourceMgr().getDisplayList();
      this.applications = window.rtcEngine
        .screenSourceMgr()
        .getApplicationList();
      if (
        ![...this.applications, ...this.screens]
          .map(i => i.ssid)
          .includes(this.selectdScreenSid)
      ) {
        this.selectdScreenSid = '';
      }
      electron.desktopCapturer
        .getSources({
          types: ['window', 'screen'],
          thumbnailSize: { width: 300, height: 300 }
        })
        .then(async sources => {
          this.screenPreviewList = sources.map(source => ({
            name: source.name,
            id: source.id,
            display_id: source.display_id,
            appIcon: source.appIcon,
            thumbnail: source.thumbnail
          }));
        });
    }
  },
  mounted() {
    this.getScreenPreviewImg();
    electron.remote.app.createShareCtrlWindow();
  },
  beforeDestroy() {}
};
</script>

<style lang="scss" scoped>
.preview-list-wrapper {
  max-height: 330px;
  overflow-y: auto;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  .preview-list-item {
    border: 2px solid transparent;
    border-radius: 3px;
    &--selected {
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
    .preview-img-wrapper {
      flex: 1;
      img {
        width: 150px;
        height: auto;
      }
    }
    .preview-item__tittle {
      max-width: 100%;
      margin-top: 5px;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
