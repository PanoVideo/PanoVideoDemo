import { mapGetters } from 'vuex';
import Vue from 'vue';
import store from '@/store';
import * as mutations from '@/store/mutations';

let preventDisplaySleepID = 0;

/**
 * JoinChannel 页面 electron 相关逻辑
 *
 * 调整窗口大小，禁止最大化和禁止调整窗口大小
 *
 */
export default Vue.extend<
  Record<string, never>,
  {
    handleCloseWindow(): void;
  },
  {
    userMe: UserInfo;
  }
>({
  computed: {
    ...mapGetters(['userMe']),
  },
  methods: {
    handleCloseWindow() {
      window.ipc.sendToMainProcess({ command: 'closeApp' });
    },
  },
  mounted() {
    window.remote.getCurrentWindow().unmaximize();
    window.remote.getCurrentWindow().setSize(700, 680, true);
    window.remote.getCurrentWindow().center();
    window.remote.getCurrentWindow().setResizable(false);
    window.remote.getCurrentWindow().setMaximizable(false);
    // login 页面不再需要阻止息屏
    preventDisplaySleepID &&
      window.remote.powerSaveBlocker.stop(preventDisplaySleepID);
    window.electron.ipcRenderer.on('closeWindow', this.handleCloseWindow);
    // login 页面时先获取一次 桌面、APP 缩略图，因为某些机型调用 desktopCapturer.getSources 返回会很慢
    // 之后在打开桌面共享时会再取一次最新的桌面、APP 缩略图
    window.electron.desktopCapturer
      .getSources({
        types: ['window', 'screen'],
        thumbnailSize: { width: 300, height: 300 },
      })
      .then(async (sources: any) => {
        store.commit(mutations.SET_SCREEN_PREVIEW_LIST, sources);
      });
  },
  beforeDestroy() {
    window.electron.ipcRenderer.off('closeWindow', this.handleCloseWindow);
    // 离开login 页面取消禁止最大化
    window.remote.getCurrentWindow().setResizable(true);
    window.remote.getCurrentWindow().setMaximizable(true);
    window.remote.getCurrentWindow().maximize();
    // powerSaveBlocker 在会中时，禁止息屏
    preventDisplaySleepID = window.remote.powerSaveBlocker.start(
      'prevent-display-sleep'
    );
  },
});
