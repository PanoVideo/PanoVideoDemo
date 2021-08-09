<template>
  <div class="wb-panel">
    <div class="wb-wrapper" ref="whiteboard"></div>
    <div class="small-video-view">
      <PvcVideo
        v-if="isOpen(user.videoStatus) && user.videoTag"
        :videoTag="user.videoTag"
      />
      <div v-else class="no-video"></div>
      <div class="user-bar">
        <span>{{ user.userName || user.userId }}</span>
        <em>(Me)</em>
        <i :class="{ closed: !isOpen(user.audioStatus) }"></i>
      </div>
    </div>
    <div class="wb-header">
      <ul class="wb-header-left">
        <li @click="close">收起白板</li>
      </ul>
      <div class="wb-header-title">
        <span>{{
          rtcWhiteboard.activeDoc.currentPageIndex + 1
        }}/{{
          rtcWhiteboard.activeDoc.pages.length
        }}</span>
        <span>{{ (rtcWhiteboard.scale * 100).toFixed(0) }}%</span>
        <i @click="openPage"></i>
      </div>
      <ul class="wb-header-right">
        <li @click="undo"></li>
        <li @click="redo"></li>
      </ul>
    </div>
    <div v-if="pageVisible" class="page-panel" @click="closePage">
      <div>
        <ul class="page-info">
          <li><label>当前白板：</label><div>默认白板</div></li>
          <li><label>当前演示人：</label><div>{{ user.userName }}</div></li>
        </ul>
        <ul class="page-nav">
          <li @click="prevPage">前一页</li>
          <li @click="nextPage">后一页</li>
          <li @click="addPage">增加页</li>
          <li @click="removePage">删除当前页</li>
        </ul>
        <ul class="doc-list">
          <li>默认白板</li>
        </ul>
      </div>
    </div>
    <div v-if="openOption === ShapeType.Pen" class="shape-option-panel">
      <span style="left: 20%"></span>
      <div class="shape-option-wrapper">
        <div class="pen-width">
          <label></label>
          <span>{{ rtcWhiteboard.lineWidth }}</span>
          <div>
            <Slider :button-size="18" :min="1" :max="20" v-model="rtcWhiteboard.lineWidth" />
          </div>
        </div>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === rtcWhiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <div v-else-if="openOption === ShapeType.Text" class="shape-option-panel">
      <span style="left: 40%"></span>
      <div class="shape-option-wrapper">
        <div class="font-size">
          <label></label>
          <span>{{ rtcWhiteboard.fontSize }}</span>
          <div>
            <Slider :button-size="18" :min="1" :max="21" v-model="rtcWhiteboard.fontSize" />
          </div>
        </div>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === rtcWhiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <div v-else-if="openOption === 'figure'" class="shape-option-panel">
      <span style="left: 60%"></span>
      <div class="shape-option-wrapper">
        <ul class="figure-list">
          <li :class="{
            active: figureType === ShapeType.Line && rtcWhiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Line, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Ellipse && rtcWhiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Ellipse, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Rect && rtcWhiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Rect, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Ellipse && rtcWhiteboard.fillType === 'color'
          }" @click="() => setFigureType(ShapeType.Ellipse, 'color')"></li>
          <li :class="{
            active: figureType === ShapeType.Rect && rtcWhiteboard.fillType === 'color'
          }" @click="() => setFigureType(ShapeType.Rect, 'color')"></li>
        </ul>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === rtcWhiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <ul class="wb-footer">
      <li :class="{active: rtcWhiteboard.getToolType() === ShapeType.Select}"
        @click="() => setToolType(ShapeType.Select)">
        <i></i>
        <span>选择</span>
      </li>
      <li :class="{active: rtcWhiteboard.getToolType() === ShapeType.Pen}"
        @click="() => changeToolType(ShapeType.Pen)">
        <i></i>
        <span>画笔</span>
      </li>
      <li :class="{active: rtcWhiteboard.getToolType() === ShapeType.Text}"
        @click="() => changeToolType(ShapeType.Text)">
        <i></i>
        <span>文字</span>
      </li>
      <li :class="{active: figureTypeList.includes(rtcWhiteboard.getToolType())}"
        @click="() => setFigureTool()">
        <i></i>
        <span>图形</span>
      </li>
      <li :class="{active: rtcWhiteboard.getToolType() === ShapeType.Delete}"
        @click="() => setToolType(ShapeType.Delete)">
        <i></i>
        <span>删除</span>
      </li>
    </ul>
  </div>
</template>

<script>
import Slider from 'vant/lib/slider';
import 'vant/lib/slider/style';
import { Constants } from '@pano.video/panortc';
import { mapState } from 'vuex';
import PvcVideo from '../PvcVideo.vue';

const { ShapeType } = Constants;

const colorList = ['rgba(1, 2, 4, 1)', 'rgba(224, 44, 11, 1)', 'rgba(228, 100, 8, 1)',
  'rgba(231, 189, 14, 1)', 'rgba(84, 214, 18, 1)', 'rgba(144, 222, 15, 1)',
  'rgba(12, 142, 229, 1)', 'rgba(22, 49, 211, 1)', 'rgba(86, 19, 216, 1)'];

const openStatusList = ['open', 'unmute'];

export default {
  name: 'Whiteboard',
  components: {
    Slider,
    PvcVideo,
  },
  data() {
    return {
      pageVisible: false,
      ShapeType,
      figureTypeList: [ShapeType.Line, ShapeType.Ellipse, ShapeType.Rect],
      colorList,
      rtcWhiteboard: {
        getToolType() {
          return ShapeType.Select;
        },
      },
      openOption: '',
      figureType: ShapeType.Line,
    };
  },
  computed: mapState(['user']),
  created() {
    this.rtcWhiteboard = window.panoSDK.getWhiteboard();
    if (!colorList.includes(this.rtcWhiteboard.strokeStyle)) {
      this.changeStrokeStyle(colorList[0]);
    } else {
      this.rtcWhiteboard.fillStyle = this.rtcWhiteboard.strokeStyle;
    }
    if (this.figureTypeList.includes(this.rtcWhiteboard.getToolType())) {
      this.figureType = this.rtcWhiteboard.getToolType();
    }
    if (!this.rtcWhiteboard.fillStyle) {
      this.rtcWhiteboard.fillStyle = 'none';
    }
  },
  mounted() {
    const { rtcWhiteboard } = this;
    rtcWhiteboard.open(this.$refs.whiteboard);
  },
  methods: {
    isOpen(status) {
      return openStatusList.includes(status);
    },
    close() {
      this.$store.commit('closeWhiteboard');
    },
    openPage() {
      this.pageVisible = true;
      this.openOption = '';
    },
    closePage() {
      this.pageVisible = false;
    },
    prevPage() {
      this.rtcWhiteboard.prevPage();
    },
    nextPage() {
      this.rtcWhiteboard.nextPage();
    },
    addPage() {
      this.rtcWhiteboard.addPage(true);
    },
    removePage() {
      this.rtcWhiteboard.removePage(this.rtcWhiteboard.activeDoc.currentPageIndex);
    },
    undo() {
      this.rtcWhiteboard.undo();
    },
    redo() {
      this.rtcWhiteboard.redo();
    },
    changeStrokeStyle(color) {
      this.rtcWhiteboard.strokeStyle = color;
      // 两个是一样的
      this.rtcWhiteboard.fillStyle = color;
    },
    setToolType(toolType) {
      if (this.rtcWhiteboard.getToolType() === toolType) {
        return;
      }
      this.openOption = '';
      this.rtcWhiteboard.setToolType(toolType);
    },
    changeToolType(toolType) {
      if (this.rtcWhiteboard.getToolType() === toolType) {
        if (this.openOption === toolType) {
          this.openOption = '';
        } else {
          this.openOption = toolType;
        }
        return;
      }
      this.openOption = toolType;
      this.rtcWhiteboard.setToolType(toolType);
    },
    setFigureTool() {
      if (this.figureTypeList.includes(this.rtcWhiteboard.getToolType())) {
        if (this.openOption === 'figure') {
          this.openOption = '';
        } else {
          this.openOption = 'figure';
        }
        return;
      }
      this.openOption = 'figure';
      this.rtcWhiteboard.setToolType(this.figureType);
    },
    setFigureType(toolType, fillStyle) {
      this.figureType = toolType;
      this.rtcWhiteboard.setToolType(toolType);
      this.rtcWhiteboard.fillType = fillStyle;
    },
  },
};
</script>

<style lang="less" scoped>
@base-path: "../../";
@import "@{base-path}less/variables.less";

.wb-panel {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: #fff;
}
.wb-wrapper {
  height: 100%;
  position: relative;
}
.wb-header {
  position: absolute;
  left: 0;
  right: 0;
  top: 10px;
  line-height: 30px;
  z-index: 50;
}
.wb-header-left,
.wb-header-right,
.wb-header-title {
  position: absolute;
  top: 0;
}

.wb-header-left {
  left: 0;
  padding-left: 10px;
  > li {
    padding: 0 5px;
    color: #EF476B;
  }
}

.wb-header-right {
  right: 0;
  padding-right: 7px;
  display: flex;
  > li {
    width: 30px;
    text-align: center;
    background-color: #F0F0F0;
    border-radius: 2px;
    margin-right: 8px;
    &::after {
      display: inline-block;
      vertical-align: top;
      font-family: "pvc icon";
      font-size: 24px;
      line-height: inherit;
    }
    &:nth-child(1)::after {
      content: "\e77f";
    }
    &:nth-child(2)::after {
      content: "\e77d";
    }
  }
}

.wb-header-title {
  position: absolute;
  left: 50%;
  top: 0;
  transform: translate3d(-50%, 0, 0);
  background-color: #f0f0f0;
  border-radius: 2px;
  display: flex;
  padding: 0 4px;
  > span {
    text-align: center;
    &:nth-child(1) {
      min-width: 36px;
    }
    &:nth-child(2) {
      min-width: 46px;
    }
  }
  > i {
    width: 30px;
    margin-left: 2px;
    font-style: normal;
    text-align: center;
    &::after {
      content: "\e781";
      display: inline-block;
      vertical-align: top;
      font-family: "pvc icon";
      font-size: 24px;
      line-height: inherit;
    }
  }
}
.page-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, .2);
  z-index: 60;
  > div {
    width: 130px;
    margin: 44px auto 0;
    font-size: 12px;
    background-color: #f8f8f8;
    border-radius: 2px;
  }
}
.page-info {
  padding: 4px 8px;
  margin-bottom: 5px;
  line-height: 18px;
  background-color: #fff;
  border-radius: 2px 2px 0 0;
  > li {
    display: flex;
    > label {
      color: #999;
    }
    > div {
      flex: 1;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }
}
.page-nav {
  margin-bottom: 5px;
  background-color: #fff;
  line-height: 24px;
  > li {
    padding: 0 8px;
    display: flex;
    border-bottom: 0.5px solid #ddd;
    &:last-child {
      border-bottom: none;
    }
    &::before {
      margin-right: 2px;
      font-family: "pvc icon";
      line-height: inherit;
      font-size: 20px;
    }
    &:nth-child(1)::before {
      content: "\e785";
    }
    &:nth-child(2)::before {
      content: "\e784";
    }
    &:nth-child(3)::before {
      content: "\e789";
    }
    &:nth-child(4)::before {
      content: "\e786";
    }
  }
}
.doc-list {
  background-color: #fff;
  border-radius: 0 0 2px 2px;
  line-height: 24px;
  > li {
    padding: 0 8px;
    display: flex;
    &::before {
      content: "\e78f";
      margin-right: 2px;
      font-family: "pvc icon";
      line-height: inherit;
      font-size: 20px;
      color: @primary-color;
    }
  }
}
.user-bar {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  line-height: 24px;
  > em {
    font-style: normal;
  }
  > i {
    font-style: normal;
    position: relative;
    &::before,
    &::after {
      font-family: "pvc icon";
      vertical-align: top;
      line-height: inherit;
    }
    &::before {
      content: "\e776";
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: "\e7c4";
    }
  }
}
.no-video::after {
  font-family: "pvc icon";
  line-height: 1;
  content: "\e76e";
}
.small-video-view {
  position: absolute;
  right: 15px;
  top: 60px;
  width: 100px;
  height: 132px;
  border-radius: 3px;
  background-color: #333;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
  color: #fff;
  > .no-video {
    font-size: 44px;
  }
  > .user-bar {
    left: 0;
    padding: 0 2px 0 6px;
    background-color: rgba(0, 0, 0, 0.56);
    justify-content: flex-end;
    font-size: 13px;
    line-height: 19px;
    > span {
      flex: 1;
      text-align: right;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    > i {
      margin-left: 1px;
      font-size: 17px;
    }
  }
}
.shape-option-panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 85px;
  padding: 0 10px;
  z-index: 50;
  &::before {
    content: "";
    position: absolute;
    left: 10px;
    right: 10px;
    top: 0;
    bottom: 0;
    border: 0.5px solid #f4f4f4;
    border-radius: 3px;
    box-shadow: 0px 4px 10px 0px rgba(4, 0, 0, 0.16);
  }
  > span {
    position: absolute;
    width: 20%;
    top: 100%;
    &::after {
      content: "";
      position: absolute;
      left: 50%;
      bottom: -10px;
      width: 13px;
      height: 13px;
      border-left: 0.5px solid #f4f4f4;
      border-bottom: 0.5px solid #f4f4f4;
      background-color: #fff;
      transform-origin: left bottom;
      transform: rotate(-45deg);
    }
  }
}
.shape-option-wrapper {
  position: relative;
  background: #fff;
  padding: 0.5px;
  border-radius: 3px;
}
.pen-width, .font-size {
  display: flex;
  padding: 0 10px;
  line-height: 38px;
  > label {
    width: 30px;
    text-align: center;
    &::after {
      display: inline-block;
      vertical-align: top;
      font-family: "pvc icon";
      font-size: 28px;
      line-height: inherit;
    }
  }
  > span {
    width: 30px;
    padding-right: 8px;
    text-align: right;
  }
  > div {
    flex: 1;
    padding: 0 5px 0 9px;
    display: flex;
    align-items: center;
  }
}
.pen-width > label::after {
  content: "\e78a";
}
.font-size > label::after {
  content: "\e792";
}
.figure-list {
  display: flex;
  padding: 0 5px;
  > li {
    flex: 1;
    line-height: 38px;
    text-align: center;
    color: #000;
    &::after {
      display: inline-block;
      vertical-align: top;
      font-family: "pvc icon";
      font-size: 28px;
      line-height: inherit;
    }
    &:nth-child(1)::after {
      content: "\e78a";
    }
    &:nth-child(2)::after {
      content: "\e78b";
    }
    &:nth-child(3)::after {
      content: "\e78c";
    }
    &:nth-child(4)::after {
      content: "\e78d";
    }
    &:nth-child(5)::after {
      content: "\e78e";
    }
    &.active {
      color: @primary-color;
    }
  }
}
.color-list {
  display: flex;
  border-top: 0.5px solid #ddd;
  padding: 0 5px;
  > li {
    flex: 1;
    text-align: center;
    padding: 6px 0;
    &::after {
      content: "\e78e";
      display: inline-block;
      vertical-align: top;
      font-family: "pvc icon";
      font-size: 24px;
      line-height: inherit;
      border: 0.5px solid transparent;
      border-radius: 2px;
    }
    &.active::after {
      border-color: @primary-color;
    }
  }
}
.wb-footer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 70px;
  display: flex;
  background-color: #fff;
  color: #666;
  border-top: 1px solid #ddd;
  z-index: 50;
  > li {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    &.active {
      color: @primary-color;
    }
    > i {
      font-style: normal;
      position: relative;
      &::after {
        font-family: "pvc icon";
        font-size: 32px;
        line-height: 1;
      }
    }
    > span {
      font-size: 12px;
    }
    &:nth-child(1) > i::after {
      content: "\e793";
    }
    &:nth-child(2) > i::after {
      content: "\e791";
    }
    &:nth-child(3) > i::after {
      content: "\e794";
    }
    &:nth-child(4) > i::after {
      content: "\e795";
    }
    &:nth-child(5) > i::after {
      content: "\e790";
    }
  }
}
</style>
