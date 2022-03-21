<template>
  <div
    :class="{
      'pano-wb-tb': true,
      'pano-wb-tb--hidden': toolbarHidden,
    }"
  >
    <div
      :class="{ 'hide-hander': true, 'hide-hander--sticky': toolbarHidden }"
      @click="toggleHide"
    >
      <i
        :class="[
          'iconfont',
          toolbarHidden ? 'icon-rightArrow' : 'icon-leftArrow',
        ]"
      />
    </div>

    <!-- 课件交互 -->
    <div
      v-if="isHost"
      class="pano-wb-tb__item pano-withtip"
      :class="{
        'pano-wb-tb__item--selected': toolType === ToolType.Click,
      }"
      @click="setToolType(ToolType.Click)"
      data-tip="课件交互"
    >
      <div class="iconfont icon-move" />
    </div>

    <!-- 选择 -->
    <div
      @click="setToolType(ToolType.Select)"
      class="pano-wb-tb__item pano-withtip"
      :class="{
        'pano-wb-tb__item--selected': toolType === ToolType.Select,
      }"
      data-tip="图形选择"
    >
      <div class="iconfont icon-click" />
    </div>

    <!-- 激光笔 -->
    <div
      v-if="isHost"
      class="pano-wb-tb__item pano-withtip"
      :class="{
        'pano-wb-tb__item--selected': toolType === ToolType.LaserPointer,
      }"
      @click="setToolType(ToolType.LaserPointer)"
      data-tip="激光笔"
    >
      <div class="iconfont icon-laser-pointer" :style="{ fontSize: '20px' }" />
    </div>

    <!-- 画笔 -->
    <a-popover placement="right" trigger="click">
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
        class="pano-wb-tb__item pano-withtip"
        :class="{
          'pano-wb-tb__item--selected': toolType === ToolType.Pen,
        }"
        data-tip="画笔"
        @click="setToolType(ToolType.Pen)"
      >
        <span class="iconfont icon-pen" />
        <div
          :class="{
            'pano-wb-tb__item__triangle--selected': toolType === ToolType.Pen,
            'pano-wb-tb__item__triangle': toolType !== ToolType.Pen,
          }"
        />
      </div>
    </a-popover>

    <!-- 图形 -->
    <a-popover placement="right" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup">
          <div class="pano-wb-popup__item pano-wb-popup__item--no-flex">
            <div class="pano-wb-popup__item2">
              <a-tooltip title="直线">
                <div
                  :class="{
                    'pano-wb-popup__item__select': true,
                    'pano-wb-popup__item__select--selected':
                      toolType === ToolType.Line,
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
                      toolType === ToolType.Arrow,
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
                      fillType === 'none' && toolType === ToolType.Ellipse,
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
                      fillType === 'none' && toolType === ToolType.Rect,
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
                      fillType === 'color' && toolType === ToolType.Ellipse,
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
                      fillType === 'color' && toolType === ToolType.Rect,
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
        data-tip="图形"
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

    <!-- 图章 -->
    <Stamps
      :whiteboard="whiteboard"
      :selected="toolType === ToolType.Stamp"
      @select="setToolType(ToolType.Stamp)"
    />

    <!-- 文本 -->
    <a-popover placement="right" trigger="click">
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
          'pano-wb-tb__item--selected': toolType === ToolType.Text,
        }"
        data-tip="文本"
        @click="setToolType(ToolType.Text)"
      >
        <span class="iconfont icon-text" />
        <div
          :class="
            toolType === ToolType.Text
              ? 'pano-wb-tb__item__triangle--selected'
              : 'pano-wb-tb__item__triangle'
          "
        />
      </div>
    </a-popover>

    <!-- 橡皮擦 -->
    <a-popover placement="right" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup" :style="{ width: 'auto' }">
          <div class="pano-wb-popup__item">
            <a-tooltip title="擦除涂抹到的轨迹">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected':
                    toolType === ToolType.Eraser,
                }"
                @click="setToolType(ToolType.Eraser)"
              >
                <span class="iconfont icon-brush" />
              </div>
            </a-tooltip>
            <a-tooltip title="删除与擦除轨迹有交汇的文字、图形">
              <div
                :class="{
                  'pano-wb-popup__item__select': true,
                  'pano-wb-popup__item__select--selected':
                    toolType === ToolType.Delete,
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
              size="small"
              type="text"
              :style="{ display: 'block', marginBottom: '5px' }"
              @click="clearContents(false, WBClearType.All)"
              v-if="isHost"
            >
              清除所有内容
            </a-button>
            <a-button
              v-if="isHost"
              type="text"
              size="small"
              :style="{ display: 'block', marginBottom: '5px' }"
              @click="clearContents(true, WBClearType.All)"
            >
              清除当前页
            </a-button>
            <a-button
              type="text"
              size="small"
              :style="{ display: 'block', marginBottom: '5px' }"
              @click="clearMyContents(WBClearType.DRAWS)"
            >
              清除我的笔迹
            </a-button>
            <a-button
              type="text"
              size="small"
              :style="{ display: 'block', marginBottom: '5px' }"
              @click="clearMyContents(WBClearType.BACKGROUND_IMAGE)"
            >
              清除我的背景图
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
        data-tip="橡皮擦"
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
    <div
      @click="whiteboard.undo()"
      class="pano-wb-tb__item pano-withtip"
      data-tip="撤销"
    >
      <span class="iconfont icon-undo" />
    </div>

    <!-- 重做 -->
    <div
      @click="whiteboard.redo()"
      class="pano-wb-tb__item pano-withtip"
      data-tip="重做"
    >
      <span class="iconfont icon-redo" />
    </div>

    <!-- 截图 -->
    <a-popover placement="right" trigger="click">
      <template slot="content">
        <div class="pano-wb-popup">
          <a-button
            size="small"
            :loading="snapshotingAll"
            @click="snapshot('all')"
          >
            全部内容
          </a-button>
          <a-button
            size="small"
            style="margin-left: 10px"
            :loading="snapshotingView"
            @click="snapshot('view')"
          >
            可见区域
          </a-button>
        </div>
      </template>
      <div class="pano-wb-tb__item pano-withtip" data-tip="截图">
        <div class="iconfont icon-snapshot" />
      </div>
    </a-popover>

    <!-- 工具箱 -->
    <a-popover placement="right" trigger="click" v-if="isHost">
      <template slot="content">
        <div class="toolbox-panel">
          <div class="toolbox-panel__btn" @click="uploadAudio">
            <i class="iconfont toolbox-panel__btn__icon icon-audio" />
            <span class="toolbox-panel__btn__label">上传音频</span>
          </div>
          <div class="toolbox-panel__btn" @click="uploadVideo">
            <i class="iconfont toolbox-panel__btn__icon icon-medias" />
            <span class="toolbox-panel__btn__label">上传视频</span>
          </div>
          <div class="toolbox-panel__btn" @click="uploadImage">
            <i class="iconfont toolbox-panel__btn__icon icon-images" />
            <span class="toolbox-panel__btn__label">上传图片</span>
          </div>

          <div class="toolbox-panel__btn" @click="uploadLocalImage">
            <i class="iconfont toolbox-panel__btn__icon icon-images" />
            <span class="toolbox-panel__btn__label">上传背景图</span>
          </div>

          <div
            class="toolbox-panel__btn"
            :style="{
              color: isWhiteboardHidden ? '#0899F9' : '#333',
            }"
            @click="toggleHideShapes"
          >
            <i class="iconfont toolbox-panel__btn__icon icon-track" />
            <span class="toolbox-panel__btn__label">
              {{ !isWhiteboardHidden ? '隐藏笔迹' : '已隐藏笔迹' }}
            </span>
          </div>

          <div
            class="toolbox-panel__btn"
            :style="{
              color: isSharingVision ? '#0899F9' : '#333',
            }"
            @click="toggleShareVision"
          >
            <i class="iconfont toolbox-panel__btn__icon icon-vision" />
            <span class="toolbox-panel__btn__label">
              {{ !isSharingVision ? '视角分享' : '正在分享视角' }}
            </span>
          </div>
        </div>
      </template>
      <div class="pano-wb-tb__item pano-withtip" data-tip="工具箱">
        <span class="iconfont icon-toolbox" />
        <div class="pano-wb-tb__item__triangle" />
      </div>
    </a-popover>
  </div>
</template>

<script>
import { Popover, Slider, Button, Tooltip } from 'ant-design-vue';
import { RtcWhiteboard } from '@pano.video/panorts';
import { randomWbColor } from '@/pano/panorts';
import ToolColors from '@/components/whiteboard/ColorPicker';
import Stamps from '@/components/whiteboard/Stamps.vue';

const { ToolType, WBClearType, WBImageScalingMode } = RtcWhiteboard;

export default {
  name: 'Toolbar',
  data() {
    return {
      ToolType,
      WBClearType,
      WBImageScalingMode,
      imageUploading: false,
      imageUploadProgress: '',
      audioUploading: false,
      audioUploadProgress: '',
      videoUploading: false,
      videoUploadProgress: '',
      imgScaleMode: WBImageScalingMode.Fit,
      fontSize: 24,
      bold: this.whiteboard.bold,
      italic: this.whiteboard.italic,
      scale: this.whiteboard.scale,
      pageCount: this.whiteboard.getTotalNumberOfPages(),
      pageIndex: this.whiteboard.getCurrentPageNumber(),
      penWidth: this.whiteboard.lineWidth,
      shapeWidth: 3,
      earserWidth: 50,
      strokeStyle: randomWbColor(),
      toolType: this.whiteboard.getToolType(),
      fillType: this.whiteboard.fillType,
      snapshotingAll: false,
      snapshotingView: false,
      isWhiteboardHidden: false,
      isSharingVision: false,
      toolbarHidden: false,
    };
  },
  props: {
    isHost: Boolean,
    whiteboard: RtcWhiteboard,
  },
  components: {
    ToolColors,
    Stamps,
    'a-popover': Popover,
    'a-tooltip': Tooltip,
    'a-button': Button,
    'a-slider': Slider,
  },
  computed: {
    isShapeSelected() {
      return (
        (this.fillType === 'none' &&
          (this.toolType === ToolType.Line ||
            this.toolType === ToolType.Arrow ||
            this.toolType === ToolType.Rect ||
            this.toolType === ToolType.Ellipse)) ||
        (this.fillType === 'color' &&
          (this.toolType === ToolType.Rect ||
            this.toolType === ToolType.Ellipse))
      );
    },
    isEraseSelected() {
      return (
        this.toolType === ToolType.Delete || this.toolType === ToolType.Eraser
      );
    },
  },
  methods: {
    toggleHide() {
      this.toolbarHidden = !this.toolbarHidden;
    },
    toggleShareVision() {
      if (this.isSharingVision) {
        this.whiteboard.stopVisionShare();
        this.$message.success('您已停止视角分享');
      } else {
        this.$message.success('其他用户将跟随您的视角');
        this.whiteboard.startVisionShare();
      }
      this.isSharingVision = !this.isSharingVision;
    },
    toggleHideShapes() {
      if (!this.isWhiteboardHidden) {
        this.whiteboard.hideWhiteboard();
      } else {
        this.whiteboard.showWhiteboard();
      }
      this.isWhiteboardHidden = !this.isWhiteboardHidden;
    },
    setToolType(type) {
      switch (type) {
        case ToolType.Pen:
          this.whiteboard.lineWidth = this.penWidth;
          break;
        case ToolType.Eraser:
          this.whiteboard.lineWidth = this.earserWidth;
      }
      this.whiteboard.setToolType(type);
      this.toolType = type;
    },
    setShapeType(type, fillType) {
      this.toolType = type;
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
    uploadImage() {
      this.whiteboard.uploadImage((state) => {
        if (state.code === 2) {
          this.imageUploading = true;
          this.imageUploadProgress = `(${(
            (state.uploadProgress.loaded / state.uploadProgress.total) *
            100
          ).toFixed(1)})%`;
        } else {
          this.imageUploading = false;
          this.imageUploadProgress = '';
          state.code !== 1 && this.$message.error('上传失败');
        }
      }, false);
    },
    uploadAudio() {
      this.whiteboard.uploadAudio((state) => {
        if (state.code === 2) {
          this.audioUploading = true;
          this.audioUploadProgress = `(${(
            (state.uploadProgress.loaded / state.uploadProgress.total) *
            100
          ).toFixed(1)})%`;
        } else {
          this.audioUploading = false;
          this.audioUploadProgress = '';
          state.code !== 1 && this.$message.error('上传失败');
        }
      });
    },
    uploadVideo() {
      this.whiteboard.uploadVideo((state) => {
        if (state.code === 2) {
          this.videoUploading = true;
          this.videoUploadProgress = `(${(
            (state.uploadProgress.loaded / state.uploadProgress.total) *
            100
          ).toFixed(1)}%)`;
        } else {
          this.videoUploading = false;
          this.videoUploadProgress = '';
          state.code !== 1 && this.$message.error('上传失败');
        }
      });
    },
    uploadLocalImage() {
      this.whiteboard.uploadImage((state) => {
        if (state.code === 1) {
          console.log('上传成功!');
        } else if (state.code === 2) {
          console.log(
            `已上传${
              (state.uploadProgress.loaded / state.uploadProgress.total) * 100
            }%`
          );
        } else {
          this.$message.error('上传失败');
        }
      });
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
    async snapshot(mode) {
      mode === 'all'
        ? (this.snapshotingAll = true)
        : (this.snapshotingView = true);
      await this.whiteboard.snapshot(true, mode);
      this.snapshotingAll = false;
      this.snapshotingView = false;
    },
    clearContents(curPage, type) {
      this.whiteboard.clearContents(curPage ? curPage : false, type);
    },
    clearMyContents(type) {
      this.whiteboard.clearUserContents(
        this.$store.getters.userMe.userId,
        false,
        type
      );
    },
  },
  mounted() {
    this.whiteboard.enableCursorSync();
    this.didSelectedColor(this.strokeStyle);
    this.setToolType(ToolType.Pen);
  },
};
</script>

<style lang="less" scoped>
.pano-wb-tb {
  border-radius: 5px;
  font-size: 12px;
  padding: 8px;
  position: absolute;
  z-index: 10;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  user-select: none;
  font-size: 12px;
  box-shadow: 2px 2px 8px rgba(20, 14, 14, 0.2);
  background-color: #fff;
  pointer-events: all;
  transition: all 0.3s ease-in-out;
  &--hidden {
    box-shadow: none;
    left: -45px;
  }
  .hide-hander {
    position: absolute;
    right: -21px;
    top: calc(50% - 22px);
    width: 20px;
    height: 25px;
    border-top-right-radius: 50%;
    border-bottom-right-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 2px 2px 8px rgba(20, 14, 14, 0.2);
    z-index: 11;
    background-color: #fff;
    cursor: pointer;
    opacity: 0;
    transition: all 0.3s ease-in-out;
    &--sticky {
      opacity: 1;
    }
  }
  &:hover {
    .hide-hander {
      opacity: 1;
    }
  }
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
    margin: 1px 0;
    color: #333;
    position: relative;
    > .iconfont {
      font-size: 18px;
    }
    &:hover {
      background-color: lightgray;
    }
    &--selected {
      // background-color: @deep-grey;
      color: #0899f9;
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

.toolbox-panel {
  width: 260px;
  background: #ffffff;
  border-radius: 2px;

  &__btn {
    position: relative;
    display: inline-flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    outline: none;
    border: none;
    width: 76px;
    height: 52px;
    margin-top: 4px;
    border-radius: 4px;
    background-color: transparent;
    color: #000;
    font-weight: 400;
    transition: all 0.3s ease;
    position: relative;
    padding: 8px 0;
    cursor: pointer;
    margin: 0 5px;
    &:hover {
      background-color: rgba(0, 0, 0, 0.1);
    }
    &__more-trigger {
      position: absolute;
      right: 4px;
      top: 3px;
      height: 22px;
      width: 20px;
      border-radius: 3px;
      text-align: center;
      background-color: transparent;
      padding: 0px;
      color: white;
      border: none;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
      .iconfont {
        font-size: 16px;
      }
      &:hover {
        background-color: #5b5b5b;
        color: white;
      }
    }
    &--active {
      color: #68d56a !important;
    }
    &__label {
      position: absolute;
      bottom: 3px;
      left: 0;
      width: 100%;
      text-align: center;
      font-size: 12px;
    }
    &__icon {
      position: absolute;
      top: -3px;
      left: 0;
      width: 100%;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px !important;
    }
  }
}

.toolbar-instruction {
  list-style: none;
  max-width: 350px;
  padding: 10px 10px;
  color: #555;
  font-size: 12px;
  &__item {
    display: flex;
    &-title {
      min-width: 60px;
    }
  }
  &__item ~ &__item {
    margin-top: 10px;
  }
}
</style>
