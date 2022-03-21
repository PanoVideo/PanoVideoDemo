<template>
  <div v-if="mainViewUserData" class="pvc-main-view">
    <PureVideo
      v-if="showMedia"
      :userId="user.userId"
      :videoType="videoType"
      :mirror="
        (!$IS_ELECTRON && isUserMe && !user.videoAnnotationOn) ||
        ($IS_ELECTRON && isUserMe && user.videoAnnotationOn)
      "
    />
    <MeetingInfo />
    <MainViewTopBar @unlock="handlerUnlock" />
    <FullScreenButton
      class="fullscreen"
      @changed="handlerFullscrrenAction($event)"
    />

    <!-- 显示在 view 中部的大号用户名 -->
    <div
      v-if="showCenterName"
      class="user-name-main-view"
      :style="{ fontSize: '80px' }"
    >
      {{ userName || userId }}
      {{ isUserMe ? '(Me)' : '' }}
    </div>

    <!-- 视频标注 -->
    <VideoAnnotation />

    <!-- 底部的状态条和用户名 -->
    <div class="user-video-status">
      <div v-if="showBottomName" class="user-video-user-name">
        {{ userName || userId }}
        {{ videoType === MediaType.screen ? '(Share)' : '' }}
        {{ isUserMe ? '(Me)' : '' }}
      </div>
      <UserAudio
        :fontSize="22"
        :userId="user.userId"
        :audioMuted="user.audioMuted"
      />
    </div>
  </div>
</template>

<script>
import MeetingInfo from '@/components/MeetingInfo.vue';
import PureVideo from './PureVideo.vue';
import MainViewTopBar from '@/components/MainViewTopBar.vue';
import FullScreenButton from '@/components/FullscreenButton.vue';
import VideoAnnotation from '@/components/annotation/VideoAnnotation';
import UserAudio from '@/components/audio/UserAudio';
import { LOCK_USER, UPDATE_MEDIALAYOUT } from '@/store/mutations';
import { MediaType } from '@/store/modules/user';
import { mapGetters, mapMutations } from 'vuex';
import { LogUtil } from '@/utils/';

export default {
  data() {
    return {
      adminControl: false,
      controller: undefined,
      MediaType,
    };
  },
  components: {
    MeetingInfo,
    PureVideo,
    MainViewTopBar,
    FullScreenButton,
    UserAudio,
    VideoAnnotation,
  },
  watch: {
    mainViewUserData() {
      LogUtil('MainViewUserData changed:', this.mainViewUserData);
    },
  },
  computed: {
    ...mapGetters([
      'getUserById',
      'userMe',
      'mainViewUser',
      'mainViewUserData',
    ]),
    user() {
      return this.mainViewUser;
    },
    showMedia() {
      return !this.user.videoMuted || this.user.screenOpen;
    },
    videoType() {
      return this.mainViewUserData.type == MediaType.screen &&
        this.user !== this.userMe
        ? MediaType.screen
        : MediaType.video;
    },
    showCenterName() {
      return this.user.videoMuted && this.videoType === MediaType.video;
    },
    showBottomName() {
      return !this.user.videoMuted || this.videoType === MediaType.screen;
    },
    isUserMe() {
      return this.user.userId === this.userMe.userId;
    },
    userName() {
      return this.user.userName;
    },
    userId() {
      return this.user.userId;
    },
  },
  methods: {
    ...mapMutations([LOCK_USER, UPDATE_MEDIALAYOUT]),
    handlerUnlock() {
      this.$store.commit(LOCK_USER, { userId: '' });
    },
    handlerFullscrrenAction(fullscreen) {
      this.updateMediaLayout({ fullscreen });
    },
  },
};
</script>

<style lang="less" scoped>
@infoZindex: 20;
@top-icon-size: 26px;

.pvc-main-view {
  display: block;
  height: 100%;
  overflow: hidden;
  position: relative;
  user-select: none;

  .user-video-status {
    position: absolute;
    display: flex;
    width: auto;
    bottom: 23px;
    right: 10px;
    padding: 0 5px;
    justify-content: flex-end;
    align-items: center;
    z-index: @infoZindex;
    background-color: rgba(0, 0, 0, 0.7);
    border-radius: 5px;
    z-index: @infoZindex;
    .user-video-user-name {
      max-width: 400px;
    }
  }

  .user-video-user-name {
    max-width: 115px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 0;
    text-align: right;
    color: white;
    font-size: 14px;
    margin-right: 15px;
  }

  .user-name-main-view {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 80px;
    color: #fff;
    text-align: center;
    z-index: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 90%;
    user-select: none;
  }

  .fullscreen {
    position: absolute;
    top: 20px;
    right: 30px;
    width: @top-icon-size;
    height: @top-icon-size;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
    background-color: rgba(0, 0, 0, 0.7);
    border: 1px solid rgba(0, 0, 0, 0.4);
    z-index: @infoZindex;
    color: #fff;
    font-size: 12px;
    cursor: pointer;
    user-select: none;
    i {
      font-size: 12px;
    }
    &__info {
      position: absolute;
      top: 20px;
      left: 30px;
      font-size: 16px !important;
    }
  }
}
</style>
