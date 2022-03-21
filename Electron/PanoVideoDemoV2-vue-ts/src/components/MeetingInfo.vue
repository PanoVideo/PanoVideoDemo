<template>
  <div
    class="meetingInfo"
    :style="{ color: isWhiteboardOpen ? '#333' : '#ddd' }"
  >
    <Icon type="info-circle" />
    <span class="content">
      <span>房间号: {{ channelId }}</span>
      <span v-if="remainSeconds > 0" class="remain-time">
        剩余时间: {{ mins }} {{ seconds }} 秒
      </span>
    </span>
  </div>
</template>

<script>
import { Icon } from 'ant-design-vue';
import { mapGetters } from 'vuex';

/**
 * 会议信息
 */
export default {
  name: 'MeetingInfo',
  components: {
    Icon,
  },
  computed: {
    ...mapGetters(['isWhiteboardOpen', 'channelId', 'remainSeconds']),
    seconds() {
      const seconds = this.remainSeconds % 60;
      return seconds >= 10 ? `${seconds}` : `0${seconds}`;
    },
    mins() {
      const mins = Math.floor(this.remainSeconds / 60);
      return mins > 0 ? `${mins}分钟` : '';
    },
  },
};
</script>

<style lang="less" scoped>
.meetingInfo {
  position: absolute;
  left: 20px;
  bottom: 23px;
  color: #333;
  z-index: 10;
}
.info {
  display: inline-block;
  margin-right: 5px;
  font-size: 12px;
}

.content {
  padding-left: 5px;
  font-size: 12px;
  user-select: none;
  .remain-time {
    padding-left: 5px;
  }
}
</style>
