<template>
  <div class="pvcWbWrapper" ref="domRef">
    <Toolbar :whiteboard="whiteboard" />
  </div>
</template>

<script>
import Toolbar from './Toolbar';

export default {
  data() {
    return {
      whiteboard: window.rtcWhiteboard
    };
  },
  components: {
    Toolbar
  },
  mounted() {
    window.rtcWhiteboard.open(this.$refs.domRef);
    // whiteboardToolbar.install(window.rtcWhiteboard);
    setTimeout(() => {
      window.rtcWhiteboard.updateCanvasSize();
    }, 2000);
  }
};
</script>

<style lang="scss" scoped>
.pvcWbWrapper {
  width: 100%;
  height: 100%;
  background-color: #fff;
  position: relative;
}
</style>
