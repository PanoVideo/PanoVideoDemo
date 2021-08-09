<template>
  <div class="user-list-panel">
    <div class="user-list-header">
      <span @click="back"></span>
      <h3>用户列表( {{ userList.length + (user.userId ? 1 : 0) }} )</h3>
    </div>
    <div class="user-list-wrapper">
      <ul class="user-list">
        <li v-if="user.userId" class="user me">
          <i>{{ user.userName.substring(0, 1) }}</i>
          <span>{{ user.userName }}</span>
          <em :class="{closed: !isOpen(user.audioStatus)}"></em>
          <em :class="{closed: !isOpen(user.videoStatus)}"></em>
        </li>
        <li v-for="user in userList" :key="user.userId" class="user">
          <i>{{ user.userName.substring(0, 1) }}</i>
          <span>{{ user.userName }}</span>
          <em :class="{closed: !isOpen(user.audioStatus)}"></em>
          <em :class="{closed: !isOpen(user.videoStatus)}"></em>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';

const openStatusList = ['open', 'unmute'];

export default {
  name: 'UserList',
  computed: mapState(['user', 'userList']),
  methods: {
    isOpen(status) {
      return openStatusList.includes(status);
    },
    back() {
      this.$store.commit('closeUserList');
    },
  },
};
</script>

<style lang="less" scoped>
@import url("../less/variables.less");

.user-list-panel {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: #F5F7FF;
  z-index: 99;
  display: flex;
  flex-direction: column;
}
.user-list-header {
  background-color: #fff;
  margin-bottom: 10px;
  @header-heigt: 52px;
  position: relative;
  > h3 {
    margin: 0;
    text-align: center;
    font-size: 18px;
    font-weight: 500;
    line-height: @header-heigt;
  }
  > span {
    position: absolute;
    left: 0;
    top: 0;
    width: @header-heigt;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    &::after {
      content: "\e75b";
      font-family: "pvc icon";
      font-size: 28px;
      line-height: 1;
    }
  }
}
.user-list-wrapper {
  flex: 1;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.user-list {
  background-color: #fff;
  margin: 0;
  padding: 0 0 0 15px;
}
.user {
  padding: 0 15px 0 0;
  border-bottom: 1px solid #ddd;
  display: flex;
  align-items: center;
  > i {
    width: 26px;
    border-radius: 50%;
    background-color: @primary-color;
    margin-right: 10px;
    font-style: normal;
    line-height: 26px;
    color: #fff;
    text-align: center;
  }
  > span {
    flex: 1;
    line-height: 40px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  &.me > span {
    color: @primary-color;
  }
  > em {
    font-style: normal;
    margin-left: 5px;
    position: relative;
    &::before,
    &::after {
      font-family: "pvc icon";
      vertical-align: top;
      font-size: 24px;
      line-height: 1;
      color: #666;
    }
    &:nth-child(3)::before {
      content: "\e776";
    }
    &:nth-child(4)::before {
      content: "\e774";
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: "\e7c4";
    }
  }
}
</style>
