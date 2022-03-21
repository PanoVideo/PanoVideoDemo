<template>
  <transition name="fade">
    <div class="leave-mask" v-show="visible">
      <transition name="leave-confirm">
        <div class="leave-confirm" v-show="visible">
          <button class="leave-confirm__btn" @click="onLeave">离开会议</button>
          <button class="leave-confirm__cancle-btn" @click="onCancel">
            取消
          </button>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script>
import * as mutations from '@/store/mutations';

export default {
  props: {
    visible: {
      type: Boolean,
      required: true,
    },
  },
  methods: {
    onLeave() {
      this.$emit('confirm');
      this.onCancel();
    },
    onCancel() {
      this.$store.commit(mutations.SET_CLOSE_CONFIRM_VISIBLE, false);
    },
  },
};
</script>

<style lang="less" scoped>
.leave-mask {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  bottom: 0;
  z-index: 99;
  background: rgba(0, 0, 0, 0.45);
}
.leave-confirm {
  position: fixed;
  left: calc(50% - 110px);
  top: calc(40% - 100px);
  background-color: #45454a;
  z-index: 100;
  width: 220px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 10px 10px;

  &__btn,
  &__cancle-btn {
    height: 32px;
    width: 100%;
    border: none;
    outline: none;
    font-size: 14px;
    border-radius: 5px;
    cursor: pointer;
  }

  &__btn {
    background-color: #e4242b;
    color: #fff;
    &:hover {
      background-color: #cc3b34;
    }
  }

  &__cancle-btn {
    color: #c5c4c5;
    background-color: #59595b;
    &:hover {
      background-color: #535355;
    }
  }

  button ~ button {
    margin-top: 10px;
  }
}

.fade-enter,
.fade-leave-to {
  background: rgba(0, 0, 0, 0);
}

.fade-enter-to,
.fade-leave {
  background: rgba(0, 0, 0, 0.45);
}

.fade-leave-active,
.fade-enter-active {
  transition: all 0.3s ease-in-out;
}

.leave-confirm-enter,
.leave-confirm-leave-to {
  transform: scale(0.7);
  opacity: 0;
}

.leave-confirm-enter-active,
.leave-confirm-leave-active {
  transition: all 0.3s ease-in-out;
}

.leave-confirm-enter-to .leave-confirm-leave {
  transform: scale(1);
}
</style>
