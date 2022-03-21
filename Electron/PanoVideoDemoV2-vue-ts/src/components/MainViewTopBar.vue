<template>
  <!-- 左上角的按钮 -->
  <div class="topIcons">
    <!-- 视频分辨率信息 -->
    <Tooltip
      arrowPointAtCenter
      trigger="click"
      destroyTooltipOnHide
      :visible="toolTipVisible"
      placement="bottomRight"
      :getPopupContainer="
        (triggerNode) => {
          return triggerNode.parentNode;
        }
      "
    >
      <template slot="title">
        <VideoInfo
          :userId="userId"
          :type="showScreenShare ? 'screen' : 'video'"
          :userName="userName"
        />
      </template>
      <div class="userInfoIcon" @click="toolTipVisible = !toolTipVisible">
        <div class="iconfont icon-info-s" />
      </div>
    </Tooltip>
    <!-- 锁定用户 -->
    <Tooltip destroyTooltipOnHide placement="bottom">
      <template slot="title">
        <span :style="{ fontSize: '12px' }">解除锁定</span>
      </template>
      <div v-if="locked" class="userLockIcon" @click="$emit('unlock')">
        <i class="iconfont icon-lock" />
      </div>
    </Tooltip>
  </div>
</template>

<script>
import { Tooltip } from 'ant-design-vue';
import { mapGetters } from 'vuex';
import VideoInfo from '@/components/VideoInfo.vue';
import { MediaType } from '@/store/modules/user';

export default {
  data() {
    return {
      toolTipVisible: false,
    };
  },
  components: {
    VideoInfo,
    Tooltip,
  },
  computed: {
    ...mapGetters([
      'mainViewUserData',
      'isHost',
      'isLocked',
      'mainViewUser',
      'userMe',
      'whiteboardAvailable',
    ]),
    userId() {
      return this.mainViewUser.userId;
    },
    userName() {
      return this.mainViewUser.userName;
    },
    locked() {
      return this.isLocked(this.mainViewUserData);
    },
    showScreenShare() {
      return (
        this.mainViewUserData.type === MediaType.screen &&
        this.mainViewUser !== this.userMe
      );
    },
  },
};
</script>

<style lang="less" scoped>
@infoZindex: 20;
@top-icon-size: 26px;

.topIcons {
  position: absolute;
  top: 20px;
  left: 40px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  z-index: @infoZindex;
  color: #fff;
  font-size: 12px;
  user-select: none;
}

.userInfoIcon,
.userLockIcon {
  height: @top-icon-size;
  display: flex;
  padding: 0 5px;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.7);
  cursor: pointer;
  margin-right: 10px;
  i {
    font-size: 12px;
  }
}

.userLockIcon {
  padding-left: 10px;
}

.user-video-info {
  width: auto;
  padding: 2px 4px;
  border-radius: 4px;
  background: #000000;
  color: white;
  font-size: 12px;
}
</style>
