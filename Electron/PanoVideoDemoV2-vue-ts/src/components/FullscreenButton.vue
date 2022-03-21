<template>
  <a-button
    size="small"
    type="primary"
    :style="{ marginLeft: '10px' }"
    :icon="fullscreenIcon"
    @click="onToggleFullScreen"
  />
</template>

<script>
import { Button } from 'ant-design-vue';
import { isFullScreen, toggleFullScreen } from '@/utils/common';

export default {
  props: {
    userId: String,
    audioMuted: Boolean,
  },
  components: {
    'a-button': Button,
  },
  computed: {
    fullscreenIcon() {
      return isFullScreen ? 'fullscreen' : 'fullscreen-exit';
    },
  },
  methods: {
    onToggleFullScreen() {
      const fullscreen = isFullScreen();
      toggleFullScreen();
      this.$emit('changed', !fullscreen);
    },
  },
};
</script>

<style lang="less" scoped></style>
