<template>
  <div class="video-annotation-wrapper">
    <div class="video-annotation" ref="annotationWbRef" />
    <AnnotationTools v-if="annotationEnabled" :whiteboard="annotationWb" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { MediaType } from '@/store/modules/user';
import AnnotationTools from '@/components/annotation/VideoAnnotationToolbar.vue';
import { RtsService } from '@pano.video/panorts';
import { getVideoInfo } from '@/utils';

export default {
  props: {
    userId: String,
    audioMuted: Boolean,
    audioType: String,
    fontSize: String,
  },
  data() {
    return {
      annotationWb: undefined,
      annotationEnabled: false,
      setANViewSizeTimer: undefined,
    };
  },
  components: { AnnotationTools },
  computed: {
    ...mapGetters([
      'mainViewUser',
      'mainViewUserData',
      'userMe',
      'enableVideoAnnotation',
      'isWhiteboardOpen',
    ]),
    showScreenShare() {
      return (
        this.mainViewUserData.type === MediaType.screen &&
        this.mainViewUser !== this.userMe
      );
    },
    videoAnnotationOn() {
      return this.mainViewUser.videoAnnotationOn;
    },
    shareAnnotationOn() {
      return this.mainViewUser.shareAnnotationOn;
    },
  },
  watch: {
    ['mainViewUser.userId']() {
      this.checkAnnotation();
    },
    ['mainViewUserData.type'](newV) {
      if (newV === MediaType.screen) {
        this.checkAnnotation();
      }
    },
    ['mainViewUser.videoAnnotationOn']() {
      this.checkAnnotation();
    },
    ['mainViewUser.shareAnnotationOn']() {
      this.checkAnnotation();
    },
    ['mainViewUser.externalAnnotationOn']() {
      this.checkAnnotation();
    },
    ['mainViewUser.videoMuted']() {
      this.checkAnnotation();
    },
    isWhiteboardOpen() {
      this.checkAnnotation();
    },
  },
  methods: {
    openAnnotation() {
      let type = 'share';
      if (this.mainViewUser.externalAnnotationOn) {
        type = 'external';
      } else if (this.mainViewUser.videoAnnotationOn) {
        type = 'video';
      }
      this.annotationWb = RtsService.getInstance().getAnnotation(
        this.mainViewUser.userId,
        type
      );
      if (
        this.isUserMe &&
        !this.showScreenShare &&
        this.videoAnnotationOn &&
        this.mainViewUser.videoMuted
      ) {
        this.annotationWb.stop();
        // 关闭视频时结束视频标注
        this.$message.info({
          content: '视频已关闭，结束视频标注',
          key: 'videoAnnotationStop',
        });
        clearInterval(this.setANViewSizeTimer);
        return;
      }
      this.annotationWb.enableCursorSync();
      if (type !== 'share') {
        // 视频标注的情况，需要主动设置自己的分辨率信息
        const size = getVideoInfo(
          this.showScreenShare
            ? this.mainViewUser.screenDomRef
            : this.mainViewUser.videoDomRef
        );
        if (size && size.width && size.height) {
          this.annotationWb.setAnnotationViewSize(size);
          clearInterval(this.setANViewSizeTimer);
        } else {
          clearInterval(this.setANViewSizeTimer);
          this.setANViewSizeTimer = setInterval(this.openAnnotation, 500);
        }
      }
      if (!this.annotationWb.isWhiteboardOpen) {
        this.annotationWb?.open(this.$refs.annotationWbRef);
      }
      this.annotationEnabled = true;
      window.shareAnnotationWb = this.annotationWb;
    },
    isUserMe() {
      return this.mainViewUser === this.userMe;
    },
    checkAnnotation() {
      if (this.annotationWb && this.annotationWb.tragetUserId !== this.userId) {
        this.annotationWb.close();
        this.annotationWb = undefined;
      }
      if (
        (this.showScreenShare && this.shareAnnotationOn) ||
        (this.showScreenShare && this.mainViewUser.externalAnnotationOn) ||
        (!this.showScreenShare && this.videoAnnotationOn)
      ) {
        this.openAnnotation();
      } else {
        this.closeShareAnnotation();
      }
    },
    closeShareAnnotation() {
      this.annotationWb?.close();
      this.$refs.annotationWbRef.innerHTML = '';
      this.annotationEnabled = false;
    },
  },
  mounted() {
    this.checkAnnotation();
  },
  beforeDestroy() {
    this.annotationWb?.close();
  },
};
</script>

<style lang="less" scoped>
.video-annotation-wrapper {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 11;
  .video-annotation {
    width: 100%;
    height: 100%;
    margin: auto;
    display: block;
  }
}
</style>
