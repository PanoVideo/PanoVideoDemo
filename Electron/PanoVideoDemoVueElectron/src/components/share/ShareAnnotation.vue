<template>
  <div class="share-annotation">
    <div
      class="share-annotation__wb"
      ref="annotationRef"
      :style="{
        ...(this.shareType === 'application' && this.annotationPosition
          ? {
              left: `${this.annotationPosition.left}px`,
              top: `${this.annotationPosition.top}px`,
              width: `${this.annotationPosition.width}px`,
              height: `${this.annotationPosition.height}px`
            }
          : {
              width: '100%',
              height: '100%',
              left: 0,
              top: 0
            })
      }"
    />
    <ShareAnnotationToolbar
      v-if="annotationWb"
      :whiteboard="annotationWb"
      @updateIgnoreMouseEvents="updateIgnoreMouseEvents"
      @mouseenter.native="onMouseEnterCtrl"
      @focus.native="onMouseEnterCtrl"
      @mouseleave.native="onMouseLeaveCtrl"
      @blur.native="onMouseLeaveCtrl"
    />
  </div>
</template>

<script>
import { AnnotationProxy } from '@pano.video/whiteboard';
import ShareAnnotationToolbar from './ShareAnnotationToolbar';

const electron = window.require('electron');

export default {
  data() {
    return { annotationWb: undefined };
  },
  props: {
    shareType: { type: String },
    annotationPosition: { type: Object, required: false }
  },
  watch: {
    annotationPosition() {
      this.setAnnotationViewSize();
    }
  },
  components: {
    ShareAnnotationToolbar
  },
  methods: {
    handleMessage(_, data) {
      switch (data.command) {
        case 'wbCmd':
          this.annotationWb?.commandProxy(data.payload);
          break;
        case 'init':
          this.annotationWb?.init(data.payload.nodeId, data.payload.userId);
          break;
      }
    },
    setAnnotationViewSize() {
      let width = this.annotationPosition.width;
      let height = this.annotationPosition.height;
      if (this.shareType === 'application') {
        // windows 高分屏的屏幕分辨率和 rtcEngine 上报的share位置是一致的，
        // 但是浏览器的最大宽度是经过 devicePixelRatio 换算的，需要再转换一下
        const scaleRatio = process.platform === 'darwin' ? 1 : devicePixelRatio;
        width = width * scaleRatio;
        height = height * scaleRatio;
      }
      this.annotationWb?.setAnnotationViewSize({ width, height });
      this.annotationWb?.updateCanvasSize();
    },
    sendMessageToMainWindow(data) {
      electron.remote.app.sendToMainWindow(data);
    },
    updateIgnoreMouseEvents(ignoreMouseEvents) {
      this.$emit('updateIgnoreMouseEvents', ignoreMouseEvents);
    },
    onMouseEnterCtrl() {
      this.$emit('mouseEnterToolbar');
    },
    onMouseLeaveCtrl() {
      this.$emit('mouseLeaveToolbar');
    }
  },
  mounted() {
    electron.ipcRenderer.on('msgToShareWindow', this.handleMessage);
    this.sendMessageToMainWindow({ command: 'startShareAnnotation' });
    this.annotationWb = new AnnotationProxy(cmd =>
      this.sendMessageToMainWindow({
        command: 'sendProxyWbData',
        payload: cmd
      })
    );
    this.annotationWb.open(this.$refs.annotationRef);
    this.setAnnotationViewSize();
  },
  beforeDestroy() {
    electron.ipcRenderer.off('msgToShareWindow', this.handleMessage);
    this.sendMessageToMainWindow({
      command: 'stopShareAnnotation'
    });
  }
};
</script>

<style lang="scss" scoped>
.share-annotation {
  width: 100%;
  height: 100%;
  position: absolute;
  z-index: 100;
  left: 0;
  top: 0;
  .share-annotation__wb {
    position: absolute;
    width: 100%;
    height: 100%;
  }
}
</style>
