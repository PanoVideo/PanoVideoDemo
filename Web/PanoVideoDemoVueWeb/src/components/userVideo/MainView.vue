<template>
  <div class="panoMainViewVideo">
    <div
      class="videoDomRef"
      :style="{
        visibility:
          (mainViewUser.isScreenInMainView && !mainViewUser.screenOpen) ||
          (!mainViewUser.isScreenInMainView && mainViewUser.videoMuted)
            ? 'hidden'
            : 'visible',
        ...(this.isUserMe && !videoAnnotationOpen
          ? {
              transform: 'rotateY(180deg)' // 本地预览翻转
            }
          : {})
      }"
      ref="domRef"
    />

    <div class="mv__top-btns">
      <div
        class="mv__top-btns__btn"
        @click="onUnLock"
        v-if="mainViewUser.locked"
      >
        <span class="iconfont icon-lock1" />
        <span>取消锁定</span>
      </div>
      <div
        class="mv__top-btns__btn"
        @click="toggleAnnotation"
        v-if="isUserMe && !userMe.videoMuted"
      >
        <span>{{ userMe.videoAnnotationOpen ? '关闭' : '打开' }}视频标注</span>
      </div>
    </div>

    <!-- 显示在 view 中部的大号用户名 -->
    <div
      class="userNameMainView"
      :style="{ fontSize: '80px' }"
      v-show="mainViewUser.videoMuted && !mainViewUser.isScreenInMainView"
    >
      {{ mainViewUser.userName || mainViewUser.userId }}
      {{ isUserMe && '(Me)' }}
    </div>

    <!-- 视频标注 -->
    <div class="annotation" ref="annotationRef" />

    <AnnotationToolbar
      v-if="
        (videoAnnotationOpen || shareAnnotationOpen) && annotationWhiteboard
      "
      :whiteboard="annotationWhiteboard"
    />

    <!-- 底部的状态条和用户名  -->
    <div class="userVideoStatus">
      <div
        class="userVideoUserName"
        v-show="!mainViewUser.videoMuted || mainViewUser.isScreenInMainView"
      >
        {{ mainViewUser.userName || mainViewUser.userId }}
        {{ mainViewUser.isScreenInMainView ? '(Share)' : '' }}
        {{ isUserMe ? '(Me)' : '' }}
      </div>
      <AudioLevel
        :userId="mainViewUser.userId"
        :audioMuted="mainViewUser.audioMuted"
        :fontSize="18"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { RtsService } from '@pano.video/panortc';
import AudioLevel from '@/components/AudioLevel';
import AnnotationToolbar from '@/components/annotation/AnnotationToolbar';

export default {
  data() {
    return {
      controller: undefined,
      annotationWhiteboard: undefined
    };
  },
  components: {
    AnnotationToolbar,
    AudioLevel
  },
  computed: {
    ...mapGetters(['mainViewUser', 'userMe', 'isWhiteboardOpen']),
    isUserMe() {
      return this.mainViewUser.userId === this.userMe.userId;
    },
    userViewRef() {
      return this.mainViewUser.isScreenInMainView
        ? this.mainViewUser.screenDomRef
        : this.mainViewUser.videoDomRef;
    },
    videoAnnotationOpen() {
      return this.mainViewUser.videoAnnotationOpen;
    },
    shareAnnotationOpen() {
      return this.mainViewUser.shareAnnotationOpen;
    }
  },
  watch: {
    ['mainViewUser.userId']() {
      this.updateVideoView();
      this.checkAnnotation();
    },
    ['mainViewUser.isScreenInMainView']() {
      this.updateVideoView();
      this.checkAnnotation();
    },
    ['mainViewUser.videoMuted']() {
      this.updateVideoView();
    },
    ['mainViewUser.videoAnnotationOpen']() {
      this.checkAnnotation();
    },
    ['mainViewUser.shareAnnotationOpen']() {
      this.checkAnnotation();
    },
    isWhiteboardOpen() {
      this.updateVideoView();
    }
  },
  methods: {
    ...mapMutations(['updateUser']),
    toggleAnnotation() {
      this.updateUser({
        userId: this.mainViewUser.userId,
        videoAnnotationOpen: !this.mainViewUser.videoAnnotationOpen
      });
      if (!this.mainViewUser.videoAnnotationOpen) {
        this.annotationWhiteboard?.close();
      }
    },
    onUnLock() {
      this.updateUser({ userId: this.mainViewUser.userId, locked: false });
    },
    async checkAnnotation() {
      if (
        this.annotationWhiteboard &&
        this.annotationWhiteboard.tragetUserId !== this.mainViewUser.userId
      ) {
        // 如果标注白板对象存在，这种情况是主视图用户切换的情况，需要先关闭之前的标注
        this.annotationWhiteboard.close();
        this.annotationWhiteboard = undefined;
      }
      if (
        (this.mainViewUser.isScreenInMainView &&
          this.mainViewUser.shareAnnotationOpen) ||
        (!this.mainViewUser.isScreenInMainView &&
          this.mainViewUser.videoAnnotationOpen)
      ) {
        this.openAnnotation();
      } else {
        this.closeAnnotation();
      }
    },
    openAnnotation() {
      this.annotationWhiteboard = RtsService.getInstance().getAnnotation(
        this.mainViewUser.userId,
        this.mainViewUser.isScreenInMainView ? 'share' : 'video'
      );
      window.annotationWhiteboard = this.annotationWhiteboard;
      if (!this.annotationWhiteboard.isWhiteboardOpen) {
        this.annotationWhiteboard.open(this.$refs.annotationRef);
      }
      if (
        !this.mainViewUser.isScreenInMainView &&
        this.mainViewUser.videoDomRef
      ) {
        // 视频标注的情况，需要主动设置自己的分辨率信息
        const videoDom = this.mainViewUser.videoDomRef.getElementsByTagName(
          'video'
        )[0];
        if (!videoDom) {
          return;
        }
        this.annotationWhiteboard.setAnnotationViewSize({
          width: videoDom.videoWidth,
          height: videoDom.videoHeight
        });
      }
    },
    closeAnnotation() {
      this.annotationWhiteboard?.close();
      if (this.annotationWhiteboard?.tragetUserId === this.userMe.userId) {
        this.annotationWhiteboard?.stop();
      }
      this.annotationWhiteboard = undefined;
    },
    updateVideoView() {
      if (!this.$refs.domRef) return;
      this.$refs.domRef.innerHTML = '';
      this.$refs.domRef.appendChild(this.userViewRef);
      const video = this.userViewRef.getElementsByTagName('video');
      if (video.length) {
        video[0].autoplay = true;
        video[0].play();
      }
    }
  },
  mounted() {
    this.checkAnnotation();
    this.updateVideoView();
  },
  beforeDestroy() {
    this.closeAnnotation();
    this.annotationWhiteboard = undefined;
  }
};
</script>

<style lang="scss" src="./uservideo.scss"></style>
<style lang="scss">
.mv__top-btns {
  position: absolute;
  top: 20px;
  left: 20px;
  height: 28px;
  display: inline-flex;
  line-height: 28px;

  &__btn {
    width: auto !important;
    height: 100%;
    display: flex;
    align-items: center;
    border-radius: 4px;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 20;
    color: #fff;
    width: auto;
    padding: 0 10px;
    font-size: 12px;
    cursor: pointer;
    user-select: none;
    span {
      padding-left: 5px;
      font-size: 12px;
    }
  }
}
.annotation {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  margin: auto;
  display: block;
  position: absolute;
  z-index: 11;
}
</style>
