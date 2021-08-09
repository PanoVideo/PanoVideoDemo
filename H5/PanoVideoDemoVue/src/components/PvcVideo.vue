<template>
  <div class="video-wrapper" ref="videoDiv"></div>
</template>

<script>
import { parseSearch } from '@/utils';

const debuggerFlag = parseSearch(window.location.search.substring(1), 'debugger');

export default {
  name: 'PvcVideo',
  props: {
    videoTag: HTMLVideoElement,
  },
  mounted() {
    const { videoDiv } = this.$refs;
    // SDK提供的video元素
    const { videoTag } = this;
    videoTag.autoplay = true;
    videoTag.muted = true;
    videoTag.playsInline = true;
    // debugger 时可以显示controls
    if (debuggerFlag !== undefined) {
      videoTag.controls = true;
    }
    videoTag.onloadeddata = () => {
      videoTag.play();
    };
    videoTag.oncanplay = () => {
      videoTag.play();
    };
    videoTag.onended = () => {
      videoTag.load();
    };
    videoTag.onerror = () => {
      videoTag.load();
    };
    videoDiv.appendChild(videoTag);
    function play() {
      if (videoTag.ended) {
        videoTag.load();
      } else if (videoTag.paused) {
        videoTag.play();
      }
    }
    setTimeout(play, 0);
  },
};
</script>

<style lang="less">
.video-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}
video {
  width: 100%;
  height: 100%;
}
</style>
