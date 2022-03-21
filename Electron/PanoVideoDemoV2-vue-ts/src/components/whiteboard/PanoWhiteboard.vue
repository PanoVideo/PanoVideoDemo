<template>
  <div class="pvc-wb-wrapper" ref="wrapDomRef">
    <div class="pvc-wb-wrapper__tools" ref="domRef">
      <ToolbarDoc
        v-if="isHost"
        :whiteboard="whiteboard"
        :activeDocId="activeDocId"
      />
      <Toolbar :isHost="isHost" :whiteboard="whiteboard" />
      <ToolPagination
        :isHost="isHost"
        :whiteboard="whiteboard"
        :activeDocId="activeDocId"
      />
      <MeetingInfo />
      <Button
        class="pvc-wb__fullscreen"
        size="small"
        type="primary"
        :style="{ marginLeft: '10px' }"
        icon="fullscreen"
        @click="toggleFullScreen"
      />
    </div>
  </div>
</template>

<script>
import ToolbarDoc from './DocList.vue';
import Toolbar from './Toolbar.vue';
import ToolPagination from './Pagination.vue';
import MeetingInfo from '@/components/MeetingInfo.vue';
import { mapGetters, mapMutations } from 'vuex';
import { RtcWhiteboard } from '@pano.video/panorts';
import { Button } from 'ant-design-vue';
import { toggleFullScreen } from '../../utils/common';

export default {
  components: {
    ToolbarDoc,
    Toolbar,
    ToolPagination,
    MeetingInfo,
    Button,
  },
  data() {
    return {
      activeDocId: 'default',
      config: { width: 1600.0, height: 900.0, limit: true },
      whiteboardInited: false,
    };
  },
  methods: {
    ...mapMutations([
      'setFollowVision',
      'setFollowVisionUser',
      'setSharingVision',
    ]),
    toggleFullScreen,
    initWhiteboard() {
      if (!(this.isWhiteboardOpen && !this.whiteboardInited)) {
        return;
      }
      this.$nextTick(() => {
        this.whiteboardInited = true;
        const { width, height, limit } = this.config;
        this.whiteboard.initVision(width, height, limit);
        this.whiteboard.open(this.$refs.domRef);
        this.updateWbDomSize();
        setTimeout(() => {
          window.rtcWhiteboard.updateCanvasSize();
          window.rtcWhiteboard.center();
          this.updateWbDomSize();
        }, 1000);
      });
      window.addEventListener('resize', this.updateWbDomSize);
    },
    onDocChanged() {
      this.activeDocId = this.whiteboard.activeDocId;
    },
    updateWbDomSize() {
      if (
        this.$refs.wrapDomRef &&
        this.$refs.domRef &&
        this.$refs.wrapDomRef.clientWidth !== 0 &&
        this.$refs.wrapDomRef.clientHeight !== 0
      ) {
        const wrapDomWidth = this.$refs.wrapDomRef.clientWidth;
        const wrapDomHeight = this.$refs.wrapDomRef.clientHeight;
        const defaultRadio = this.config.width / this.config.height;
        if (wrapDomWidth / wrapDomHeight <= defaultRadio) {
          this.$refs.domRef.style.width = '100%';
          this.$refs.domRef.style.height = `${wrapDomWidth / defaultRadio}px`;
        } else if (wrapDomWidth / wrapDomHeight > defaultRadio) {
          this.$refs.domRef.style.height = '100%';
          this.$refs.domRef.style.width = `${wrapDomHeight * defaultRadio}px`;
        }
        window.rtcWhiteboard.updateCanvasSize(false);
        window.rtcWhiteboard.center();
      }
    },
  },
  computed: {
    ...mapGetters(['isWhiteboardOpen', 'isHost']),
    visible() {
      return this.isWhiteboardOpen;
    },
  },
  watch: {
    isWhiteboardOpen: function (newV) {
      if (newV) {
        this.initWhiteboard();
      }
    },
  },
  mounted() {
    this.initWhiteboard();
    this.whiteboard.on(
      RtcWhiteboard.Events.whiteboardContentUpdate,
      this.onDocChanged
    );
  },
  beforeCreate() {
    this.whiteboard = window.rtcWhiteboard;
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateWbDomSize);
    this.whiteboard.off(
      RtcWhiteboard.Events.whiteboardContentUpdate,
      this.onDocChanged
    );
    this.whiteboard.close();
  },
};
</script>

<style lang="less" scoped>
.pvc-wb-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: rgba(232, 232, 232, 1);
  position: relative;
  justify-content: center;
  align-items: center;
  &__tools {
    width: 100%;
    height: 100%;
    position: relative;
    background-color: rgba(255, 255, 255, 1);
    overflow: hidden;
  }
}
.pvc-wb__fullscreen {
  position: absolute;
  right: 16px;
  top: 16px;
  z-index: 20;
  background-color: #444444;
  color: #fff;
  border: none;
}
</style>
