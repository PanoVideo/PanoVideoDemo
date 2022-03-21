<template>
  <Draggable :initPosition="position" :disabled="disabled">
    <template slot="content">
      <div
        :class="{
          horizontalVideoContainer: horizontal,
          horizontalVideoContainerEmpty:
            !isWhiteboardOpen && allUsers.length === 1 && horizontal,
          verticalVideoContainer: !horizontal,
        }"
      >
        <div v-if="!horizontal" class="verticalHeader">
          <span
            :class="{
              'iconfont icon-collapse': true,
              'iconfont icon-single-window': true,
              upArrow__active: mediaLayout === 'asl_text',
            }"
            @click="updateMediaLayout({ layout: 'asl_text' })"
          />
          <span
            :class="{
              'iconfont icon-single-window': true,
              upArrow__active: mediaLayout === 'vertical_asl',
            }"
            @click="updateMediaLayout({ layout: 'vertical_asl' })"
          />
          <span
            :class="{
              'iconfont icon-expand': true,
              upArrow__active: mediaLayout === 'vertical',
            }"
            @click="updateMediaLayout({ layout: 'vertical' })"
          />
          <div class="drag-header" id="dragHeader"></div>
        </div>

        <div
          :class="{
            subvideos: horizontal,
            verticalSubvideos: !horizontal,
          }"
          ref="videoListContainer"
        >
          <div
            v-if="enableGotoPrevPage"
            :class="{
              leftScroll: horizontal,
              upArrow: !horizontal,
            }"
            @click="gotoPrevPage"
          >
            <span
              :class="{
                'iconfont icon-leftArrow': horizontal,
                'iconfont icon-upArrow': !horizontal && expanded,
              }"
            />
          </div>
          <div
            :class="{
              'video-list': true,
              'video-list--asl-text': mediaLayout === 'asl_text',
            }"
            ref="videoList"
          >
            <div
              class="speaking-user text-overflow"
              v-if="mediaLayout === 'asl_text'"
            >
              正在讲话 :
              <span
                class="name"
                v-if="mostActiveUser && !mostActiveUser.audioMuted"
              >
                {{ mostActiveUser.userName }}
              </span>
            </div>
            <SmallVideo
              v-for="data of currentPageUsers"
              :user="data.user"
              :showScreenShare="data.type === 'screen'"
              :key="`${data.user.userId}-${data.type}`"
              :direction="direction"
              :data="data"
              @lock="onLock(data)"
              @unlock="onUnLock(data)"
            />
          </div>
          <div
            v-if="enableGotoNextPage && showNextButton"
            :class="{
              rightScroll: horizontal,
              downArrow: !horizontal && expanded,
            }"
            @click="gotoNextPage"
          >
            <span
              :class="{
                'iconfont icon-rightArrow': horizontal,
                'iconfont icon-downArrow': !horizontal,
              }"
            />
          </div>
        </div>
      </div>
    </template>
  </Draggable>
</template>

<script>
import { mapGetters, mapMutations, mapActions } from 'vuex';
import {
  MaxHorizontalPageSize,
  MinPageSize,
  MaxVerticalPageSize,
} from '@/store/modules/user';
import Draggable from './Draggable';
import SmallVideo from '@/components/video/SmallVideo.vue';
import { UPDATE_MEDIALAYOUT, LOCK_USER } from '@/store/mutations';
import * as actions from '@/store/actions';
import { throttle } from 'lodash';
import { LogUtil } from '@/utils/';
import { addObserverForScreen } from '@/utils/common';

export default {
  data() {
    return {
      position: { left: window.screen.width - 300, top: 50 },
      windowBounds: undefined,
      isMac: process.platform === 'darwin',
    };
  },
  components: {
    Draggable,
    SmallVideo,
  },
  computed: {
    ...mapGetters([
      'userMe',
      'allUsers',
      'isWhiteboardOpen',
      'enableGotoPrevPage',
      'enableGotoNextPage',
      'currentPageUsers',
      'mediaLayout',
      'mostActiveUser',
    ]),
    direction() {
      return this.mediaLayout;
    },
    horizontal() {
      return this.direction === 'horizontal';
    },
    expanded() {
      return this.direction === 'vertical';
    },
    showNextButton() {
      return this.direction === 'horizontal' || this.expanded;
    },
    isSharing() {
      return window.IS_ELECTRON && this.userMe.screenOpen;
    },
    disabled() {
      return this.horizontal || this.isSharing;
    },
  },
  watch: {
    direction() {
      LogUtil('MediaList: componentDidUpdate:', this.direction);
      if (this.direction === 'vertical') {
        this.updatePageSizeAction({ size: MaxVerticalPageSize });
      }
      if (this.direction === 'vertical_asl') {
        this.updatePageSizeAction({ size: MinPageSize });
      }
      if (this.direction === 'horizontal') {
        this.updatePageSizeAction({ size: MaxHorizontalPageSize });
      }
    },
    currentPageUsers() {
      this.syncMainWindowFrame();
    },
  },
  methods: {
    ...mapMutations([LOCK_USER, UPDATE_MEDIALAYOUT]),
    ...mapActions([
      actions.GOTO_PREV_PAGE,
      actions.GOTO_NEXT_PAGE,
      actions.UPDATE_PAGE_SIZE_ACTION,
    ]),
    onLock(data) {
      this.$store.commit(LOCK_USER, {
        userId: data.user.userId,
        type: data.type,
      });
      this.$store.dispatch(actions.SET_IS_WHITEBOARD_OPEN, false);
    },
    onUnLock(data) {
      this.$store.commit(LOCK_USER, { userId: '', type: data.type });
    },
    checkWeatherArrowNeedShow: throttle(function () {
      try {
        if (this.direction !== 'horizontal') {
          return;
        }
        const innerWidth =
          this.$refs.videoListContainer.getBoundingClientRect().width;
        const size = Math.floor((innerWidth - 40) / (240 + 20));
        this.updatePageSizeAction({ size });
      } catch (err) {
        console.log(err);
      }
    }, 500),
    /**
     * 同步主窗口的大小
     */
    syncMainWindowFrame(position) {
      if (!this.isSharing || !this.windowBounds) return;
      this.position = { left: 0, top: 0 };
      window.ipc.sendToMainProcess({
        command: 'updateMainWindow',
        payload: {
          ...position,
          width: 240,
          height: this.currentPageUsers.length * 138 + 45,
        },
      });
    },
    async minMainWindow() {
      window.remote.getCurrentWindow().unmaximize();
      this.setFrameVisibility(false);
      // 开启共享后，布局默认调整为 ASL
      this.updateMediaLayout({ layout: 'vertical_asl' });
      const x = this.windowBounds.x + this.windowBounds.width - 300;
      const y = this.windowBounds.y + 50;
      this.syncMainWindowFrame({ x, y });
    },
    maxMainWindow() {
      if (this.isMac) {
        window.ipc.sendToMainProcess({
          command: 'updateMainWindow',
          payload: this.windowBounds,
        });
      }
      this.setFrameVisibility(true);
      this.updateMediaLayout({ layout: 'horizontal' });
      this.windowBounds = undefined;
      this.position = { left: window.screen.width - 300, top: 50 };
    },
    setFrameVisibility(visible) {
      window.ipc.sendToMainProcess({
        command: 'setFrameVisibility',
        payload: { visible: visible },
      });
      const target = window.document.getElementById('titlebar');
      visible
        ? (target.style.display = 'block')
        : (target.style.display = 'none');
    },
  },
  mounted() {
    setTimeout(() => {
      this.checkWeatherArrowNeedShow();
      window.addEventListener('resize', this.checkWeatherArrowNeedShow);
    });
    addObserverForScreen((fullscreen) => {
      const dragHeader = document.getElementById('dragHeader');
      if (this.isSharing && this.isMac) {
        dragHeader && (dragHeader.style.visibility = 'visible');
        this.minMainWindow();
      } else {
        dragHeader &&
          (dragHeader.style.visibility = fullscreen ? 'hidden' : 'visible');
        this.updateMediaLayout({ fullscreen });
      }
    });
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkWeatherArrowNeedShow);
  },
};
</script>

<style lang="less" scoped>
// 横屏小视频容器布局
.horizontalVideoContainer {
  overflow: hidden;
  width: 100%;
  height: 120px;
  padding: 10px 0;
  background-color: black;
  transition: height 0.5s ease;
  position: relative;
  text-align: center;
}

.horizontalVideoContainerEmpty {
  height: 0;
  transition: height 0.5s ease;
  padding: 0;
}

.subvideos {
  position: absolute;
  left: 0;
  right: 0;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  text-align: center;
}

.video-list {
  display: inline-block;
  background-color: black;
  &--asl-text {
    width: 100%;
    border-radius: 4px;
    background-color: #555;
  }
  .speaking-user {
    color: #f1f1f1;
    height: 36px;
    line-height: 36px;
    align-items: center;
    font-size: 12px;
    padding-left: 10px;
    .name {
      padding-left: 10px;
    }
  }
}

.leftScroll,
.rightScroll {
  display: inline-block;
  position: absolute;
  background-color: #0f0f0f;
  width: 30px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 11;
  color: white;
  cursor: pointer;
  span {
    font-size: 32px;
  }
}

.leftScroll {
  transform: translateX(-100%) translateY(-50%);
}

.verticalVideoContainer {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 1001;
  overflow: hidden;
  width: 240px;
  padding: 0;
  transition: height 0.5s ease;
  -webkit-app-region: no-drag;
}

.drag-header {
  display: inline-block;
  width: 190px;
  height: 100%;
  -webkit-app-region: drag;
  position: absolute;
}

.verticalSubvideos {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upArrow,
.downArrow {
  width: 100%;
  height: 32px;
  background: #0f0f0f;
  line-height: 32px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  cursor: pointer;
  span {
    color: #fff;
    font-size: 32px;
  }
  &__active {
    color: #0899f9;
  }
  -webkit-app-region: no-drag;
}

.verticalHeader {
  width: 100%;
  height: 36px;
  border-radius: 4px;
  line-height: 36px;
  padding: 0px 8px;
  background: #000000;
  color: #fff;
  position: relative;
  span {
    cursor: pointer;
  }
  span ~ span {
    margin-left: 8px;
  }
}
</style>
