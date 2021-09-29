<template>
  <div class="pano-demo-root">
    <router-view />
    <Setting />
  </div>
</template>

<script>
import Setting from './components/Setting';

export default {
  components: {
    Setting
  },
  methods: {
    checkBrowserSupport() {
      if (!window.rtcEngine.checkEnvRequirement()) {
        this.$confirm(
          '您当前使用的浏览器可能无法使用音视频通信功能，建议使用最新版本的Chrome(≥69)或Edge(≥19)等浏览器',
          '提示'
        );
      }
    }
  },
  mounted() {
    this.checkBrowserSupport();
  }
};
</script>

<style lang="scss">
.pano-demo-root {
  width: 100%;
  height: 100%;
}
</style>
