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
        ...(this.isUserMe
          ? {
              transform: 'rotateY(180deg)' // 本地预览翻转
            }
          : {})
      }"
      ref="domRef"
    />

    <div class="userLocked" @click="onUnLock" v-if="mainViewUser.locked">
      <span class="iconfont icon-lock1" /> 取消锁定
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
      <i
        class="iconfont icon-microphone-slash"
        v-if="mainViewUser.audioMuted"
      />
      <i
        class="iconfont icon-microphone"
        v-else
        :style="{
          color: mainViewUser.isSpeaking ? '#00ff00' : '#fff',
          fontSize: '14px'
        }"
      />
    </div>
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';

export default {
  computed: {
    ...mapGetters(['mainViewUser', 'userMe', 'isWhiteboardOpen']),
    isUserMe() {
      return this.mainViewUser.userId === this.userMe.userId;
    },
    userViewRef() {
      return this.mainViewUser.isScreenInMainView
        ? this.mainViewUser.screenDomRef
        : this.mainViewUser.videoDomRef;
    }
  },
  watch: {
    ['mainViewUser.userId']() {
      this.updateVideoView();
    },
    ['mainViewUser.videoMuted']() {
      this.updateVideoView();
    },
    isWhiteboardOpen() {
      this.updateVideoView();
    }
  },
  methods: {
    ...mapMutations(['updateUser']),
    onUnLock() {
      this.updateUser({ userId: this.mainViewUser.userId, locked: false });
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
    this.updateVideoView();
  }
};
</script>

<style lang="scss" src="./uservideo.scss"></style>
