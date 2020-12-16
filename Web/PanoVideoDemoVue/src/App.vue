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
  }
};
</script>

<style lang="scss">
.pano-demo-root {
  width: 100%;
  height: 100%;
}
</style>
