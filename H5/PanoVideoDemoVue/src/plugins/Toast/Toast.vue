<template>
  <transition name="toast-show">
    <div class="toast-wrapper" v-show="display">
      <div>{{ message }}</div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'Toast',
  data() {
    return {
      display: false,
      classList: [],
      message: null,
    };
  },
  methods: {
    show(val) {
      this.message = val;
      this.display = true;
      this.$nextTick(() => {
        this.display = false;
      });
    },
  },
};
</script>

<style lang="less">
.toast-wrapper {
  position: fixed;
  left: 15%;
  top: 42%;
  width: 70%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.72);
  border-radius: 6px;
  padding: 20px 20px;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  color: #fff;
  opacity: 1;
  z-index: 3000;
}
.toast-show-leave-active {
  transition: opacity 3.6s ease-in;
}
.toast-show-leave-to {
  opacity: 0;
}
</style>
