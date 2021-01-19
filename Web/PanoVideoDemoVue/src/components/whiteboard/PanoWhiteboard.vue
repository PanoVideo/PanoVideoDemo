<template>
  <div class="pvcWbWrapper">
    <div class="pvc-whiteboard-rapper" ref="whiteboardWrapper"></div>
    <Toolbar :whiteboard="whiteboard" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Toolbar from './Toolbar';

let whiteboardLoaded = false;

export default {
  data() {
    return {
      whiteboard: window.rtcWhiteboard
    };
  },
  computed: {
    ...mapGetters(['isWhiteboardOpen'])
  },
  watch: {
    isWhiteboardOpen() {
      if (this.isWhiteboardOpen && !whiteboardLoaded) {
        this.initWhiteboard();
      }
    }
  },
  components: {
    Toolbar
  },
  methods: {
    initWhiteboard() {
      whiteboardLoaded = true;
      this.$nextTick(() => {
        window.rtcWhiteboard.open(this.$refs.whiteboardWrapper);
        setTimeout(() => {
          window.rtcWhiteboard.updateCanvasSize();
        }, 2000);
      });
    }
  },
  mounted() {
    if (this.isWhiteboardOpen && !whiteboardLoaded) {
      this.initWhiteboard();
    }
  },
  beforeDestroy() {
    whiteboardLoaded = false;
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
