<template>
  <div class="audio-level" :style="{ fontSize: iFontSize }">
    <i v-if="audioMuted" class="iconfont icon-audio-off" />
    <template v-else>
      <i class="iconfont icon-audio" :style="{ color }" />
      <div
        class="audio-level__indicator"
        :style="{ 'background-color': color }"
      >
        <span
          class="indicator"
          :style="{ height: `${convertedLevel * 100}%` }"
        />
      </div>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    audioMuted: Boolean,
    fontSize: Number,
    level: Number,
    color: {
      type: String,
      required: false,
      default: '#fff',
    },
  },
  computed: {
    iFontSize() {
      return `${this.fontSize || 14}px`;
    },
    convertedLevel() {
      const { level } = this;
      const maxLevel = 0.1;
      return level / maxLevel > 1 ? 1 : level / maxLevel;
    },
  },
};
</script>

<style lang="less" scoped>
.audio-level {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  height: 1.1em;
  width: 1em;
  position: relative;
  .iconfont {
    font-size: 1em;
    line-height: normal;
  }
  &__indicator {
    position: absolute;
    width: 0.297em;
    height: 0.5em;
    top: 0.19em;
    left: 0.35em;
    border-radius: 0.175em;
    background-color: #fff;
    overflow: hidden;
    .indicator {
      position: absolute;
      width: 100%;
      bottom: 0;
      left: 0;
      background-color: #00ff00;
      transition: height 0.3s ease-in-out;
    }
  }
}
.icon-audio-off {
  color: red;
}
</style>
