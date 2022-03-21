<template>
  <AudioLevel
    :audioMuted="audioMuted"
    :fontSize="fontSize"
    :level="level"
    :color="color"
  />
</template>

<script>
import AudioLevel from './AudioLevel.vue';

export default {
  data() {
    return {
      level: 0,
      interval: 0,
    };
  },
  props: {
    userId: String,
    audioMuted: Boolean,
    fontSize: Number,
    color: {
      type: String,
      required: false,
    },
  },
  components: {
    AudioLevel,
  },
  computed: {
    isVoip() {
      return this.audioType === 'voip';
    },
  },
  mounted() {
    this.interval = setInterval(() => {
      this.level = userAudioLevel[this.userId] || 0;
    }, 200);
  },
  beforeDestroy() {
    clearInterval(this.interval);
  },
};
</script>

<style lang="less" scoped></style>
