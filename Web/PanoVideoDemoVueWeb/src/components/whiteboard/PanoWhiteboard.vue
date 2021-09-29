<template>
  <div class="pvcWbWrapper">
    <div class="pvc-whiteboard-rapper" ref="whiteboardWrapper"></div>
    <Toolbar v-if="whiteboardLoaded" :whiteboard="whiteboard" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Toolbar from './Toolbar';

export default {
  data() {
    return {
      whiteboard: window.rtcWhiteboard,
      // whiteboardLoaded 控制每次进入meeting页面，白板只会初始化一次，
      // 可以优化白板打开速度，等到退出会议界面再销毁白板和toolbar
      whiteboardLoaded: false
    };
  },
  computed: {
    ...mapGetters(['isWhiteboardOpen'])
  },
  watch: {
    isWhiteboardOpen() {
      // 在 mounted 和 isWhiteboardOpen 变化时检测是否初始化过白板，但是白板只会初始化一次
      // 关闭白板再打开不会再次初始化
      if (this.isWhiteboardOpen && !this.whiteboardLoaded) {
        this.initWhiteboard();
      }
    }
  },
  components: {
    Toolbar
  },
  methods: {
    initWhiteboard() {
      this.whiteboard = window.rtcWhiteboard;
      this.whiteboardLoaded = true;
      this.$nextTick(() => {
        window.rtcWhiteboard.open(this.$refs.whiteboardWrapper);
        setTimeout(() => {
          window.rtcWhiteboard.updateCanvasSize();
        }, 1000);
      });
    }
  },
  mounted() {
    if (this.isWhiteboardOpen && !this.whiteboardLoaded) {
      this.initWhiteboard();
    }
  },
  beforeDestroy() {
    this.whiteboardLoaded = false;
  }
};
</script>

<style lang="scss" scoped>
.pvcWbWrapper {
  width: 100%;
  height: 100%;
  background-color: #fff;
  position: relative;
  .pvc-whiteboard-rapper {
    width: 100%;
    height: 100%;
    background-color: #fff;
    position: relative;
  }
}
</style>
