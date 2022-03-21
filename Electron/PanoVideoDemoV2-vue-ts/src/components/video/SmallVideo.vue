<template>
  <div
    :id="videoId"
    @dblclick="toggleLock"
    :class="{
      'video-vertical': !isHorizontal,
      'video-vertical--active': !isHorizontal && active,
      'video-horizontal': isHorizontal,
      'video-horizontal--active': isHorizontal && active,
    }"
  >
    <PureVideo
      v-if="!mediaFlag && showMedia"
      :userId="user.userId"
      :videoType="showScreenShare ? 'screen' : 'video'"
      :mirror="false"
    />
    <CanvasVideo
      v-if="!mediaFlag && !showMedia"
      :userId="user.userId"
      :showScreenShare="showScreenShare"
      :direction="direction"
    />
    <Tooltip placement="bottom" :title="locked ? '解除锁定' : '锁定'">
      <span
        v-if="canLock"
        @click="toggleLock"
        :class="{
          iconfont: true,
          'icon-lock': locked,
          'icon-unlocked': !locked,
          'icon-lock-video': true,
        }"
      />
    </Tooltip>

    <Tooltip trigger="hover" destroyTooltipOnHide placement="bottom">
      <template slot="title">
        <VideoInfo
          :userId="user.userId"
          :type="showScreenShare ? 'screen' : 'video'"
          :userName="user.userName"
        />
      </template>
      <i class="iconfont icon-info-s video-info" />
    </Tooltip>

    <div v-if="mediaFlag" class="user-name" :style="{ fontSize: '24px' }">
      {{ user.userName || user.userId }}
      {{ showScreenShare ? '(Share)' : '' }}
      {{ isUserMe ? '(Me)' : '' }}
    </div>

    <div class="user-video-status">
      <div v-if="!mediaFlag" class="user-video-status__name">
        {{ user.userName || user.userId }}
        {{ showScreenShare ? '(Share)' : '' }}
        {{ isUserMe ? '(Me)' : '' }}
      </div>
      <UserAudio
        :fontSize="20"
        :userId="user.userId"
        :audioMuted="user.audioMuted"
      />
    </div>
  </div>
</template>

<script>
import { Tooltip } from 'ant-design-vue';
import { mapGetters } from 'vuex';
import PureVideo from './PureVideo.vue';
import CanvasVideo from './CanvasVideo.vue';
import VideoInfo from '@/components/VideoInfo.vue';
import UserAudio from '@/components/audio/UserAudio.vue';

export default {
  props: {
    user: Object,
    showScreenShare: Boolean,
    direction: String,
    data: Object,
  },
  data() {
    return {};
  },
  components: {
    CanvasVideo,
    PureVideo,
    Tooltip,
    VideoInfo,
    UserAudio,
  },
  computed: {
    ...mapGetters([
      'userMe',
      'getUserById',
      'mostActiveUser',
      'mainViewUserData',
      'isLocked',
      'isWhiteboardOpen',
      'screenSharingUser',
      'videoAnnotationUser',
    ]),
    isUserMe() {
      return this.user === this.userMe;
    },
    isHorizontal() {
      return this.direction === 'horizontal';
    },
    mediaFlag() {
      return (
        (this.user.videoMuted && !this.showScreenShare) ||
        (!this.user.screenOpen && this.showScreenShare)
      );
    },
    locked() {
      return this.isLocked(this.data);
    },
    showMedia() {
      return (
        !this.mainViewUserData ||
        this.data.user.userId !== this.mainViewUserData.user.userId ||
        this.data.type !== this.mainViewUserData.type
      );
    },
    videoId() {
      return `smallVideo-${this.user.userId}-${
        this.showScreenShare ? 'screen' : 'video'
      }`;
    },
    active() {
      return (
        this.mostActiveUser === this.user &&
        !this.user.showScreenShare &&
        !this.user.audioMuted
      );
    },
    canLock() {
      return (
        !this.user.videoMuted &&
        !this.isWhiteboardOpen &&
        !this.screenSharingUser &&
        !this.videoAnnotationUser
      );
    },
  },
  methods: {
    toggleLock() {
      if (this.canLock) {
        this.locked ? this.$emit('unlock') : this.$emit('lock');
      }
    },
  },
};
</script>

<style lang="less" scoped>
@infoZindex: 20;
.video-horizontal,
.video-vertical {
  width: 174px;
  height: 100px;
  display: inline-block;
  vertical-align: top; // 默认inlineblock是baseline对齐，如果内部元素高度不同导致整行不对齐
  align-items: center;
  // background-image: url(../../assets/img/poster.jpg);
  // background-image: #000;
  background-color: #333;
  background-position: center center;
  background-size: cover;
  position: relative;
  border: 2px solid #fff;
  overflow: hidden;
  flex-shrink: 0;
  flex-grow: 0;
  user-select: none;

  .video-info {
    visibility: hidden;
    position: absolute;
    bottom: 2px;
    left: 5px;
    color: white;
    font-size: 12px !important;
    cursor: pointer;
    background-color: rgba(0, 0, 0, 0.7);
    border-radius: 5px;
    width: 18px;
    z-index: 100;
    text-align: center;
  }
  &--active {
    border: 2px solid #68d56a;
  }

  &:hover .icon-lock-video {
    visibility: visible;
  }

  .icon-lock-video {
    visibility: hidden;
    font-size: 12px;
    position: absolute;
    right: 10px;
    top: 10px;
    cursor: pointer;
    transition: color 0.3s ease;
    border-radius: 5px;
    width: 18px;
    z-index: @infoZindex;
    color: white;
    cursor: pointer;
    background-color: rgba(0, 0, 0, 0.4);
    text-align: center;
    padding-left: 4px;
  }

  .icon-lock {
    visibility: visible;
  }

  &:hover {
    .video-info {
      visibility: visible;
    }
  }
}

.video-horizontal ~ .video-horizontal {
  margin-left: 20px;
}

.video-vertical ~ .video-vertical {
  margin-left: 0px;
}

.video-vertical {
  width: 240px;
  height: 134px;
  border-left: 2px solid black;
  border-right: 2px solid black;
  border-top: 1px solid black;
  border-bottom: 1px solid black;
  &--active {
    height: 136px;
    border: 2px solid #68d56a;
  }
}

.user-name {
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

.user-video-status {
  position: absolute;
  width: auto;
  bottom: 23px;
  right: 10px;
  z-index: @infoZindex;
  bottom: 2px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  right: 5px;
  padding: 0 5px;
  z-index: @infoZindex;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 5px;
  .user-video-status__name {
    max-width: 115px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 0;
    text-align: right;
    color: white;
    margin-right: 5px;
    font-size: 12px;
  }
}

.user-video-info {
  position: absolute;
  width: auto;
  left: 8px;
  top: 4px;
  padding: 2px 4px;
  border-radius: 4px;
  background: #000000;
  color: white;
  font-size: 12px;
}
</style>
