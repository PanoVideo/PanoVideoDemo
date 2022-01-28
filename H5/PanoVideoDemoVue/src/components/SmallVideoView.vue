<template>
  <div v-show="shouldShow" class="small-video-view"
  @touchstart.prevent="startMove" @transitionend="adjustPosEnd">
    <PvcVideo
      :videoTag="user.videoTag"
      :mirror="isMe"
      :shouldShow="shouldShow && !!user.videoTag"
      alias="smallVideo"
    />
    <div v-show="!user.videoTag" class="no-video"></div>
    <div class="user-bar">
      <span>{{ user.userName || user.userId }}</span>
      <em v-if="isMe">(Me)</em>
      <i v-if="isOpen(user.audioStatus)">
        <i>
          <i>
            <i :style="{
              height: audioLevelMap[user.userId] !== undefined
                ? `${audioLevelMap[user.userId] * 1000}%` : '0',
            }"></i>
          </i>
        </i>
      </i>
      <i v-else class="closed"></i>
      <strong :class="networkStatusMap[user.userId] || 'good'"></strong>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import { isOpen } from '@/utils';
import PvcVideo from './PvcVideo.vue';

export default {
  components: {
    PvcVideo,
  },
  props: {
    user: Object,
    isMe: {
      type: Boolean,
      default: false,
    },
    edgeDistance: {
      type: Array,
      default() {
        return [5, 5, 5, 5];
      },
    },
    shouldShow: Boolean,
  },
  computed: mapState(['audioLevelMap', 'networkStatusMap']),
  mounted() {
    document.addEventListener('touchmove', this.move, { passive: false });
    document.addEventListener('touchend', this.endMove);
    document.addEventListener('touchcancel', this.endMove);
  },
  destroyed() {
    document.removeEventListener('touchmove', this.move);
    document.removeEventListener('touchend', this.endMove);
    document.removeEventListener('touchcancel', this.endMove);
  },
  methods: {
    isOpen,
    startMove(e) {
      if (e.touches.length !== 1) {
        return;
      }
      const touch = e.touches[0];
      this.moving = true;
      const elem = e.currentTarget;
      this.elemStartLeft = elem.offsetLeft;
      this.elemStartTop = elem.offsetTop;
      this.moveStartX = touch.pageX;
      this.moveStartY = touch.pageY;
    },
    move(e) {
      if (!this.moving) {
        return;
      }
      e.preventDefault();
      const touch = e.touches[0];
      this.$el.style.left = `${this.elemStartLeft + touch.pageX - this.moveStartX}px`;
      this.$el.style.top = `${this.elemStartTop + touch.pageY - this.moveStartY}px`;
    },
    endMove() {
      if (!this.moving) {
        return;
      }
      this.moving = false;
      let left = this.$el.style.left;
      if (left) {
        left = parseFloat(left);
      } else {
        left = this.elemStartLeft;
      }
      let top = this.$el.style.top;
      if (top) {
        top = parseFloat(top);
      } else {
        top = this.elemStartTop;
      }
      let endLeft;
      const { edgeDistance } = this;
      if (left < edgeDistance[3]) {
        endLeft = `${edgeDistance[3]}px`;
      } else {
        const maxLeft = this.$el.offsetParent.offsetWidth - this.$el.offsetWidth - edgeDistance[1];
        if (left > maxLeft) {
          endLeft = `${maxLeft}px`;
        }
      }
      let endTop;
      if (top < edgeDistance[0]) {
        endTop = `${edgeDistance[0]}px`;
      } else {
        const maxTop = this.$el.offsetParent.offsetHeight - this.$el.offsetHeight - edgeDistance[2];
        if (top > maxTop) {
          endTop = `${maxTop}px`;
        }
      }
      if (endLeft || endTop) {
        this.$el.classList.add('adjust-pos');
      }
      if (endLeft) {
        this.$el.style.left = endLeft;
      }
      if (endTop) {
        this.$el.style.top = endTop;
      }
    },
    adjustPosEnd() {
      this.$el.classList.remove('adjust-pos');
    },
  },
};
</script>

<style lang="less" scoped>
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
    > i {
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      > i {
        width: 0.25em;
        height: 54 / 128 * 1em;
        border-radius: 0.125em;
        margin-bottom: 26 / 128 * 1em;
        position: relative;
        overflow: hidden;
        > i {
          position: absolute;
          left: 0;
          right: 0;
          bottom: 0;
          background-color: #0f0;
        }
      }
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: "\e7c4";
    }
  }
  > strong {
    font-weight: normal;
    position: relative;
    &::before,
    &::after {
      font-family: "pvc icon";
      vertical-align: top;
      line-height: inherit;
    }
    &::before {
      content: "\e81d";
    }
    &::after {
      position: absolute;
      left: 0;
      top: 0;
    }
    &.good::after {
      content: "\e81e";
      color: rgb(52, 199, 88);
    }
    &.poor::after {
      content: "\e820";
      color: rgb(255, 163, 16);;
    }
    &.bad::after {
      content: "\e821";
      color: rgb(247, 67, 64);;
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
  top: 20px;
  width: 100px;
  height: 134px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 3px;
  background-color: #333;
  overflow: hidden;
  color: #fff;
  &.adjust-pos {
    transition: all 0.15s ease-out;
  }
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
</style>
