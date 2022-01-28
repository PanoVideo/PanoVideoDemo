<template>
  <div v-show="videoTag" :shouldShow="shouldShow" class="video-wrapper" ref="videoDiv"></div>
</template>

<script>
import { Logger } from '../utils/logger';

const logger = new Logger('PvcVideo');

export default {
  name: 'PvcVideo',
  props: {
    videoTag: HTMLVideoElement,
    mirror: {
      type: Boolean,
      default: false,
    },
    alias: String,
    shouldShow: Boolean,
  },
  watch: {
    videoTag() {
      this.setSrcObject();
    },
    mirror() {
      this.setVideoMirror();
    },
    shouldShow() {
      logger.info('watch shouldShow', this.alias, this.shouldShow);
      this.setSrcObject();
    },
  },
  mounted() {
    logger.info('mounted shouldShow', this.alias, this.shouldShow, this.videoTag);
    // const videoTag = getVideoElement();
    this.setSrcObject();
    // iOS QQ浏览器从后台切换回来不会自动播放，需要监听visibilitychange事件处理
    document.addEventListener('visibilitychange', this.documentVisibilityChange);
  },
  destroyed() {
    document.removeEventListener('visibilitychange', this.documentVisibilityChange);
  },
  methods: {
    setSrcObject() {
      this.video = this.videoTag;
      this.setVideoMirror();
      if (this.video && this.shouldShow) {
        logger.info('setVideotag', this.alias, this.video);
        this.$refs.videoDiv.innerHTML = '';
        this.$refs.videoDiv.appendChild(this.video);
        this.play();
      } else {
        logger.info('setVideotag', this.alias, this.video);
        this.$refs.videoDiv.innerHTML = '';
      }
    },
    setVideoMirror() {
      if (this.video) {
        if (this.mirror) {
          this.video.classList.add('video-mirror');
        } else {
          this.video.classList.remove('video-mirror');
        }
      }
    },
    play() {
      logger.info('call play in pvch5');
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          const video = this.video;
          if (video && video.ended) {
            logger.info('hit load for video is ended');
            video.load();
          } else if (video && video.paused) {
            logger.info('hit play for video is paused');
            video.play();
          }
        });
      });
    },
    documentVisibilityChange() {
      if (document.visibilityState === 'visible') {
        this.play();
      }
    },
  },
};
</script>

<style lang="less">
.video-wrapper {
  width: 100%;
  position: relative;
}
video {
  display: block;
  width: 100%;
}
.video-mirror {
  transform: rotateY(180deg);
}
</style>
