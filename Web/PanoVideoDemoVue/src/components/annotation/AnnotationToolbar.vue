<template>
  <div class="pano-wb-at">
    <!-- 选择 -->
    <div
      :class="{
        'pano-wb-at__item': true,
        'pano-wb-at__item--selected': insertType === Constants.ShapeType.Select
      }"
      @click="setToolType(Constants.ShapeType.Select)"
      class="pano-wb-at__item "
    >
      <span class="iconfont icon-move" />
    </div>

    <!-- 画笔 -->
    <div
      :class="{
        'pano-wb-at__item': true,
        'pano-wb-at__item--selected': insertType === Constants.ShapeType.Pen
      }"
      @click="setToolType(Constants.ShapeType.Pen)"
    >
      <span class="iconfont icon-pencil" />
    </div>

    <!-- 空心图形 -->
    <Popover placement="top" trigger="click">
      <div class="pano-wb-popup">
        <div class="pano-wb-popup__item">
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                insertType === Constants.ShapeType.Line
            }"
            @click="setToolType(Constants.ShapeType.Line)"
          >
            <span class="iconfont icon-minus" />
          </div>
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                insertType === Constants.ShapeType.Arrow
            }"
            @click="setToolType(Constants.ShapeType.Arrow)"
          >
            <span class="iconfont icon-arrow-left2" />
          </div>
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                fillType === 'none' &&
                insertType === Constants.ShapeType.Ellipse
            }"
            @click="setToolType(Constants.ShapeType.Ellipse)"
          >
            <span class="iconfont icon-radio-unchecked" />
          </div>
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                fillType === 'none' && insertType === Constants.ShapeType.Rect
            }"
            @click="setToolType(Constants.ShapeType.Rect)"
          >
            <span class="iconfont icon-checkbox-unchecked" />
          </div>
        </div>
        <div class="pano-wb-popup__item">
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                fillType === 'color' &&
                insertType === Constants.ShapeType.Ellipse
            }"
            @click="setSolidToolType(Constants.ShapeType.Ellipse)"
          >
            <span class="iconfont icon-circle-h" />
          </div>
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                fillType === 'color' && insertType === Constants.ShapeType.Rect
            }"
            @click="setSolidToolType(Constants.ShapeType.Rect)"
          >
            <span class="iconfont icon-rect-h" />
          </div>
        </div>
      </div>
      <div
        :class="{
          'pano-wb-at__item': true,
          'pano-wb-at__item--selected':
            fillType === 'none' &&
            (insertType === Constants.ShapeType.Line ||
              insertType === Constants.ShapeType.Arrow ||
              insertType === Constants.ShapeType.Rect ||
              insertType === Constants.ShapeType.Ellipse)
        }"
        slot="reference"
      >
        <span class="iconfont icon-hollow" />
        <div class="pano-wb-at__item__triangle" />
      </div>
    </Popover>

    <!-- 文本 -->
    <Popover placement="top" trigger="click">
      <div
        :class="{
          'pano-wb-at__item': true,
          'pano-wb-at__item--selected': insertType === Constants.ShapeType.Text
        }"
        slot="reference"
        @click="setToolType(Constants.ShapeType.Text)"
      >
        <span class="iconfont icon-text" />
        <div class="pano-wb-at__item__triangle" />
      </div>
      <div class="pano-wb-popup">
        <div
          :class="{
            'pano-wb-popup__item__select': true,
            'pano-wb-popup__item__select--selected': bold
          }"
          @click="toggleBold"
        >
          <span class="iconfont icon-bold" />
        </div>
        <div
          :class="{
            'pano-wb-popup__item__select': true,
            'pano-wb-popup__item__select--selected': italic
          }"
          @click="toggleItalic"
        >
          <span class="iconfont icon-italic" />
        </div>
      </div>
    </Popover>

    <!-- 工具箱 -->
    <Popover placement="top" trigger="click">
      <div class="pano-wb-popup">
        <div class="pano-wb-popup__item">
          <span class="pano-wb-popup__item__label">线宽：</span>
          <el-slider
            :value="lineWidth"
            @input="setLineWidth"
            :step="1"
            :min="1"
            :max="20"
            style="flex: 1; max-width: 140px"
          />
        </div>
        <div class="pano-wb-popup__item">
          <span class="pano-wb-popup__item__label">字号：</span>
          <el-slider
            :step="2"
            :min="12"
            :max="60"
            :value="fontSize"
            @input="setFontSize"
            style="flex: 1; max-width: 140px"
          />
        </div>
        <div class="pano-wb-popup__item">
          <div
            v-for="color in colors"
            class="pano-wb-popup__item__colors"
            :key="color"
            @click="selectColor(color)"
          >
            <div
              :class="{
                'pano-wb-popup__item__colors__color-dot': true,
                'pano-wb-popup__item__colors__color-dot--selected':
                  selectedColor === color
              }"
              :style="{
                backgroundColor: color
              }"
            />
          </div>
        </div>
      </div>

      <div class="pano-wb-at__item" slot="reference">
        <span class="iconfont icon-tools" />
        <div
          class="pano-wb-at__item__color-dot"
          :style="{ backgroundColor: selectedColor }"
        />
      </div>
    </Popover>

    <!-- 橡皮擦 -->
    <Popover placement="top" trigger="click">
      <div class="pano-wb-popup">
        <div class="pano-wb-popup__item">
          <div
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                insertType === Constants.ShapeType.Eraser
            }"
            @click="
              () => {
                this.whiteboard.setToolType(Constants.ShapeType.Eraser);
                this.insertType = Constants.ShapeType.Eraser;
              }
            "
          >
            <span class="iconfont icon-eraser" />
          </div>
          <div
            @click="
              () => {
                this.whiteboard.setToolType(Constants.ShapeType.Delete);
                this.insertType = Constants.ShapeType.Delete;
              }
            "
            :class="{
              'pano-wb-popup__item__select': true,
              'pano-wb-popup__item__select--selected':
                insertType === Constants.ShapeType.Delete
            }"
          >
            <span class="iconfont icon-delete" />
          </div>
        </div>
      </div>

      <div class="pano-wb-at__item" slot="reference">
        <span class="iconfont icon-eraser" />
        <div class="pano-wb-at__item__triangle" />
      </div>
    </Popover>

    <!-- 激光笔 -->
    <div
      :class="{
        'pano-wb-at__item': true,
        'pano-wb-at__item--selected':
          insertType === Constants.ShapeType.LaserPointer
      }"
      @click="setToolType(Constants.ShapeType.LaserPointer)"
    >
      <span class="iconfont icon-laser-pointer" style="font-size: 20px" />
    </div>

    <!-- 撤销 -->
    <div @click="undo" class="pano-wb-at__item">
      <span class="iconfont icon-undo" />
    </div>

    <!-- 重做 -->
    <div @click="redo" class="pano-wb-at__item">
      <span class="iconfont icon-redo" />
    </div>
  </div>
</template>

<script>
import { RtcWhiteboard, Constants } from '@pano.video/whiteboard';
import { mapGetters } from 'vuex';
import { get } from 'lodash-es';
import { Popover } from 'element-ui';

const colors = [
  'RGBA(220, 85, 76, 1.00)',
  'RGBA(238, 155, 78, 1.00)',
  'RGBA(250, 230, 115, 1.00)',
  'RGBA(153, 190, 103, 1.00)',
  'RGBA(84, 170, 233, 1.00)',
  'RGBA(69, 89, 170, 1.00)',
  'RGBA(141, 65, 165, 1.00)',
  'RGBA(239, 161, 157, 1.00)',
  'RGBA(255, 255, 255, 1.00)',
  'RGBA(155, 155, 153, 1.00)',
  'RGBA(81, 84, 91, 1.00)',
  'RGBA(0, 0, 0, 1.00)'
];

export default {
  data() {
    return {
      Constants,
      colors,
      imgUrl: 'https://developer.pano.video/download/example.jpeg',
      imgScaleMode: Constants.WBImageScalingMode.Fit,
      fontSize: this.whiteboard.fontSize,
      bold: this.whiteboard.bold,
      italic: this.whiteboard.italic,
      scale: this.whiteboard.scale,
      pageCount: this.whiteboard.getTotalNumberOfPages(),
      pageIndex: this.whiteboard.getCurrentPageNumber(),
      lineWidth: this.whiteboard.lineWidth,
      selectedColor: this.whiteboard.strokeStyle,
      insertType: this.whiteboard.getToolType(),
      fillType: this.whiteboard.fillType,
      activeDocId: this.whiteboard.activeDocId,
      activeDoc: this.whiteboard.activeDoc,
      imgUrlList: '',
      uploadProgress: 0,
      uploading: false,
      snapshotingView: false,
      snapshotingAll: false
    };
  },
  props: {
    whiteboard: {
      type: Object,
      required: true
    }
  },
  components: {
    Popover
  },
  computed: {
    ...mapGetters(['userMe', 'wbAdminUser']),
    isAdmin() {
      return this.userMe !== this.wbAdminUser;
    },
    thumbUrls() {
      // 课件预览图
      return this.activeDoc.courseware.thumbUrls || [];
    }
  },
  watch: {
    whiteboard() {
      this.getDataFromWhiteboard();
    }
  },
  methods: {
    /**
     * 从白板中获取最新的状态
     */
    getDataFromWhiteboard() {
      this.selectedColor = get(
        this.whiteboard.selectedShape,
        'shapeStyle.strokeStyle',
        this.whiteboard.strokeStyle
      );
      this.lineWidth = get(
        this.whiteboard.selectedShape,
        'shapeStyle.lineWidth',
        this.whiteboard.lineWidth
      );
      this.bold = get(
        this.whiteboard.selectedShape,
        'fontStyle.bold',
        this.whiteboard.bold
      );
      this.italic = get(
        this.whiteboard.selectedShape,
        'fontStyle.italic',
        this.whiteboard.italic
      );
      this.fontSize = get(
        this.whiteboard.selectedShape,
        'fontStyle.fontSize',
        this.whiteboard.fontSize
      );
      this.fillType = get(
        this.whiteboard.selectedShape,
        'shapeStyle.fillType',
        this.whiteboard.fillType
      );
      this.scale = this.whiteboard.scale;
      this.pageCount = this.whiteboard.getTotalNumberOfPages();
      this.pageIndex = this.whiteboard.getCurrentPageNumber();
      this.insertType = this.whiteboard.getToolType();
    },
    async snapshotAll() {
      this.snapshotingAll = true;
      await this.whiteboard.snapshot(true, 'all');
      this.snapshotingAll = false;
    },
    async snapshotView() {
      this.snapshotingView = true;
      await this.whiteboard.snapshot(true, 'view');
      this.snapshotingView = false;
    },
    toggleItalic() {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          italic: !this.italic
        });
      } else {
        this.whiteboard.italic = !this.italic;
      }
      this.italic = !this.italic;
    },
    toggleBold() {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          bold: !this.bold
        });
      } else {
        this.whiteboard.bold = !this.bold;
      }
      this.bold = !this.bold;
    },
    setToolType(type) {
      this.whiteboard.setToolType(type);
      this.insertType = type;
      this.fillType = 'none';
      this.whiteboard.fillType = 'none';
    },
    setSolidToolType(type) {
      this.whiteboard.setToolType(type);
      this.insertType = type;
      this.fillType = 'color';
      this.whiteboard.fillType = 'color';
    },
    setLineWidth(newValue) {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeStyle({ lineWidth: newValue });
      } else {
        this.whiteboard.lineWidth = newValue;
      }
      this.lineWidth = newValue;
    },
    setFontSize(newValue) {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeFontStyle({
          fontSize: newValue
        });
      } else {
        this.whiteboard.fontSize = newValue;
      }
      this.fontSize = newValue;
    },
    selectColor(color) {
      if (this.whiteboard.selectedShape) {
        this.whiteboard.setSelectedShapeStyle({
          strokeStyle: color,
          fillStyle: color
        });
      } else {
        this.whiteboard.strokeStyle = color;
        this.whiteboard.fillStyle = color;
      }
      this.selectedColor = color;
    },
    undo() {
      this.whiteboard.undo();
    },
    redo() {
      this.whiteboard.redo();
    }
  },
  mounted() {
    this.whiteboard.on(
      RtcWhiteboard.Events.whiteboardContentUpdate,
      this.getDataFromWhiteboard
    );
    this.whiteboard.lineWidth = 3;
    this.lineWidth = 3;
  },
  beforeDestroy() {
    this.whiteboard.off(
      RtcWhiteboard.Events.whiteboardContentUpdate,
      this.getDataFromWhiteboard
    );
  }
};
</script>

<style lang="scss" scoped>
$deep-grey: #e6e6e6;
$light-grey: #f1f1f1;
$white-bg: #fff;
$font-color: #2b3139;
$hover-bg: #e9f4f8;
$fixed-left: 40px;
// $hover-bg: #f3f4f4;

.el-popover {
  min-width: 40px !important;
}

.pano-wb-at * {
  user-select: none;
}

.pano-wb-at {
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
      background-color: $light-grey;
    }
    &--selected {
      background-color: $deep-grey;
      &:hover {
        background-color: $deep-grey;
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
      border-right-color: #666;
      border-bottom-color: #666;
    }
  }
}

.pano-wb-popup {
  font-size: 12px;
  &__item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    button {
      font-size: 12px !important;
    }
    &__label {
      font-size: 12px;
      padding-right: 5px;
    }
    &__select {
      font-size: 18px;
      display: inline-flex;
      width: 30px;
      height: 30px;
      padding: 2px;
      justify-content: center;
      align-items: center;
      border-radius: 3px;
      cursor: pointer;
      color: #666;
      position: relative;
      &:hover {
        background-color: $light-grey;
      }
      &--selected {
        background-color: $deep-grey;
        &:hover {
          background-color: $deep-grey;
        }
      }
    }
    &__select ~ &__select {
      margin-left: 5px;
    }
    &__colors {
      width: 25%;
      display: flex;
      justify-content: center;
      padding: 8px 0;
      &__color-dot {
        cursor: pointer;
        box-shadow: 0 0 6px rgba(0, 0, 0, 0.4);
        width: 20px;
        height: 20px;
        border-radius: 50%;
        transition: all 0.3s ease-in-out;
        position: relative;
        &:hover {
          box-shadow: 0 0 10px rgba(0, 0, 0, 0.6);
        }
        &--selected::before {
          position: absolute;
          left: -1px;
          top: -1px;
          width: 22px;
          height: 22px;
          border-radius: 50%;
          background-color: transparent;
          background-clip: content-box;
          border: 1px solid #fff;
          box-shadow: 0 0 8px rgba(0, 0, 0, 0.5);
          content: '';
        }
      }
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
}

.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>
