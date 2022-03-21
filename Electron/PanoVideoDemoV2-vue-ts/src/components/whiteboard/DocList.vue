<template>
  <div class="wb-docs">
    <div class="wb-docs__current">
      <a-select
        ref="docsSelectRef"
        :value="activeDocId"
        style="width: 200px"
        @change="handleSelectChange"
      >
        <a-select-option
          v-for="(doc, index) in docs"
          :key="doc.docId"
          :value="doc.docId"
        >
          <div class="wb-doc">
            <span class="wb-doc__name">{{
              index === 0 ? '默认白板' : doc.name
            }}</span>
            <span
              v-if="index > 0"
              class="iconfont icon-delete_doc doc-delete"
              :style="{
                fontSize: '14px',
                color: '#afafaf',
                fontWeight: 'normal',
              }"
              @click="deleteDoc(doc.docId)"
            >
            </span>
          </div>
        </a-select-option>
      </a-select>
    </div>
    <a-popover
      placement="bottom"
      trigger="click"
      :visible="uploadPopoverVisible"
    >
      <template slot="content">
        <div class="upload-doc-popover" v-clickoutside="hiddePopover">
          <div class="upload-doc-popover__item">
            <div class="upload-doc-popover__item-title">静态课件</div>
            <div class="upload-doc-popover__item-content">
              将文档转为图片，支持 .docx, .xlsx, .pptx, .doc, .xls, .ppt, .pdf
              等格式
            </div>
            <div class="upload-doc-popover__item-button">
              <a-button
                type="primary"
                size="small"
                @click="uploadFileAndTranscode('doc')"
              >
                上传文档
              </a-button>
            </div>
          </div>
          <div class="upload-doc-popover__item">
            <div class="upload-doc-popover__item-title">高清静态课件</div>
            <div class="upload-doc-popover__item-content">
              将文档转为PDF，支持.docx, .xlsx, .pptx, .doc, .xls, .ppt, .pdf
              等格式
            </div>
            <div class="upload-doc-popover__item-button">
              <a-button
                type="primary"
                size="small"
                @click="uploadFileAndTranscode('pdf')"
              >
                上传文档
              </a-button>
            </div>
          </div>
          <div class="upload-doc-popover__item">
            <div class="upload-doc-popover__item-title">动态课件</div>
            <div class="upload-doc-popover__item-content">
              将 PPT 课件转为在线资源，支持动态交互，支持.pptx、.ppt格式
            </div>
            <div class="upload-doc-popover__item-button">
              <a-button
                type="primary"
                size="small"
                @click="uploadFileAndTranscode('courseware')"
              >
                上传课件
              </a-button>
            </div>
          </div>
        </div>
      </template>
      <a-button
        :style="{ marginLeft: '10px' }"
        @click.stop="uploadPopoverVisible = !uploadPopoverVisible"
      >
        <span className="iconfont icon-doc-plus" :style="{ fontSize: '14px' }">
          &nbsp;&nbsp;添加文档
        </span>
      </a-button>
    </a-popover>
  </div>
</template>

<script>
import { Popover, Button, Select } from 'ant-design-vue';
import { RtcWhiteboard } from '@pano.video/panorts';
import { mapGetters } from 'vuex';

export default {
  name: 'ToolbarDoc',
  props: {
    whiteboard: RtcWhiteboard,
    activeDocId: String,
  },
  data() {
    return {
      docs: [],
      activePPT: '303964793939697664',
      uploadPopoverVisible: false,
    };
  },
  components: {
    'a-select': Select,
    'a-select-option': Select.Option,
    'a-popover': Popover,
    'a-button': Button,
  },
  computed: {
    ...mapGetters([
      'enableHdStaticTranscode',
      'enableDynamicTranscode',
      'isWhiteboardOpen',
    ]),
  },
  methods: {
    beforeDestroy() {
      this.removeWbListener();
    },
    addWbListener() {
      this.whiteboard.on(RtcWhiteboard.Events.docCreated, this.onDocChanged);
      this.whiteboard.on(RtcWhiteboard.Events.docDeleted, this.onDocChanged);
      this.whiteboard.on(RtcWhiteboard.Events.docUpdated, this.onDocChanged);
    },
    removeWbListener() {
      this.whiteboard.off(RtcWhiteboard.Events.docCreated, this.onDocChanged);
      this.whiteboard.off(RtcWhiteboard.Events.docDeleted, this.onDocChanged);
      this.whiteboard.off(RtcWhiteboard.Events.docUpdated, this.onDocChanged);
    },
    onDocChanged() {
      this.docs = this.whiteboard.enumerateDocs();
    },
    handleSelectChange(value) {
      this.whiteboard.switchToDoc(value);
    },
    deleteDoc(docId) {
      if (docId === this.whiteboard.activeDocId) {
        this.$refs.docsSelectRef.blur();
      }
      this.whiteboard.deleteDoc(docId);
    },
    uploadFileAndTranscode(type) {
      this.uploadPopoverVisible = false;
      this.whiteboard.uploadDoc(
        this.onUploadStateChange,
        type,
        type === 'courseware'
      );
    },
    onUploadStateChange(state) {
      const key = state.docId;
      const { fileName } = state;
      switch (state.code) {
        case 1:
          this.$message.loading({
            content: `${fileName} 上传成功，正在转码...`,
            key,
            duration: 0,
          });
          break;
        case 2:
          break;
        case 3:
          this.$message.success({
            content: `${fileName} 转码成功`,
            key,
            duration: 3,
          });
          break;
        case 4:
          this.$message.loading({
            content: `${fileName} 正在上传，${(
              (state.uploadProgress.loaded / state.uploadProgress.total) *
              100
            ).toFixed(2)}%`,
            key,
            duration: 0,
          });
          break;
        case 5:
          this.$message.success({
            content: `${fileName} 动态课件转码预览已完成`,
            key,
            duration: 3,
          });
          break;
        case -1:
          this.$message.error({
            content: `${fileName} 文件过大上传失败, 静态课件最大${RtcWhiteboard.docTranscodeStaicMaxSize}M，动态课件最大${RtcWhiteboard.docTranscodeCoursewareMaxSize}M`,
            key,
            duration: 3,
          });
          break;
        case -2:
          this.$message.error({
            content: `${fileName} 上传失败`,
            key,
            duration: 3,
          });
          break;
        case -3:
          this.$message.error({
            content: `${fileName} 在转码中遇到错误，请联系管理员`,
            key,
            duration: 3,
          });
          break;
      }
    },
    loadDoc() {
      this.uploadPopoverVisible = false;
      this.whiteboard.loadDoc(this.activePPT).catch(() => {
        this.$message.error('文档载入失败!');
      });
    },
    resetWhiteboard() {
      this.whiteboard.center();
    },
    hiddePopover() {
      this.uploadPopoverVisible = false;
    },
  },
  mounted() {
    this.addWbListener();
    this.onDocChanged();
  },
  beforeDestroy() {
    this.removeWbListener();
  },
};
</script>

<style lang="less" scoped>
.wb-doc {
  width: 100%;
  display: flex;
  align-items: center;
  &__name {
    width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.upload-doc-popover__item {
  /deep/ .ant-form-item {
    margin-bottom: 0 !important;
  }
}
.wb-docs {
  pointer-events: all;
  position: absolute;
  top: 17px;
  left: 0;
  padding-left: 40px;
  display: flex;
  flex-wrap: wrap;
  z-index: 10;
  font-size: 12px;
  align-items: center;
  user-select: none;
  max-width: calc(100% - 180px);
  color: #333;
  &__item {
    display: flex;
    align-items: center;
    padding: 5px 16px;
    border-radius: 4px;
    box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
    cursor: pointer;
    margin-right: 8px;
    margin-bottom: 10px;
    background-color: #fff;
    min-height: 26px;
    flex-shrink: 0;
    position: relative;
    .icon-bin {
      position: absolute;
      visibility: visible;
      position: relative;
      right: -4px;
    }
  }
  &__item--active {
    color: #1890ff;
  }
  &__add-doc-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background-color: #fff;
    padding: 4px 10px;
    span {
      color: #555;
    }
  }
  &__current {
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
  }

  &__tool {
    margin-left: 8px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 3px 8px;
    background-color: #fff;
    border-radius: 2px;
    border: 1px solid rgb(217, 217, 217);
    box-shadow: 0 0 2px rgb(217, 217, 217);
    .iconfont {
      font-size: 18px;
      cursor: pointer;
      min-width: 26px;
      height: 26px;
      border-radius: 5px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin: 0 3px;
      &:hover {
        background-color: #f0f0f0;
      }
    }
  }
}

.upload-doc-popover {
  margin: 0;
  width: 325px;
  &__item {
    position: relative;
    padding: 4px 8px;
    transition: all 0.3s ease-in-out;
    border-radius: 5px;
    cursor: pointer;
    // &:hover {
    //    background-color: #f5f5f5;
    // }
    &-title {
      font-size: 14px;
      font-weight: bold;
      color: #333;
      margin-bottom: 5px;
    }
    &-content {
      font-size: 14px;
      color: #777;
      margin-bottom: 5px;
    }
    &-button {
      margin-bottom: 5px;
      height: 32px;
      position: relative;
      button {
        position: absolute;
        right: 0;
        top: 0;
      }
    }
  }
  &__item ~ &__item {
    border-top: 1px solid #eee;
  }

  /deep/ .ant-form-item {
    margin-bottom: 20px;
  }
}
</style>
