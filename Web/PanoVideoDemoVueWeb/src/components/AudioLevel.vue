<template>
  <i
    :class="{
      iconfont: true,
      'icon-microphone-slash': audioMuted,
      'icon-microphone': !audioMuted
    }"
    :style="{
      color: audioMuted ? 'red' : '#fff',
      fontSize: `${fontSize}px`
    }"
  >
    <i class="audio-indicator" v-show="!audioMuted">
      <i
        class="audio-indicator__level"
        :style="{
          height: `${level * 100}%`
        }"
      />
    </i>
  </i>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  props: {
    audioMuted: { type: Boolean, required: true },
    userId: { type: String, required: true },
    fontSize: { type: Number, default: 14 }
  },
  computed: {
    ...mapGetters(['userAudioLevel']),
    level() {
      const userAudioLevel = this.userAudioLevel[this.userId] || 0;
      const maxLevel = 0.1;
      return userAudioLevel / maxLevel > 1 ? 1 : userAudioLevel / maxLevel;
    }
  }
};
</script>

<style lang="scss" scoped>
.iconfont {
  color: #fff;
  position: relative;
}
.audio-indicator {
  position: absolute;
  width: 0.35em;
  height: 63%;
  bottom: 37%;
  left: 32.5%;
  border-radius: 0.175em;
  background-color: #fff;
  overflow: hidden;
  &__level {
    position: absolute;
    width: 100%;
    bottom: 0;
    left: 0;
    background-color: #00ff00;
  }
}
</style>
