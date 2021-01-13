<template>
  <div
    :style="{
      position: 'absolute',
      left: '20px',
      bottom: '23px',
      color: isWhiteboardOpen ? '#333' : '#ddd',
      zIndex: 10
    }"
  >
    <i
      class="el-icon-info"
      :style="{
        display: 'inline',
        marginRight: '5px',
        fontSize: '12px'
      }"
    />
    <span :style="{ marginRight: '15px', fontSize: '12px' }">
      <span>房间号：</span>
      {{ channelId }}
    </span>
    <span :style="{ marginRight: '5px', fontSize: '12px', userSelect: 'none' }">
      剩余时间：
      {{ mins > 0 ? `${mins}分钟` : '' }}
      {{ seconds }} 秒
    </span>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  computed: {
    ...mapGetters(['isWhiteboardOpen', 'channelId', 'remainSeconds']),
    seconds() {
      const seconds = this.remainSeconds % 60;
      return seconds > 10 ? seconds : `0${seconds}`;
    },
    mins() {
      return Math.floor(this.remainSeconds / 60);
    }
  }
};
</script>

<style lang="scss" scoped></style>
