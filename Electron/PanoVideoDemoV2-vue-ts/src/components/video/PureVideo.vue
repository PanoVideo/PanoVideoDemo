<template>
  <div
    class="videoDomRef"
    :style="{
      transform: mirror ? 'rotateY(180deg)' : 'rotateY(0deg)',
    }"
    ref="domRef"
  />
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  data() {
    return {
      lastUserId: this.userId,
      lastVideoType: this.videoType,
    };
  },
  props: {
    userId: String,
    videoType: {
      validator(value) {
        return ['video', 'screen'].indexOf(value) !== -1;
      },
    },
    mirror: Boolean,
  },
  computed: {
    ...mapGetters(['getUserById']),
  },
  watch: {
    userId() {
      this.insertUserVideoDom();
    },
    videoType() {
      this.insertUserVideoDom();
    },
  },
  methods: {
    insertUserVideoDom() {
      const { userId } = this;
      const user = this.getUserById(userId);
      if (!user) return;
      const userViewRef =
        this.videoType === 'screen' ? user.screenDomRef : user.videoDomRef;
      userViewRef.parentElement?.removeChild(userViewRef);
      this.$refs.domRef.innerHTML = '';
      this.$refs.domRef.appendChild(userViewRef);
      const video = userViewRef.getElementsByTagName('video');
      if (video.length) {
        video[0].autoplay = true;
        video[0].play();
      }
    },
  },
  mounted() {
    this.insertUserVideoDom();
  },
};
</script>

<style lang="less" scoped>
.videoDomRef {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  top: 0;
  left: 0;
  position: absolute;
  z-index: 1;
  overflow: hidden;
  & > div {
    background-color: #000;
  }
}
</style>
