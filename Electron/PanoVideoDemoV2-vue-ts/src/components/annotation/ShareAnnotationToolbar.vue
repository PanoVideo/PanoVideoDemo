<template>
  <div class="pano-wb-tb">
    <!-- 穿透点击 -->
    <div
      @click="setToolType(ToolType.Click)"
      class="pano-wb-tb__item"
      :class="{
        'pano-wb-tb__item--selected': insertType === ToolType.Click,
      }"
    >
      <div class="iconfont icon-move" />
    </div>

    <!-- 选择 -->
    <div
      @click="setToolType(ToolType.Select)"
      class="pano-wb-tb__item"
      :class="{
        'pano-wb-tb__item--selected': insertType === ToolType.Select,
      }"
    >
      <div class="iconfont icon-click" />
    </div>

    <!-- 画笔 -->
    <a-popover placement="top" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup">
          <div class="pano-wb-popup__item">
            <span class="pano-wb-popup__item__label">线宽：</span>
            <a-slider
              :style="{ flex: '1', width: '140px' }"
              v-model="penWidth"
              :step="1"
              :min="1"
              :max="20"
              @change="handlerLineWidthChange"
            />
          </div>
          <div class="pano-wb-popup__item">
            <ToolColors
              :selectedColor="strokeStyle"
              @selectColor="didSelectedColor"
            />
          </div>
        </div>
      </template>
      <div
        class="pano-wb-tb__item"
        :class="{
          'pano-wb-tb__item--selected': insertType === ToolType.Pen,
        }"
        @click="setToolType(ToolType.Pen)"
      >
        <span class="iconfont icon-pen" />
        <div
          :class="{
            'pano-wb-tb__item__triangle--selected': insertType === ToolType.Pen,
            'pano-wb-tb__item__triangle': insertType !== ToolType.Pen,
          }"
        />
      </div>
    </a-popover>

    <!-- 图形 -->
    <a-popover placement="top" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup">
          <div class="pano-wb-popup__item pano-wb-popup__item--no-flex">
            <div class="pano-wb-popup__item2">
              <a-tooltip title="直线">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      insertType === ToolType.Line,
                  }"
                  @click="setShapeType(ToolType.Line, 'none')"
                >
                  <span class="iconfont icon-minus" />
                </div>
              </a-tooltip>

              <a-tooltip title="箭头">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      insertType === ToolType.Arrow,
                  }"
                  @click="setShapeType(ToolType.Arrow, 'none')"
                >
                  <span class="iconfont icon-arrow-left2" />
                </div>
              </a-tooltip>

              <a-tooltip title="椭圆, 按住shift可绘制正圆">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      fillType === 'none' && insertType === ToolType.Ellipse,
                  }"
                  @click="setShapeType(ToolType.Ellipse, 'none')"
                >
                  <span class="iconfont icon-radio-unchecked" />
                </div>
              </a-tooltip>

              <a-tooltip title="矩形, 按住shift可绘制正方形">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      fillType === 'none' && insertType === ToolType.Rect,
                  }"
                  @click="setShapeType(ToolType.Rect, 'none')"
                >
                  <span class="iconfont icon-checkbox-unchecked" />
                </div>
              </a-tooltip>
            </div>
            <div class="pano-wb-popup__item2">
              <a-tooltip title="椭圆, 按住shift可绘制正圆">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      fillType === 'color' && insertType === ToolType.Ellipse,
                  }"
                  @click="setShapeType(ToolType.Ellipse, 'color')"
                >
                  <span class="iconfont icon-circle-h" />
                </div>
              </a-tooltip>
              <a-tooltip title="矩形, 按住shift可绘制正方形">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      fillType === 'color' && insertType === ToolType.Rect,
                  }"
                  @click="setShapeType(ToolType.Rect, 'color')"
                >
                  <span class="iconfont icon-rect-h" />
                </div>
              </a-tooltip>
            </div>
          </div>
          <div class="pano-wb-popup__item">
            <span class="pano-wb-popup__item__label">线宽: </span>
            <a-slider
              :style="{ flex: '1', maxWidth: '140px' }"
              v-model="shapeWidth"
              :step="1"
              :min="1"
              :max="20"
              @change="handlerLineWidthChange"
            />
          </div>
          <ToolColors
            :selectedColor="strokeStyle"
            @selectColor="didSelectedColor"
          />
        </div>
      </template>
      <div
        :class="{
          'pano-withtip': true,
          'pano-wb-tb__item': true,
          'pano-wb-tb__item--selected': this.isShapeSelected,
        }"
        @click="chooseShape"
      >
        <span class="iconfont icon-solid" />
        <div
          :class="
            isShapeSelected
              ? 'pano-wb-tb__item__triangle--selected'
              : 'pano-wb-tb__item__triangle'
          "
        />
      </div>
    </a-popover>

    <!-- 文本 -->
    <a-popover placement="top" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup">
          <div class="pano-wb-popup__item">
            <a-tooltip title="加粗">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected': bold,
                }"
                @click="toggleFontBold"
              >
                <span class="iconfont icon-bold" />
              </div>
            </a-tooltip>
            <a-tooltip title="斜体">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected': italic,
                }"
                @click="toggleItalics"
              >
                <span class="iconfont icon-italic" />
              </div>
            </a-tooltip>
          </div>
          <div class="pano-wb-popup__item">
            <span class="pano-wb-popup__item__label">字号：</span>
            <a-slider
              :style="{ flex: '1', width: '140px' }"
              v-model="fontSize"
              :step="2"
              :min="12"
              :max="60"
              @change="handlerFontSizeChange"
            />
          </div>
          <ToolColors
            :selectedColor="strokeStyle"
            @selectColor="didSelectedColor"
          />
        </div>
      </template>
      <div
        :class="{
          'pano-withtip': true,
          'pano-wb-tb__item': true,
          'pano-wb-tb__item--selected': insertType === ToolType.Text,
        }"
        @click="setToolType(ToolType.Text)"
      >
        <span class="iconfont icon-text" />
        <div
          :class="
            insertType === ToolType.Text
              ? 'pano-wb-tb__item__triangle--selected'
              : 'pano-wb-tb__item__triangle'
          "
        />
      </div>
    </a-popover>

    <!-- 橡皮擦 -->
    <a-popover placement="top" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup" :style="{ width: 'auto' }">
          <div class="pano-wb-popup__item">
            <a-tooltip title="擦除涂抹到的轨迹">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected':
                    insertType === ToolType.Eraser,
                }"
                @click="setToolType(ToolType.Eraser)"
              >
                <span class="iconfont icon-eraser" />
              </div>
            </a-tooltip>
            <a-tooltip title="删除与擦除轨迹有交汇的文字、图形">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected':
                    insertType === ToolType.Delete,
                }"
                @click="setToolType(ToolType.Delete)"
              >
                <span class="iconfont icon-delete" />
              </div>
            </a-tooltip>
          </div>
          <div class="pano-wb-popup__item">
            <span>大小：</span>
            <a-slider
              :style="{ flex: '1', maxWidth: '140px', minWidth: '100px' }"
              v-model="earserWidth"
              :step="1"
              :min="10"
              :max="100"
              @change="handlerLineWidthChange"
            />
          </div>
          <div class="pano-wb-popup__item" :style="{ display: 'block' }">
            <a-button
              type="text"
              size="small"
              :style="{ display: 'block', marginBottom: '5px' }"
              @click="clearMyContents"
            >
              清除我的笔迹
            </a-button>
          </div>
        </div>
      </template>
      <div
        :class="{
          'pano-withtip': true,
          'pano-wb-tb__item': true,
          'pano-wb-tb__item--selected': isEraseSelected,
        }"
        @click="setToolType(ToolType.Delete)"
      >
        <span class="iconfont icon-eraser" />
        <div
          :class="
            isEraseSelected
              ? 'pano-wb-tb__item__triangle--selected'
              : 'pano-wb-tb__item__triangle'
          "
        />
      </div>
    </a-popover>

    <!-- 撤销 -->
    <div @click="whiteboard.undo()" class="pano-wb-tb__item">
      <span class="iconfont icon-undo" />
    </div>

    <!-- 重做 -->
    <div @click="whiteboard.redo()" class="pano-wb-tb__item">
      <span class="iconfont icon-redo" />
    </div>
  </div>
</template>

<script>
import { Popover, Slider, Button, Tooltip } from 'ant-design-vue';
import { AnnotationProxy } from '@pano.video/panorts';
import { randomWbColor } from '@/pano/panorts';
import ToolColors from '@/components/whiteboard/ColorPicker';

const { ToolType, WBClearType } = AnnotationProxy;

export default {
  name: 'Toolbar',
  props: {
    isAdmin: Boolean,
    whiteboard: AnnotationProxy,
  },
  components: {
    ToolColors,
    'a-popover': Popover,
    'a-tooltip': Tooltip,
    'a-button': Button,
    'a-slider': Slider,
  },
  data() {
    return {
      ToolType,
      WBClearType,
      fontSize: 24,
      bold: this.whiteboard.bold,
      italic: this.whiteboard.italic,
      scale: this.whiteboard.scale,
      penWidth: this.whiteboard.lineWidth,
      shapeWidth: 3,
      earserWidth: 50,
      strokeStyle: randomWbColor(),
      insertType: this.whiteboard.getToolType(),
      fillType: this.whiteboard.fillType,
    };
  },
  computed: {
    isShapeSelected() {
      return (
        (this.fillType === 'none' &&
          (this.insertType === ToolType.Line ||
            this.insertType === ToolType.Arrow ||
            this.insertType === ToolType.Rect ||
            this.insertType === ToolType.Ellipse)) ||
        (this.fillType === 'color' &&
          (this.insertType === ToolType.Rect ||
            this.insertType === ToolType.Ellipse))
      );
    },
    isEraseSelected() {
      return (
        this.insertType === ToolType.Delete ||
        this.insertType === ToolType.Eraser
      );
    },
  },
  methods: {
    setToolType(type) {
      switch (type) {
        case ToolType.Pen:
          this.whiteboard.lineWidth = this.penWidth;
          break;
        case ToolType.Eraser:
          this.whiteboard.lineWidth = this.earserWidth;
      }
      this.whiteboard.setToolType(type);
      this.insertType = type;
      this.$emit('updateIgnoreMouseEvents', type === ToolType.Click);
    },
    setShapeType(type, fillType) {
      this.insertType = type;
      this.fillType = fillType;
      this.setToolType(type);
      this.whiteboard.fillType = fillType;
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeStyle({
          strokeStyle: this.strokeStyle,
          fillType,
        });
      }
    },
    chooseShape() {
      this.whiteboard.lineWidth = this.shapeWidth;
      if (!this.isShapeSelected) {
        this.setShapeType(ToolType.Line, 'none');
      }
    },
    handlerLineWidthChange(lineWidth) {
      if (lineWidth > 0) {
        if (this.whiteboard.selectedShape) {
          this.whiteboard.setSelectedShapeStyle({
            lineWidth,
          });
        }
        this.whiteboard.lineWidth = lineWidth;
      }
    },
    handlerFontSizeChange(fontSize) {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          fontSize,
        });
      } else {
        this.whiteboard.fontSize = fontSize;
      }
    },
    toggleItalics() {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          italic: !this.italic,
        });
      } else {
        this.whiteboard.italic = !this.italic;
      }
      this.italic = !this.italic;
    },
    toggleFontBold() {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          bold: !this.bold,
        });
      } else {
        this.whiteboard.bold = !this.bold;
      }
      this.bold = !this.bold;
    },
    didSelectedColor(color) {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeStyle({
          strokeStyle: color,
          fillStyle: color,
        });
      }
      this.whiteboard.fillStyle = color;
      this.whiteboard.strokeStyle = color;
      this.strokeStyle = color;
    },
    clearMyContents() {
      this.whiteboard.clearUserContents(
        this.whiteboard.userId,
        false,
        WBClearType.DRAWS
      );
    },
  },
  mounted() {
    // this.whiteboard.enableCursorSync();
    this.didSelectedColor(this.strokeStyle);
  },
};
</script>

<style lang="less" scoped>
@deep-grey: #e6e6e6;

.pano-wb-at * {
  user-select: none;
}
.pano-wb-tb {
  border-radius: 5px;
  font-size: 12px;
  padding: 5px;
  position: absolute;
  z-index: 12;
  left: 50%;
  bottom: 70px;
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
  font-size: 12px;
  box-shadow: 2px 2px 8px rgba(20, 14, 14, 0.2);
  color: #444;
  background-color: #fff;
  &__item {
    font-size: 18px;
    display: inline-flex;
    width: 30px;
    height: 30px;
    padding: 2px;
    justify-content: center;
    align-items: center;
    border-radius: 5px;
    cursor: pointer;
    margin: 0 6px;
    color: #666;
    position: relative;
    &:hover {
      background-color: lightgray;
    }
    &--selected {
      background-color: @deep-grey;
      // color: #0899f9;
      &:hover {
        background-color: lightgray;
      }
    }
    &__color-dot {
      position: absolute;
      right: 0;
      bottom: 3px;
      width: 6px;
      height: 6px;
      border-radius: 6px;
    }
    &__triangle {
      position: absolute;
      right: 2px;
      bottom: 3px;
      border: 3px solid transparent;
      border-right-color: #333;
      border-bottom-color: #333;
      &--selected {
        position: absolute;
        right: 2px;
        bottom: 3px;
        border: 3px solid transparent;
        border-right-color: #0899f9;
        border-bottom-color: #0899f9;
      }
    }
  }
}

.pano-wb-popup {
  font-size: 12px;
  button:hover {
    background-color: #eee;
  }
  &__item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    button {
      font-size: 12px !important;
      border: none;
    }
    &__label {
      font-size: 12px;
      padding-right: 5px;
    }
    &__select {
      font-size: 18px;
      display: inline-flex;
      width: 40px;
      height: 36px;
      padding: 8px 0;
      justify-content: center;
      align-items: center;
      border-radius: 3px;
      cursor: pointer;
      color: #333;
      position: relative;
      &:hover {
        background-color: lightgray;
      }
      &--selected {
        color: #0899f9;
        &:hover {
          background-color: lightgray;
        }
      }
    }
    // &__select ~ &__select {
    //   margin-left: 5px;
    // }
    &__colors {
      width: 25%;
      display: flex;
      justify-content: center;
      padding: 8px 0;
      &__color-dot {
        cursor: pointer;
        width: 20px;
        height: 20px;
        position: relative;
        &--selected::before {
          position: absolute;
          left: -5px;
          top: -5px;
          width: 30px;
          height: 30px;
          border: 1px solid #0899f9;
          content: '';
        }
      }
    }
    &--no-flex {
      display: block;
    }
    &--fixedWith {
      width: 160px;
    }
  }

  &__item ~ &__item {
    margin-top: 5px;
    position: relative;
    line-height: 32px;
    padding-top: 5px;
    &::before {
      position: absolute;
      content: '';
      width: 100%;
      height: 1px;
      background-color: #eee;
      top: 0;
    }
  }

  &__item2 {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    width: 160px;
  }
}
</style>
