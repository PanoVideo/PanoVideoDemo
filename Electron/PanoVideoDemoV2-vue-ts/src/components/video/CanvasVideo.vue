<template>
  <div
    class="video-canvas-renderer"
    :style="{
      ...(!$IS_ELECTRON && isUserMe
        ? {
            transform: 'rotateY(180deg)',
          }
        : {}),
    }"
    ref="domRef"
  />
</template>

<script>
import { mapGetters } from 'vuex';
import { get } from 'lodash-es';

const RENDER_RATE = 10;

/**
 * 当某个人的视频或共享作为主视图时，他在小图列表中的小图从大图渲染出来
 */
export default {
  props: {
    userId: String,
    direction: String,
    showScreenShare: Boolean,
  },
  data() {
    return {
      renderInterval: undefined,
      smallViewCanvas: document.createElement('canvas'),
    };
  },
  computed: {
    ...mapGetters(['getUserById', 'userMe']),
    isUserMe() {
      return this.userId === this.userMe.userId;
    },
  },
  methods: {
    renderSmallVideo() {
      const user = this.getUserById(this.userId);
      if (!user) return;
      const flag = this.direction === 'horizontal';
      const canvasWith = flag ? 170 : 236;
      const canvasHeiht = flag ? 96 : 132;
      if (canvasWith * devicePixelRatio !== this.smallViewCanvas.width) {
        this.smallViewCanvas.width = canvasWith * devicePixelRatio;
        this.smallViewCanvas.height = canvasHeiht * devicePixelRatio;
        this.smallViewCanvas.style.zoom = `${1 / devicePixelRatio}`;
      }
      const userViewRef = this.showScreenShare
        ? user.screenDomRef
        : user.videoDomRef;
      const bigViewImageSrc =
        get(userViewRef.getElementsByTagName('video'), 0) ||
        get(userViewRef.getElementsByTagName('canvas'), 0);
      if (!bigViewImageSrc) return;
      if (!this.smallViewCanvas.parentElement) return;
      const { smallViewRenderCtx } = this;
      const samllViewRect = this.$refs.domRef.getBoundingClientRect();
      const bigViewRect = bigViewImageSrc.getBoundingClientRect();
      let zoom = 1;
      if (bigViewImageSrc instanceof HTMLCanvasElement) {
        if (
          samllViewRect.width / bigViewRect.width <
          samllViewRect.height / bigViewRect.height
        ) {
          zoom = samllViewRect.width / bigViewRect.width;
        } else {
          zoom = samllViewRect.height / bigViewRect.height;
        }
      }

      if (bigViewImageSrc instanceof HTMLVideoElement) {
        if (
          samllViewRect.width / bigViewImageSrc.videoWidth <
          samllViewRect.height / bigViewImageSrc.videoHeight
        ) {
          zoom = samllViewRect.width / bigViewImageSrc.videoWidth;
        } else {
          zoom = samllViewRect.height / bigViewImageSrc.videoHeight;
        }
      }

      smallViewRenderCtx.clearRect(
        0,
        0,
        this.smallViewCanvas.width,
        this.smallViewCanvas.height
      );
      // electron 的自己的小图是翻转的，需要 mirror
      const isUserMe = user.userId === this.userMe.userId;
      if (this.$IS_ELECTRON && isUserMe) {
        smallViewRenderCtx.translate(this.smallViewCanvas.width, 0);
        smallViewRenderCtx.scale(-1, 1);
      }
      const renderWidth =
        (bigViewImageSrc.width || bigViewImageSrc.videoWidth) *
        zoom *
        devicePixelRatio;
      const renderHeight =
        (bigViewImageSrc.height || bigViewImageSrc.videoHeight) *
        zoom *
        devicePixelRatio;
      this.smallViewRenderCtx.fillStyle = '#000';
      this.smallViewRenderCtx.fillRect(
        0,
        0,
        this.smallViewCanvas.width,
        this.smallViewCanvas.height
      );

      if (!this.$IS_ELECTRON) {
        let rotate = null;
        const m1 = bigViewImageSrc.style.transform.match(/rotate\((\d+)/);
        if (m1 && m1[1]) {
          rotate = parseInt(m1[1], 10);
        }
        this.smallViewCanvas.style.transform = bigViewImageSrc.style.transform;
        // 注意如果旋转180度的倍数，那么长边width还是保持100%
        if (typeof rotate === 'number' && rotate % 180 !== 0) {
          this.smallViewCanvas.style.width = `${this.smallViewCanvas.parentElement?.clientHeight}px`;
          this.smallViewCanvas.parentElement.style.backgroundColor = `black`;
        } else {
          this.smallViewCanvas.style.width = `100%`;
        }
      }
      const width = bigViewImageSrc.width || bigViewImageSrc.videoWidth;
      width &&
        smallViewRenderCtx.drawImage(
          bigViewImageSrc,
          0,
          0,
          bigViewImageSrc.width || bigViewImageSrc.videoWidth,
          bigViewImageSrc.height || bigViewImageSrc.videoHeight,
          (this.smallViewCanvas.width - renderWidth) / 2,
          (this.smallViewCanvas.height - renderHeight) / 2,
          renderWidth,
          renderHeight
        );
      if (this.$IS_ELECTRON && isUserMe) {
        smallViewRenderCtx.scale(-1, 1);
        smallViewRenderCtx.translate(-this.smallViewCanvas.width, 0);
      }
    },
  },
  mounted() {
    const user = this.getUserById(this.userId);
    if (!user) return;
    if (this.$refs.domRef) {
      this.renderInterval && clearInterval(this.renderInterval);
      this.$refs.domRef.innerHTML = '';
      this.$refs.domRef.appendChild(this.smallViewCanvas);
      this.smallViewRenderCtx = this.smallViewCanvas.getContext('2d');
      this.renderInterval = setInterval(
        this.renderSmallVideo,
        1000 / RENDER_RATE
      );
    }
  },
  beforeDestroy() {
    this.renderInterval && clearInterval(this.renderInterval);
  },
};
</script>

<style lang="less" scoped>
@videoZindex: 10;
.video-canvas-renderer {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  margin: auto;
  display: block;
  background-position: center center;
  background-size: cover;
  position: absolute;
  z-index: @videoZindex;
  & > div {
    background-color: #333;
  }
  /deep/ canvas {
    width: 100%;
    height: 100%;
  }
  div {
    width: 100%;
  }
  /deep/ .userNameMainView {
    display: none !important;
  }
}
</style>
