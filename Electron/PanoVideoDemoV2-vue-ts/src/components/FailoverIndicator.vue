<template>
  <div class="pvc-loading">
    <img class="loading-img" :src="loading" />

    <img alt="loading" :src="logo" class="logo" />
    <div class="pvc-loading__text">会议断开，正在重连...</div>
  </div>
</template>

<script>
import logo from '@/assets/img/logo-white.png';
import loading from '@/assets/img/loading.svg';

export default {
  props: {
    userId: String,
    audioMuted: Boolean,
  },
  data() {
    return {
      logo,
      loading,
    };
  },
};
</script>

<style lang="less" scoped>
.pvc-loading {
  position: fixed;
  width: 300px;
  height: 280px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #524f4f;
  border-radius: 10px;
  z-index: 100;
  .loading-img {
    width: 250px;
    height: 250px;
    margin: 0 auto;
    display: block;
  }
  .logo {
    position: absolute;
    top: calc(50% - 15px);
    left: 50%;
    transform: translate(-50%, -50%);
    width: 40px;
    height: 55px;
  }
  &__text {
    text-align: center;
    font-size: 14px;
    font-weight: bold;
    color: #fff;
    margin-top: -10px;
  }
}
</style>
