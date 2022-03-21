import store from '@/store';
import { SET_CLOSE_CONFIRM_VISIBLE } from '@/store/mutations';
import Vue from 'vue';

export default Vue.extend<
  Record<string, never>,
  {
    addListeners(): void;
    removeListeners(): void;
    onScreenCaptureRegionChanged(info: {
      left: number;
      top: number;
      right: number;
      bottom: number;
    }): void;
    onScreenCaptureDisplayChanged(
      displayId: number,
      info: { left: number; top: number; right: number; bottom: number }
    ): void;
    handleCloseWindow(): void;
    handleMeetingCommand(e: any, data: { command: string; payload: any }): void;
    syncSettings(): void;
  },
  {
    userMe: UserInfo;
    stopShare(): void;
    onClickExit(): void;
    onClickMicMute(): void;
    onClickCamMute(): void;
  }
>({
  methods: {
    addListeners() {
      window.rtcEngine.on(
        'screenCaptureDisplayChanged',
        this.onScreenCaptureDisplayChanged
      );
      window.rtcEngine.on(
        'screenCaptureRegionChanged',
        this.onScreenCaptureRegionChanged
      );
      window.electron.ipcRenderer.on('closeWindow', this.handleCloseWindow);
    },
    removeListeners() {
      window.rtcEngine.off(
        'screenCaptureDisplayChanged',
        this.onScreenCaptureDisplayChanged
      );
      window.rtcEngine.off(
        'screenCaptureRegionChanged',
        this.onScreenCaptureRegionChanged
      );
      window.electron.ipcRenderer.off('closeWindow', this.handleCloseWindow);
    },
    onScreenCaptureRegionChanged(info: {
      left: number;
      top: number;
      right: number;
      bottom: number;
    }) {
      const { left, top, right, bottom } = info;
      const payload = {
        x: left,
        y: top,
        width: right - left,
        height: bottom - top,
        shareType: store.getters.userMe.screenShareType,
      };
      window.ipc?.sendToShareCtrlWindow({
        command: 'setSharePosition',
        payload,
      });
    },
    onScreenCaptureDisplayChanged(
      displayId: number,
      info: { left: number; top: number; right: number; bottom: number }
    ) {
      const { left, top, right, bottom } = info;
      const sharedScreenOrAppDispay = {
        displayId,
        x: left,
        y: top,
        width: right - left,
        height: bottom - top,
      };
      // 设置桌面共享控制窗口转移到对应的屏幕
      window.ipc?.sendToMainProcess({
        command: 'setShareWindow',
        payload: sharedScreenOrAppDispay,
      });
    },
    handleCloseWindow() {
      store.commit(SET_CLOSE_CONFIRM_VISIBLE, true);
    },
    syncSettings() {
      window.ipc.sendToShareCtrlWindow({
        command: 'syncSettings',
        payload: {
          videoMuted: this.userMe.videoMuted,
          audioMuted: this.userMe.audioMuted,
        },
      });
    },
    /**
     * @param data  { command: string; payload: any }
     */
    handleMeetingCommand(e: any, data: { command: string; payload: any }) {
      switch (data.command) {
        case 'stopShare':
          this.stopShare();
          break;
        case 'exit':
          // 退出前取消共享
          this.userMe.screenOpen && this.stopShare();
          this.onClickExit();
          break;
        case 'toggleMic':
          this.onClickMicMute();
          break;
        case 'toggleCamera':
          this.onClickCamMute();
          break;
        case 'getUserMeStatus':
          this.syncSettings();
          break;
        default:
          break;
      }
    },
  },
  mounted() {
    this.addListeners();
    window.electron?.ipcRenderer.on(
      'msgToMainWindow',
      this.handleMeetingCommand
    );
  },
  destroyed() {
    this.removeListeners();
    window.electron?.ipcRenderer.off(
      'msgToMainWindow',
      this.handleMeetingCommand
    );
  },
});
