<template>
  <div v-show="show" class="dialog">
    <i></i>
    <div class="dialog-content" :style="{ width: width }" @click="wrapperClick">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dialog',
  props: {
    width: {
      type: String,
      default: '90%',
    },
  },
  data() {
    return {
      show: false,
    };
  },
  methods: {
    open() {
      this.show = true;
    },
    close() {
      this.show = false;
    },
    wrapperClick(e) {
      let elem = e.target;
      while (elem !== e.currentTarget) {
        if (
          elem.classList.contains('close') ||
          elem.classList.contains('cancel')
        ) {
          this.close();
          break;
        }
        elem = elem.parentNode;
      }
    },
  },
};
</script>

<style lang="less">
@import url("../../less/variables.less");

.dialog {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 999;
  > i {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    background-color: rgba(37, 38, 45, 0.4);
  }
}
.dialog-content {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate3d(-50%, -50%, 0);
  background-color: #fff;
  border-radius: 10px;
  > h3 {
    margin: 0;
    font-size: 16px;
    padding: 20px 20px 0;
    text-align: center;
  }
}
.dialog-content-text {
  font-size: 14px;
  padding: 20px 15px;
  text-align: center;
}
.dialog-btn-wrapper {
  display: flex;
  border-top: 1px solid #dcdcdc;
  font-size: 16px;
  line-height: 44px;
  text-align: center;
  > a {
    flex: 1;
    &:not(:first-child) {
      border-left: 1px solid #dcdcdc;
    }
    &.cancel {
      color: #999;
    }
    &.confirm {
      color: @primary-color;
    }
  }
}
</style>
