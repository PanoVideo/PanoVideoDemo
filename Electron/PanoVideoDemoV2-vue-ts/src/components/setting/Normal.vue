<template>
  <ul class="normal-wrapper">
    <li>
      <checkbox :checked="!autoMuteAudio" @change="toggleAudio"
        >入会打开麦克风</checkbox
      >
    </li>
    <li>
      <checkbox :checked="autoOpenVideo" @change="toggleVideo"
        >入会打开摄像头</checkbox
      >
    </li>
    <li>
      <label class="normal-label">单次通话时长</label>
      <Input style="width: 240px" disabled value="90分钟" />
    </li>
    <li>
      <label class="normal-label">通话人数上限</label>
      <Input style="width: 240px" disabled value="25人" />
    </li>
  </ul>
</template>

<script>
import { Input, Checkbox } from 'ant-design-vue';
import {
  localCacheKeyOpenMicAtStart,
  localCacheKeyOpenCamAtStart,
} from '../../constants';
import { mapGetters } from 'vuex';
import { SET_MUTE_AUDIO_JOIN, SET_OPEN_VIDEO_JOIN } from '@/store/mutations';
import { SAFARI_15_1, SAFARI_15_1_MESSAGE } from '@/utils/common';

export default {
  components: {
    Checkbox,
    Input,
  },
  computed: {
    ...mapGetters(['autoMuteAudio', 'autoOpenVideo']),
  },
  methods: {
    toggleAudio() {
      this.$store.commit(SET_MUTE_AUDIO_JOIN, !this.autoMuteAudio);
      localStorage.setItem(
        localCacheKeyOpenMicAtStart,
        this.autoMuteAudio ? 'no' : 'yes'
      );
    },
    toggleVideo() {
      if (SAFARI_15_1) {
        this.$message.info(SAFARI_15_1_MESSAGE);
        return;
      }
      this.$store.commit(SET_OPEN_VIDEO_JOIN, !this.autoOpenVideo);
      localStorage.setItem(
        localCacheKeyOpenCamAtStart,
        this.autoOpenVideo ? 'yes' : 'no'
      );
    },
  },
};
</script>

<style lang="less" scoped>
.normal-wrapper {
  list-style: none;
  margin: 0;
  padding: 36px 24px 0;
  > li {
    margin-bottom: 18px;
  }
}
.normal-label {
  display: block;
  margin-bottom: 6px;
  &::after {
    content: ':';
  }
}
</style>
