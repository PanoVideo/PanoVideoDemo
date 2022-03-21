<template>
  <div
    class="wrapper"
    ref="content"
    :style="style"
    @mousedown="onDragStart($event)"
  >
    <slot name="content"></slot>
  </div>
</template>

<script>
import { throttle } from 'lodash';

export default {
  props: {
    initPosition: Object,
    disabled: Boolean,
    dragWindow: Boolean,
  },
  data() {
    return {
      left: 0,
      right: 0,
      dragStartPosition: { x: 0, y: 0, left: 0, top: 0 },
      isDragging: false,
    };
  },
  components: {},
  computed: {
    style() {
      return this.disabled
        ? {}
        : {
            left: `${this.left}px`,
            top: `${this.top}px`,
            position: 'absolute',
            zIndex: 1000,
          };
    },
  },
  methods: {
    resetPostion() {
      const left = this.initPosition ? this.initPosition.left : 0;
      const top = this.initPosition ? this.initPosition.top : 0;
      this.left = left;
      this.top = top;
      this.isDragging = false;
    },
    onDragStart(e) {
      if (this.disabled) return;
      this.dragStartPosition = {
        x: e.clientX,
        y: e.clientY,
        left: this.left,
        top: this.top,
      };
      this.isDragging = true;
      document.addEventListener('mousemove', this.onDrag);
      document.addEventListener('mouseup', this.onDragStop);
    },
    onDrag: throttle(function (e) {
      if (!this.isDragging) return;
      const newLeft =
        this.dragStartPosition.left + e.clientX - this.dragStartPosition.x;
      const newTop =
        this.dragStartPosition.top + e.clientY - this.dragStartPosition.y;
      const domWidth = this.$refs.content.clientWidth;
      const domHeight = this.$refs.content.clientHeight;
      const windowWidth = document.body.clientWidth;
      const windowHeight = document.body.clientHeight;
      let left = 0;
      let top = 0;
      if (newLeft + domWidth > windowWidth) {
        left = windowWidth - domWidth;
      } else if (newLeft < 0) {
        left = 0;
      } else {
        left = newLeft;
      }
      if (newTop + domHeight > windowHeight) {
        top = windowHeight - domHeight;
      } else if (newTop < 0) {
        top = 0;
      } else {
        top = newTop;
      }
      this.left = left;
      this.top = top;
    }),
    onDragStop() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.onDragStop);
    },
    removeListeners() {
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.onDragStop);
    },
  },
  watch: {
    disabled() {
      if (!this.disabled) {
        this.removeListeners();
        this.resetPostion();
      }
    },
    initPosition() {
      this.resetPostion();
    },
  },
  mounted() {
    this.resetPostion();
  },
  beforeDestroy() {
    this.removeListeners();
  },
};
</script>

<style lang="less" scoped>
.wrapper {
}
</style>
