<template>
  <div class="pano-wb-pages">
    <div class="pano-wb-pages__wrapper">
      <a-button
        v-if="previewImages.length > 0 && isHost"
        class="pano-withtip pano-withtip-top pano-wb-pages__btn pano-wb-pages__item"
        data-tip="课件预览"
        :style="{ paddingRight: '6px' }"
        @click="toggleDrawer"
      >
        <span class="iconfont icon-files-empty" :style="{ fontSize: '14px' }" />
      </a-button>
      <div class="pano-wb-pages__item">
        <button
          v-if="isDefaultWhiteboard && isHost"
          class="pano-withtip pano-withtip-top pano-wb-pages__btn"
          data-tip="添加一页"
          @click="addPage"
        >
          <span class="iconfont icon-plus" :style="{ fontSize: '10px' }" />
        </button>

        <button
          v-if="isHost"
          class="pano-withtip pano-withtip-top pano-wb-pages__btn"
          data-tip="前一页"
          :style="{ paddingLeft: '6px' }"
          @click="updatePageIndex(pageIndex - 1)"
        >
          <span
            class="iconfont icon-angle-double-left"
            :style="{ fontSize: '16px' }"
          />
        </button>
        <div class="pano-wb-pages__page-count">
          {{ `${this.pageIndex + 1} / ${this.pageCount}` }}
        </div>

        <button
          v-if="isHost"
          class="pano-withtip pano-withtip-top pano-wb-pages__btn"
          data-tip="后一页"
          @click="updatePageIndex(pageIndex + 1)"
        >
          <span
            class="iconfont icon-angle-double-right"
            :style="{ fontSize: '16px' }"
          />
        </button>
        <button
          v-if="isDefaultWhiteboard && isHost"
          class="pano-withtip pano-withtip-top pano-wb-pages__btn"
          data-tip="删除当前页"
          @click="removePage"
        >
          <span class="iconfont icon-minus" :style="{ fontSize: '10px' }" />
        </button>
      </div>
      <div class="pano-wb-pages__item">
        <a-popover destroyTooltipOnHide placement="top" trigger="click">
          <template slot="content">
            <div class="pano-wb-pages__slider">
              <a-slider
                :min="10"
                :max="500"
                vertical
                tooltipVisible
                :value="scale * 100"
                @change="updateScale"
              />
            </div>
          </template>
          <div class="pano-wb-pages__zoom-rate">
            {{ `${Math.round(this.scale * 100)}%` }}
          </div>
        </a-popover>
      </div>
    </div>

    <div
      v-if="drawerVisible"
      class="wb-courseware-alide-mask"
      @click="drawerVisible = false"
    />

    <aside
      :class="{
        'wb-courseware-alide': true,
        'wb-courseware-alide__hiden': !drawerVisible,
      }"
    >
      <header>
        <span>课件预览</span>
        <i
          class="iconfont icon-close"
          :style="{ fontSize: '14px' }"
          @click="drawerVisible = false"
        />
      </header>
      <div class="wb-courseware-list">
        <div
          v-for="(url, index) in previewImages"
          class="courseware-preview"
          :key="url"
        >
          <img
            :class="{
              'courseware-preview__active': pageIndex === index,
            }"
            :src="url"
            alt="thumbnail"
            @click="gotoPage(index)"
          />
          <div class="courseware-index">
            <span>{{ index + 1 }}</span>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script>
import { Popover, Slider, Button } from 'ant-design-vue';
import { RtcWhiteboard } from '@pano.video/panorts';
import { throttle } from 'lodash-es';

export default {
  name: 'ToolPagination',
  props: {
    isHost: Boolean,
    whiteboard: RtcWhiteboard,
    activeDocId: String,
  },
  components: {
    'a-popover': Popover,
    'a-slider': Slider,
    'a-button': Button,
  },
  data() {
    return {
      userListVisible: false,
      drawerVisible: false,
      pageCount: this.whiteboard.getTotalNumberOfPages(),
      pageIndex: this.whiteboard.getCurrentPageNumber(),
      scale: this.whiteboard.scale,
      previewImages: [],
    };
  },
  mounted() {
    this.addWbListener();
    this.onDocChanged();
  },
  beforeDestroy() {
    this.removeWbListener();
  },
  computed: {
    isDefaultWhiteboard() {
      return this.activeDocId === 'default';
    },
  },
  methods: {
    toggleDrawer() {
      this.drawerVisible = !this.drawerVisible;
    },
    addPage() {
      this.whiteboard.addPage();
    },
    removePage() {
      this.whiteboard.removePage();
    },
    updatePageIndex(pageIndex) {
      if (pageIndex < 0 || pageIndex >= this.whiteboard.getTotalNumberOfPages())
        return;
      this.whiteboard.gotoPage(pageIndex);
    },
    updateScale(nextScale) {
      nextScale /= 100;
      if (nextScale > 0.1 && nextScale < 5) {
        this.whiteboard.setScale(nextScale);
        this.scale = nextScale;
      }
    },
    addWbListener() {
      this.whiteboard.on(
        RtcWhiteboard.Events.whiteboardContentUpdate,
        this.getStateFromProps
      );
      this.whiteboard.on(RtcWhiteboard.Events.docCreated, this.onDocChanged);
      this.whiteboard.on(RtcWhiteboard.Events.docSwitched, this.onDocSwitched);
      this.whiteboard.on(RtcWhiteboard.Events.docDeleted, this.onDocChanged);
      this.whiteboard.on(RtcWhiteboard.Events.docUpdated, this.onDocChanged);
    },
    removeWbListener() {
      this.whiteboard.off(
        RtcWhiteboard.Events.whiteboardContentUpdate,
        this.getStateFromProps
      );
      this.whiteboard.off(RtcWhiteboard.Events.docCreated, this.onDocChanged);
      this.whiteboard.off(RtcWhiteboard.Events.docSwitched, this.onDocSwitched);
      this.whiteboard.off(RtcWhiteboard.Events.docDeleted, this.onDocChanged);
      this.whiteboard.off(RtcWhiteboard.Events.docUpdated, this.onDocChanged);
    },
    getStateFromProps: throttle(function () {
      this.scale = this.whiteboard.scale;
      this.pageCount = this.whiteboard.getTotalNumberOfPages();
      this.pageIndex = this.whiteboard.getCurrentPageNumber();
      this.previewImages = this.whiteboard.thumbnails;
    }, 200),

    onDocSwitched() {
      this.$message.info(
        `已切换到${
          this.whiteboard.activeDoc.name || this.whiteboard.activeDoc.docId
        }`
      );
    },
    onDocChanged() {
      // TODO
    },
    gotoPage(index) {
      this.whiteboard.gotoPage(index);
    },
  },
};
</script>

<style lang="less" scoped>
.pano-wb-pages {
  pointer-events: all;
  position: absolute;
  z-index: 12;
  padding: 5px;
  right: 20px;
  bottom: 20px;
  font-size: 14px;
  color: #2b3139;
  &__zoom-rate {
    font-size: 12px;
    user-select: none;
    width: 40px;
    text-align: center;
  }
  &__wrapper {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  &__wrapper ~ &__wrapper {
    margin-top: 4px;
  }
  &__page-count {
    text-align: center;
    user-select: none;
  }
  &__btn,
  &__btn-add {
    border: none;
    background: transparent;
    outline: none;
    padding: 5px 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    &--disabled {
      cursor: not-allowed;
      svg {
        fill: #c0c4cc;
      }
    }
  }
  &__item {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 32px;
    padding-left: 5px;
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
    padding: 0 12px;
  }
  &__item ~ &__item {
    margin-left: 8px;
  }
  &__slider {
    height: 120px;
    margin: 0 -5px;
  }
}

.pano-withtip {
  position: relative;
  &::after {
    content: attr(data-tip);
    position: absolute;
    // top:-30px;
    // right: -40px;
    // width: 100px;
    min-width: 80px;
    background-color: black;
    color: #fff;
    text-align: center;
    border-radius: 6px;
    padding: 5px;
    z-index: 100;
    transform: translateX(calc(90% - 10px));
    font-size: 12px;
    opacity: 0;
    transition: all 0.3s ease-in-out;
    display: none;
  }
  &:hover::after {
    display: block;
    opacity: 1;
    transform: translateX(90%);
  }
}
.pano-withtip-top {
  &::after {
    transform: translate(-50%, -30px);
    left: 50%;
  }
  &:hover::after {
    transform: translate(-50%, -40px);
  }
}

.wb-courseware-alide-mask {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  opacity: 0;
  bottom: 0;
  transition: all 0.3s ease-in-out;
  z-index: 99;
}

.wb-courseware-alide {
  position: fixed;
  top: 0;
  right: 0;
  width: 260px;
  background-color: #f9f9f9;
  bottom: 0;
  box-shadow: rgba(0, 0, 0, 0.15) 0px 8px 24px 0px;
  transition: all 0.3s ease-in-out;
  z-index: 100;
  user-select: none;
  &__hiden {
    right: -280px;
  }
  header {
    height: 60px;
    display: flex;
    justify-content: space-between;
    font-size: 15px;
    padding: 20px 10px 0;
    span {
      font-weight: 500;
    }
    i {
      cursor: pointer;
    }
  }
  .wb-courseware-list {
    overflow-y: auto;
    height: calc(100% - 60px);
    .courseware-preview {
      margin: 20px 0;
      img {
        width: 230px;
        height: auto;
        border: 1px solid #fff;
        border-radius: 5px;
        margin: 0 auto;
        display: block;
        cursor: pointer;
      }
      img.courseware-preview__active {
        border-color: #4091f7;
      }
      .courseware-index {
        display: flex;
        justify-content: space-between;
        padding: 0 15px;
        margin-top: 5px;
        color: #777;
      }

      i {
        cursor: pointer;
      }
    }
  }
}
</style>
