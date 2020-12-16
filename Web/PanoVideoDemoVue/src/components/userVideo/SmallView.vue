<template>
  <div
    class="panoTinyVideo"
    :id="`smallVideo-${user.userId}-${showScreenShare ? 'screen' : 'video'}`"
  >
    <div
      class="videoDomRef"
      :style="{
        visibility:
          (showScreenShare && !user.screenOpen) ||
          (!showScreenShare && user.videoMuted)
            ? 'hidden'
            : 'visible',
        ...(this.isUserMe
          ? {
              transform: 'rotateY(180deg)' // 本地预览翻转
            }
          : {})
      }"
      ref="domRef"
    />

    <div class="userNameMainView" style="font-size: 24px">
      {{ user.userName || user.userId }}
      {{ isUserMe ? '(Me)' : '' }}
    </div>

    <!-- 放大或缩小视频按钮 -->
    <span
      class="iconfont icon-enlarge iconEnlarge"
      v-if="!user.videoMuted"
      @click="onEnlarge"
    />

    <!-- 显示在 view 中部的大号用户名 -->
    <div class="userVideoStatus">
      <div class="userVideoUserName" v-if="!user.videoMuted || showScreenShare">
        {{ user.userName || user.userId }}
        {{ showScreenShare ? '(Share)' : '' }}
        {{ isUserMe ? '(Me)' : '' }}
      </div>
      <i
        v-if="user.audioMuted"
        class="iconfont icon-microphone-slash"
        :style="{ color: 'red', fontSize: '14px' }"
      />
      <i
        v-else
        class="iconfont icon-microphone"
        :style="{
          color: user.isSpeaking ? '#00ff00' : '#fff',
          fontSize: '14px'
        }"
      />
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters, mapMutations } from 'vuex';
import { get } from 'lodash-es';

const RENDER_RATE = 20;

export default {
  data() {
    return {
      renderInterval: '',
      smallViewCanvas: document.createElement('canvas')
    };
  },
  props: {
    user: {
      type: Object,
      required: true
    },
    forceUseSrcView: {
      // 打开白板时强制使用 video tag 渲染小图
      type: Boolean,
      default: false
    },
    showScreenShare: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    ['user.showInMainView']() {
      this.updateVideoView();
    },
    ['user.isScreenInMainView']() {
      this.updateVideoView();
    },
    forceUseSrcView() {
      this.updateVideoView();
    }
  },
  computed: {
    ...mapGetters(['mainViewUser', 'userMe']),
    isUserMe() {
      return this.user.userId === this.userMe.userId;
    }
  },
  methods: {
    ...mapMutations(['lockUser', 'setWhiteboardOpenState']),
    ...mapActions(['setAsMainView']),
    onEnlarge() {
      this.lockUser(this.user.userId);
      this.setWhiteboardOpenState(false);
      this.setAsMainView({
        user: this.user,
        screenShareAsMainView: this.showScreenShare
      });
    },
    updateVideoView() {
      const userViewRef = this.showScreenShare
        ? this.user.screenDomRef
        : this.user.videoDomRef;
      if (
        !this.user.showInMainView ||
        (this.showScreenShare && !this.user.isScreenInMainView) ||
        (!this.showScreenShare &&
          this.user.screenOpen &&
          this.user.isScreenInMainView) ||
        this.forceUseSrcView
      ) {
        // 大多数用户直接显示video就可以了
        this.$refs.domRef.innerHTML = '';
        this.$refs.domRef.appendChild(userViewRef);
        const video = userViewRef.getElementsByTagName('video');
        if (video.length) {
          video[0].autoplay = true;
          video[0].play();
        }
      } else {
        clearInterval(this.renderInterval);
        this.$refs.domRef.innerHTML = '';
        this.$refs.domRef.appendChild(this.smallViewCanvas);
        // 在大图显示的用户同时在小图区域也显示一个图像
        this.renderInterval = setInterval(
          this.renderSmallVideo,
          1000 / RENDER_RATE
        );
      }
    },
    renderSmallVideo() {
      const userViewRef = this.showScreenShare
        ? this.user.screenDomRef
        : this.user.videoDomRef;
      const bigViewImageSrc = get(userViewRef.getElementsByTagName('video'), 0);
      if (!bigViewImageSrc) return;
      const samllViewRect = this.$refs.domRef.getBoundingClientRect();
      this.smallViewCanvas.width = bigViewImageSrc.videoWidth;
      this.smallViewCanvas.height = bigViewImageSrc.videoHeight;
      if (
        samllViewRect.width / bigViewImageSrc.videoWidth <
        samllViewRect.height / bigViewImageSrc.videoHeight
      ) {
        this.smallViewCanvas.style.zoom = `${samllViewRect.width /
          bigViewImageSrc.videoWidth}`;
      } else {
        this.smallViewCanvas.style.zoom = `${samllViewRect.height /
          bigViewImageSrc.videoHeight}`;
      }
      if (!this.smallViewRenderCtx) {
        this.smallViewRenderCtx = this.smallViewCanvas.getContext('2d');
      }
      const { smallViewRenderCtx } = this;
      smallViewRenderCtx.clearRect(
        0,
        0,
        this.smallViewCanvas.width,
        this.smallViewCanvas.height
      );
      this.smallViewCanvas.width &&
        smallViewRenderCtx.drawImage(
          bigViewImageSrc,
          0,
          0,
          this.smallViewCanvas.width,
          this.smallViewCanvas.height
        );
    }
  },
  mounted() {
    this.updateVideoView();
  },
  beforeDestroy() {
    clearInterval(this.renderInterval);
  }
};
</script>

<style lang="scss" src="./uservideo.scss"></style>
