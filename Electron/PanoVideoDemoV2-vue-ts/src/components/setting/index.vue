<template>
  <Modal
    centered
    :width="700"
    :closable="false"
    :footer="null"
    :dialogStyle="{ padding: 0 }"
    :bodyStyle="{ padding: 0 }"
    :visible="visible"
    destroyOnClose
  >
    <div class="setting-wrapper">
      <h3>
        <div>设置</div>
        <span class="iconfont icon-close-app-win" @click="close"></span>
      </h3>
      <div>
        <ul class="tab-header">
          <li
            v-for="item in categoryList"
            :key="item.value"
            :class="{ active: item.value === activeCategory }"
            @click="selectCategory(item.value)"
          >
            <i :class="['iconfont', `icon-${item.icon}`]"></i>
            <span>{{ item.text }}</span>
          </li>
        </ul>
        <div>
          <Normal v-if="activeCategory === 'normal'" />
          <VideoSetting v-else-if="activeCategory === 'video'" />
          <AudioSetting v-else-if="activeCategory === 'audio'" />
          <Feedback v-else-if="activeCategory === 'feedback'" @close="close" />
          <AudioFault
            v-else-if="activeCategory === 'audioFault'"
            @close="close"
          />
          <About v-else />
        </div>
      </div>
    </div>
  </Modal>
</template>

<script>
import { Modal } from 'ant-design-vue';
import Normal from './Normal.vue';
import VideoSetting from './VideoSetting.vue';
import AudioSetting from './AudioSetting.vue';
import Feedback from './Feedback.vue';
import AudioFault from './AudioFault.vue';
import About from './About.vue';

export default {
  props: {
    visible: Boolean,
  },
  components: {
    Modal,
    Normal,
    VideoSetting,
    AudioSetting,
    Feedback,
    AudioFault,
    About,
  },
  data() {
    return {
      categoryList: [
        { value: 'normal', text: '常规设置', icon: 'setting-fill' },
        { value: 'video', text: '视频', icon: 'video' },
        { value: 'audio', text: '音频', icon: 'audio' },
        { value: 'feedback', text: '反馈与报障', icon: 'feedback' },
        { value: 'audioFault', text: '音频报障', icon: 'audio-fault' },
        { value: 'about', text: '关于我们', icon: 'about' },
      ],
      activeCategory: 'normal',
    };
  },
  methods: {
    selectCategory(value) {
      if (this.activeCategory !== value) {
        this.activeCategory = value;
      }
    },
    close() {
      this.$emit('close');
    },
  },
};
</script>

<style lang="less" scoped>
@import '~ant-design-vue/es/style/themes/default.less';

.setting-wrapper {
  > h3 {
    margin: 0;
    line-height: 44px;
    text-align: center;
    position: relative;
    border-bottom: 1px solid #ddd;
    > span {
      position: absolute;
      top: 0;
      right: 0;
      width: 46px;
      font-size: 16px;
    }
  }
  > div {
    height: 420px;
    > ul {
      float: left;
      width: 170px;
    }
    > div {
      height: 100%;
      margin-left: 170px;
      border-left: 1px solid #ddd;
    }
  }
}
.tab-header {
  padding-top: 8px;
  line-height: 40px;
  > li {
    padding-left: 20px;
    display: flex;
    cursor: pointer;
    &.active {
      background-color: @primary-color;
      color: #fff;
    }
    > i {
      font-size: 20px;
      margin-right: 4px;
    }
  }
}
</style>
