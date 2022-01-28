<template>
  <div>
    <div v-if="openOption === ShapeType.Pen" class="shape-option-panel">
      <span style="left: 20%"></span>
      <div class="shape-option-wrapper">
        <div class="pen-width">
          <label></label>
          <span>{{ whiteboard.lineWidth }}</span>
          <div>
            <Slider :button-size="18" :min="1" :max="20" v-model="whiteboard.lineWidth" />
          </div>
        </div>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === whiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <div v-else-if="openOption === ShapeType.Text" class="shape-option-panel">
      <span style="left: 40%"></span>
      <div class="shape-option-wrapper">
        <div class="font-size">
          <label></label>
          <span>{{ whiteboard.fontSize }}</span>
          <div>
            <Slider :button-size="18" :min="1" :max="21" v-model="whiteboard.fontSize" />
          </div>
        </div>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === whiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <div v-else-if="openOption === 'figure'" class="shape-option-panel">
      <span style="left: 60%"></span>
      <div class="shape-option-wrapper">
        <ul class="figure-list">
          <li :class="{
            active: figureType === ShapeType.Line && whiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Line, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Ellipse && whiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Ellipse, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Rect && whiteboard.fillType === 'none'
          }" @click="() => setFigureType(ShapeType.Rect, 'none')"></li>
          <li :class="{
            active: figureType === ShapeType.Ellipse && whiteboard.fillType === 'color'
          }" @click="() => setFigureType(ShapeType.Ellipse, 'color')"></li>
          <li :class="{
            active: figureType === ShapeType.Rect && whiteboard.fillType === 'color'
          }" @click="() => setFigureType(ShapeType.Rect, 'color')"></li>
        </ul>
        <ul class="color-list">
          <li v-for="color in colorList" :key="color" :style="{color}"
            :class="{active: color === whiteboard.strokeStyle}"
            @click="() => changeStrokeStyle(color)"></li>
        </ul>
      </div>
    </div>
    <ul class="wb-footer">
      <li :class="{active: whiteboard.getToolType() === ShapeType.Select}"
        @click="() => setToolType(ShapeType.Select)">
        <i></i>
        <span>选择</span>
      </li>
      <li :class="{active: whiteboard.getToolType() === ShapeType.Pen}"
        @click="() => changeToolType(ShapeType.Pen)">
        <i></i>
        <span>画笔</span>
      </li>
      <li :class="{active: whiteboard.getToolType() === ShapeType.Text}"
        @click="() => changeToolType(ShapeType.Text)">
        <i></i>
        <span>文字</span>
      </li>
      <li :class="{active: figureTypeList.includes(whiteboard.getToolType())}"
        @click="() => setFigureTool()">
        <i></i>
        <span>图形</span>
      </li>
      <li :class="{active: whiteboard.getToolType() === ShapeType.Delete}"
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

const { ShapeType } = Constants;

const colorList = ['rgba(1, 2, 4, 1)', 'rgba(224, 44, 11, 1)', 'rgba(228, 100, 8, 1)',
  'rgba(231, 189, 14, 1)', 'rgba(84, 214, 18, 1)', 'rgba(144, 222, 15, 1)',
  'rgba(12, 142, 229, 1)', 'rgba(22, 49, 211, 1)', 'rgba(86, 19, 216, 1)'];

export default {
  name: 'WbToolbar',
  components: {
    Slider,
  },
  props: {
    rtcWhiteboard: {
      type: Object,
      default() {
        return {
          getToolType() {
            return ShapeType.Select;
          },
        };
      },
    },
  },
  data() {
    return {
      ShapeType,
      figureTypeList: [ShapeType.Line, ShapeType.Ellipse, ShapeType.Rect],
      colorList,
      openOption: '',
      figureType: ShapeType.Line,
    };
  },
  created() {
    this.whiteboard = this.rtcWhiteboard;
    if (!colorList.includes(this.whiteboard.strokeStyle)) {
      this.changeStrokeStyle(colorList[0]);
    } else {
      this.whiteboard.fillStyle = this.whiteboard.strokeStyle;
    }
    if (this.figureTypeList.includes(this.whiteboard.getToolType())) {
      this.figureType = this.whiteboard.getToolType();
    }
    if (!this.whiteboard.fillStyle) {
      this.whiteboard.fillStyle = 'none';
    }
  },
  methods: {
    changeStrokeStyle(color) {
      this.whiteboard.strokeStyle = color;
      // 两个是一样的
      this.whiteboard.fillStyle = color;
    },
    setToolType(toolType) {
      if (this.whiteboard.getToolType() === toolType) {
        return;
      }
      this.openOption = '';
      this.whiteboard.setToolType(toolType);
    },
    changeToolType(toolType) {
      if (this.whiteboard.getToolType() === toolType) {
        if (this.openOption === toolType) {
          this.openOption = '';
        } else {
          this.openOption = toolType;
        }
        return;
      }
      this.openOption = toolType;
      this.whiteboard.setToolType(toolType);
    },
    setFigureTool() {
      if (this.figureTypeList.includes(this.whiteboard.getToolType())) {
        if (this.openOption === 'figure') {
          this.openOption = '';
        } else {
          this.openOption = 'figure';
        }
        return;
      }
      this.openOption = 'figure';
      this.whiteboard.setToolType(this.figureType);
    },
    setFigureType(toolType, fillStyle) {
      this.figureType = toolType;
      this.whiteboard.setToolType(toolType);
      this.whiteboard.fillType = fillStyle;
    },
  },
};
</script>

<style lang="less" scoped>
@base-path: "../../";
@import "@{base-path}less/variables.less";

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
