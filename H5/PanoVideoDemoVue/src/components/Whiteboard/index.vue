<template>
  <div class="wb-panel">
    <div class="wb-wrapper" ref="whiteboard"></div>
    <SmallVideoView
      :shouldShow="userIdList.length > 0"
      :user="activeUser || user" :isMe="activeUser ? false : true"
      :edgeDistance="[45, 5, 75, 5]" />
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
          <li><label>当前白板：</label><div>{{ rtcWhiteboard.activeDoc.name }}</div></li>
          <li>
            <label>当前演示人：</label>
            <div>{{ hostUser ? (hostUser.userName || hostUser.userId) : '' }}</div>
          </li>
        </ul>
        <template v-if="user.userId === hostUser.userId">
          <ul class="page-nav">
            <li @click="prevPage">前一页</li>
            <li @click="nextPage">后一页</li>
            <li @click="addPage">增加页</li>
            <li @click="removePage">删除当前页</li>
          </ul>
          <ul class="doc-list">
            <li v-for="doc in rtcWhiteboard.enumerateDocs()" :key="doc.docId"
              :class="{ active: doc.docId === rtcWhiteboard.activeDoc.docId }"
              @click="switchDoc(doc.docId)">{{ doc.name }}</li>
          </ul>
        </template>
      </div>
    </div>
    <WbToolbar :rtcWhiteboard="rtcWhiteboard" />
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import SmallVideoView from '../SmallVideoView.vue';
import WbToolbar from './Toolbar.vue';

export default {
  name: 'Whiteboard',
  components: {
    SmallVideoView,
    WbToolbar,
  },
  data() {
    return {
      pageVisible: false,
      rtcWhiteboard: null,
    };
  },
  computed: {
    ...mapState(['user', 'userIdList']),
    ...mapGetters(['activeUser', 'hostUser']),
  },
  created() {
    this.rtcWhiteboard = window.panoSDK.getWhiteboard();
  },
  mounted() {
    this.openWhiteboard();
    window.addEventListener('resize', this.openWhiteboard);
  },
  destroyed() {
    window.removeEventListener('resize', this.openWhiteboard);
  },
  methods: {
    openWhiteboard() {
      const elem = this.$refs.whiteboard;
      if (window.innerWidth / window.innerHeight >= 16 / 9) {
        elem.style.height = '100%';
        elem.style.width = `${(16 / 9) * 100}vh`;
      } else {
        elem.style.width = '100%';
        elem.style.height = `${(9 / 16) * 100}vw`;
      }
      this.rtcWhiteboard.initVision(1600, 900, true);
      this.rtcWhiteboard.open(elem);
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
    switchDoc(docId) {
      if (docId !== this.rtcWhiteboard.activeDoc.docId) {
        this.rtcWhiteboard.switchToDoc(docId);
      }
    },
    undo() {
      this.rtcWhiteboard.undo();
    },
    redo() {
      this.rtcWhiteboard.redo();
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
  bottom: constant(safe-area-inset-bottom);
  bottom: env(safe-area-inset-bottom);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
  &:after {
    content: "";
    position: absolute;
    left: 0;
    right: 0;
    top: 100%;
    height: 0;
    height: constant(safe-area-inset-bottom);
    height: env(safe-area-inset-bottom);
    background-color: #fff;
  }
}
.wb-wrapper {
  background-color: #fff;
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
  }
}
.page-nav > li {
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
.doc-list {
  background-color: #fff;
  border-radius: 0 0 2px 2px;
  line-height: 24px;
  > li {
    padding: 0 8px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    &::before {
      display: inline-block;
      content: "";
      width: 22px;
      font-family: "pvc icon";
      line-height: inherit;
      font-size: 20px;
      vertical-align: top;
      color: @primary-color;
    }
    &.active::before {
      content: "\e78f";
    }
  }
}
.small-video-view {
  top: 60px;
  z-index: 50;
}
</style>
