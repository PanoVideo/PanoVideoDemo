<template>
  <div :style="{ fontSize: '12px', userSelect: 'none', width: '180px' }">
    <div v-if="$IS_ELECTRON">
      <div>解码器类型: {{ codecInfo }}</div>
      <div>分辨率@帧率: {{ fpsInfo }}</div>
      <div>码率: {{ bitrateInfo }}</div>
    </div>
    <div v-else>
      分辨率:
      {{ videoInfo.width ? `${videoInfo.width} x ${videoInfo.height}` : '-' }}
    </div>
  </div>
</template>

<script>
import { genUserVideoDomId } from '@/utils';
import { mapGetters } from 'vuex';
import { find } from 'lodash-es';

export default {
  props: {
    userId: String,
    userName: String,
    type: String,
  },
  data() {
    return {
      stats: undefined,
      videoInfo: { width: 0, height: 0 },
      timer: undefined,
    };
  },
  components: {},
  computed: {
    ...mapGetters(['userMe']),
    codecInfo() {
      const videoStats = this.stats;
      if (!videoStats) {
        return '-';
      }
      const flag = this.$IS_ELECTRON && videoStats;
      let codec = videoStats?.codec === 1 ? 'H.264' : 'AV1';
      codec = flag ? codec : '-';
      return codec;
    },
    fpsInfo() {
      const videoStats = this.stats;
      const flag = this.$IS_ELECTRON && videoStats;
      return flag
        ? `${videoStats.width} x ${videoStats.height} @ ${videoStats.fps}`
        : '-';
    },
    bitrateInfo() {
      const videoStats = this.stats;
      const flag = this.$IS_ELECTRON && videoStats;
      return flag ? `${Math.floor(videoStats.bitrate / 1000)} kbps` : '-';
    },
  },
  methods: {
    getVideoInfo() {
      const { type, userId, userName } = this;
      const domRef = document.getElementById(
        genUserVideoDomId(type, userId, userName)
      );
      const videoDom =
        domRef?.getElementsByTagName('video')[0] ||
        domRef?.getElementsByTagName('canvas')[0];
      if (videoDom) {
        this.videoInfo = {
          width:
            videoDom instanceof HTMLVideoElement
              ? videoDom.videoWidth
              : videoDom.width,
          height:
            videoDom instanceof HTMLVideoElement
              ? videoDom.videoHeight
              : videoDom.height,
        };
      } else {
        this.videoInfo = { width: 0, height: 0 };
      }
    },
    addMediaStatsObserver() {
      window.rtcEngine.addMediaStatsObserver(this.onMediaStatsChanged);
    },
    removeMediaStatsObserver() {
      window.rtcEngine.removeMediaStatsObserver(this.onMediaStatsChanged);
    },
    onMediaStatsChanged(stats) {
      const userMe = this.$store.getters.userMe;
      const { userId, type } = this;
      if (userId === userMe.userId) {
        const localVideoStatsArray = stats.localStats.videoStats;
        const videoStats = localVideoStatsArray && localVideoStatsArray[0];
        this.stats = videoStats || undefined;
      } else if (stats.remoteStats) {
        const screenStatsArray = stats.remoteStats.screenStats;
        const videoStatsArray = stats.remoteStats.videoStats;
        if (type === 'screen' && screenStatsArray) {
          this.stats = find(screenStatsArray, (item) => item.userId === userId);
        } else {
          this.stats = find(videoStatsArray, (item) => item.userId === userId);
        }
      }
    },
  },
  mounted() {
    if (!this.$IS_ELECTRON) {
      this.getVideoInfo();
      this.timer = setInterval(this.getVideoInfo, 1000);
    } else {
      this.addMediaStatsObserver();
    }
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer);
    this.$IS_ELECTRON && this.removeMediaStatsObserver();
  },
};
</script>

<style lang="less" scoped></style>
