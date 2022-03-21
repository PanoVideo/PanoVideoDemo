import { RtsService, Annotation } from '@pano.video/panorts';
import { mapGetters } from 'vuex';
import Vue from 'vue';
import { LogUtil } from '@/utils';

/**
 * 共享标注在主渲染进程中的相关逻辑
 * 1. 创建和销毁 Annotation 对象，主进程中 Annotation 对象是用来收发标注消息（通过 shareAnnotation.sendCommand）
 * 以及转发绘制指令给 shareCtrlWindow 中的 AnnotationProxy 对象（通过 shareAnnotation.commandProxy）
 * 2. 获取桌面共享的分辨率设置到 shareAnnotation 对象中（shareAnnotation.setAnnotationViewSize）
 */
export default Vue.extend<
  {
    shareAnnotation?: Annotation;
    sharedScreenPosition?: {
      x: number;
      y: number;
      width: number;
      height: number;
    };
  },
  {
    handleShareAnnoCmd(e: any, data: { command: string; payload: any }): void;
    onShareScreenCaptureRegionChanged(info: {
      left: number;
      top: number;
      right: number;
      bottom: number;
    }): void;
    startShareAnnotation(): void;
    stopShareAnnotation(): void;
  },
  {
    userMe: UserInfo;
    userList: UserInfo[];
  }
>({
  data() {
    return {
      shareAnnotation: undefined,
      sharedScreenPosition: undefined,
    };
  },
  computed: {
    ...mapGetters(['userMe', 'userList']),
  },
  methods: {
    handleShareAnnoCmd(_, data) {
      switch (data.command) {
        case 'stopShare':
          this.stopShareAnnotation();
          break;
        case 'sendProxyWbData':
          this.shareAnnotation?.sendCommand(data.payload);
          break;
        case 'startShareAnnotation':
          this.startShareAnnotation();
          break;
        case 'stopShareAnnotation':
          this.stopShareAnnotation();
          break;
        default:
          break;
      }
    },
    // 屏幕捕捉分辨率变化
    // 一般桌面的分辨率是不会变化的，共享app时app可以由用户手动拖拽放大缩小
    onShareScreenCaptureRegionChanged(info) {
      LogUtil('共享窗口/app 的位置变化: ', info);
      const { left, top, right, bottom } = info;
      this.sharedScreenPosition = {
        x: left,
        y: top,
        width: right - left,
        height: bottom - top,
      };
      if (this.shareAnnotation) {
        this.shareAnnotation.setAnnotationViewSize({
          width: right - left,
          height: bottom - top,
        });
      }
      window.ipc.sendToShareCtrlWindow({
        command: 'setSharePosition',
        payload: {
          x: left,
          y: top,
          width: right - left,
          height: bottom - top,
          shareType: this.userMe.screenShareType,
        },
      });
    },
    startShareAnnotation() {
      this.userList.forEach((u) => {
        // 这是 APP 层控制的逻辑，为了防止标注混乱，禁止两个人同时开启标注
        // sdk 是没有限制的，理论上是支持多个人同时开启标注的
        if (u.shareAnnotationOn) {
          RtsService.getInstance().getAnnotation(u.userId, 'share').stop();
          window.ipc.sendToShareCtrlWindow({
            command: 'showIndication',
            payload: { text: `已关闭 ${u.userName} 的共享标注` },
          });
        }
      });
      if (!this.shareAnnotation) {
        this.shareAnnotation = RtsService.getInstance().getAnnotation(
          this.userMe.userId,
          'share'
        );
        this.shareAnnotation.joinSession();
        this.sharedScreenPosition &&
          this.shareAnnotation.setAnnotationViewSize(this.sharedScreenPosition);
        this.shareAnnotation.commandProxy = (cmd) => {
          window.ipc.sendToShareCtrlWindow({
            command: 'wbCmd',
            payload: cmd,
          });
        };
        this.shareAnnotation.once(Annotation.Events.sessionReady, () => {
          window.ipc.sendToShareCtrlWindow({
            command: 'init',
            payload: {
              nodeId: this.shareAnnotation!.nodeId,
              userId: this.shareAnnotation!.userId,
              userName: this.shareAnnotation!.userName,
            },
          });
        });
      }
    },
    stopShareAnnotation() {
      if (this.shareAnnotation) {
        this.shareAnnotation.stop();
        this.shareAnnotation = undefined;
      }
    },
  },
  mounted() {
    window.electron.ipcRenderer.on('msgToMainWindow', this.handleShareAnnoCmd);
    window.rtcEngine.on(
      'screenCaptureRegionChanged',
      this.onShareScreenCaptureRegionChanged
    );
  },
  beforeDestroy() {
    window.electron.ipcRenderer.off('msgToMainWindow', this.handleShareAnnoCmd);
    window.rtcEngine.off(
      'screenCaptureRegionChanged',
      this.onShareScreenCaptureRegionChanged
    );
    this.stopShareAnnotation();
  },
});
