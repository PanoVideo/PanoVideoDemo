<template>
  <div class="user-list-panel">
    <div class="user-list-header">
      <span @click="back"></span>
      <i></i>
      <h3>用户列表( {{ userList.length }} )</h3>
    </div>
    <div class="search-wrapper">
      <i></i>
      <input type="text" placeholder="搜索" v-model="keyWord" />
    </div>
    <div class="user-list-wrapper">
      <ul class="user-list">
        <li v-for="item in userList" :key="item.userId" class="user" @click="userClick(item)">
          <label>{{ item.userName.substring(0, 1) }}</label>
          <span
            >{{ item.userName || item.userId
            }}{{ item.userId === user.userId ? ' (Me)' : '' }}</span
          >
          <i>{{hostUser === item ? '主持人' : '' }}</i>
          <em v-if="isOpen(item.audioStatus)">
            <i>
              <i>
                <i
                  :style="{
                    height:
                      audioLevelMap[item.userId] !== undefined
                        ? `${audioLevelMap[item.userId] * 1000}%`
                        : '0',
                  }"
                ></i>
              </i>
            </i>
          </em>
          <em v-else class="closed"></em>
          <em :class="{ closed: !isOpen(item.videoStatus) }"></em>
        </li>
      </ul>
    </div>
    <div class="button-wrapper">
      <button @click="beHost" :disabled="hostUser !== undefined">成为主持人</button>
    </div>
    <ActionSheet v-model="userActionShow" :actions="userActionList" cancel-text="取消"
      @select="userActionSelect" />
    <Dialog ref="setHostConfirm" width="70%">
      <div class="dialog-content-text">确认将{{
        selectedUser ? selectedUser.userName || selectedUser.userId : ''
      }}设置为主持人？</div>
      <div class="dialog-btn-wrapper">
        <a class="cancel" href="javascript:;">取消</a>
        <a class="confirm" href="javascript:;" @click="setHost">确定</a>
      </div>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import Dialog from '@/components/Dialog/index.vue';
import { isOpen } from '@/utils';
import { setHostId } from '@/utils/sdk';
import ActionSheet from 'vant/lib/action-sheet';

export default {
  name: 'UserList',
  components: {
    ActionSheet,
    Dialog,
  },
  data() {
    return {
      keyWord: '',
      userActionShow: false,
      userActionList: [
        { name: '设置主持人' },
      ],
      selectedUser: undefined,
    };
  },
  computed: {
    ...mapState(['user', 'audioLevelMap']),
    ...mapGetters(['hostUser']),
    userList() {
      const { user, userIdList, userMap } = this.$store.state;
      let list = userIdList.map((userId) => userMap[userId]);
      if (user.userId) {
        list = [user, ...list];
      }
      if (this.keyWord) {
        const word = this.keyWord.toLowerCase();
        list = list.filter((item) => (item.userName ||
          item.userId).toLowerCase().includes(word));
      }
      // 主持人自己之后
      if (
        list.length > 1 &&
        this.hostUser !== undefined &&
        this.hostUser !== user
      ) {
        let i = list.indexOf(this.hostUser);
        if (i !== -1) {
          const start = list[0] === user ? 1 : 0;
          const temp = list[i];
          for (; i > start; i--) {
            list[i] = list[i - 1];
          }
          list[start] = temp;
        }
      }
      return list;
    },
  },
  methods: {
    isOpen,
    back() {
      this.$store.commit('closeUserList');
    },
    beHost() {
      setHostId(this.user.userId);
    },
    setHost() {
      setHostId(this.selectedUser.userId);
      this.$refs.setHostConfirm.close();
    },
    userClick(user) {
      if (this.hostUser !== this.user || this.user === user) {
        return;
      }
      this.userActionShow = true;
      this.selectedUser = user;
    },
    userActionSelect() {
      this.userActionShow = false;
      this.$refs.setHostConfirm.open();
    },
  },
};
</script>

<style lang="less" scoped>
@import url('../less/variables.less');

.user-list-panel {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background-color: #f5f7ff;
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
      content: '\e75b';
      font-family: 'pvc icon';
      font-size: 28px;
      line-height: 1;
    }
  }
}
.search-wrapper {
  margin-bottom: 10px;
  background-color: #fff;
  position: relative;
  > i {
    position: absolute;
    left: 10px;
    top: 12px;
    font-style: normal;
    color: #999;
    &::before {
      content: '\e847';
      font-family: 'pvc icon';
      font-size: 20px;
      line-height: 1;
    }
  }
  > input {
    position: relative;
    -webkit-appearance: none;
    display: block;
    width: 100%;
    height: 42px;
    padding: 0 16px 0 36px;
    border: 1px solid #ddd;
    border-width: 1px 0;
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
  > label {
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
  > i {
    font-style: normal;
  }
  > em {
    font-style: normal;
    margin-left: 5px;
    position: relative;
    font-size: 24px;
    line-height: 1;
    &::before,
    &::after {
      font-family: 'pvc icon';
      vertical-align: top;
      line-height: 1;
      color: #666;
    }
    &:nth-child(4)::before {
      content: '\e776';
    }
    &:nth-child(5)::before {
      content: '\e774';
    }
    &.closed::after {
      position: absolute;
      left: 0;
      top: 0;
      color: #d51c18;
      content: '\e7c4';
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
  }
}
.button-wrapper {
  padding: 16px 0 28px;
  text-align: center;
  > button {
    -webkit-appearance: none;
    width: 160px;
    height: 40px;
    font-size: 16px;
    background-color: @primary-color;
    border-radius: 4px;
    border: none;
    color: #fff;
    &:disabled {
      background-color: #ccc;
    }
  }
}
</style>
