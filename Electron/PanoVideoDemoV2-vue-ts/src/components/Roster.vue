<template>
  <Modal
    class="roster-modal"
    :width="550"
    :title="`用户列表 (${allUsers.length})`"
    :visible="visible"
    closable
    @cancel="$emit('cancel')"
  >
    <a-input class="search" placeholder="搜索" v-model="keyWord">
      <a-icon type="search" slot="prefix" />
    </a-input>
    <div class="user-list-wrapper">
      <ul class="user-list">
        <li v-for="user in userList" :key="user.userId">
          <label>{{ (user.userName || user.userId).substring(0, 1) }}</label>
          <span>
            {{ user.userName || user.userId }}
            {{ user === userMe ? ' (me)' : '' }}
          </span>
          <em v-if="user === hostUser">主持人</em>
          <em
            class="set-host"
            v-else-if="userMe === hostUser"
            @click="setHost(user)"
          >
            设为主持人
          </em>
          <UserAudio
            :fontSize="24"
            :userId="user.userId"
            :audioMuted="user.audioMuted"
          />
          <i
            :class="[
              'iconfont',
              user.videoMuted ? 'icon-video-off' : 'icon-video',
            ]"
          ></i>
        </li>
      </ul>
    </div>
    <a-button
      slot="footer"
      type="primary"
      :disabled="applyHostDisabled"
      @click="applyHost"
    >
      成为主持人
    </a-button>
  </Modal>
</template>

<script>
import { Button, Modal, Icon, Input } from 'ant-design-vue';
import { mapGetters, mapState } from 'vuex';
import { setHostId } from '@/pano/panorts';
import UserAudio from '@/components/audio/UserAudio';

export default {
  components: {
    Modal,
    'a-button': Button,
    'a-icon': Icon,
    'a-input': Input,
    UserAudio,
  },
  props: {
    visible: Boolean,
  },
  data() {
    return {
      keyWord: '',
    };
  },
  computed: {
    ...mapState({
      userMe: (state) => state.userStore.userMe,
    }),
    ...mapGetters(['hostUser', 'allUsers']),
    applyHostDisabled() {
      return this.hostUser !== undefined;
    },
    userList() {
      let list = this.allUsers;
      if (this.keyWord) {
        const word = this.keyWord.toLowerCase();
        list = list.filter((user) =>
          (user.userName || user.userId).toLowerCase().includes(word)
        );
      }
      // 主持人自己之后
      if (
        list.length > 1 &&
        this.hostUser !== undefined &&
        this.hostUser !== this.userMe
      ) {
        let i = list.indexOf(this.hostUser);
        if (i !== -1) {
          const start = list[0] === this.userMe ? 1 : 0;
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
    applyHost() {
      // TODO: jammyxu 结果提示
      setHostId(this.userMe.userId);
    },
    setHost(user) {
      this.$Modal.confirm({
        title: '设置主持人',
        content: `确认将${user.userName || user.userId}设置为主持人？`,
        okText: '确定',
        cancelText: '取消',
        onOk: () => {
          setHostId(user.userId);
        },
      });
    },
  },
};
</script>

<style lang="less" scoped>
.roster-modal {
  /deep/ .ant-modal-header {
    padding: 10px 24px;
  }
  /deep/ .ant-modal-close-x {
    width: 42px;
    height: 42px;
    line-height: 42px;
  }
  /deep/ .ant-modal-title {
    font-size: 14px;
    line-height: 21px;
  }
  /deep/ .ant-modal-body {
    padding: 16px 0 16px 24px;
  }
  /deep/ .ant-modal-footer {
    padding-left: 24px;
    text-align: left;
  }
}
.search {
  display: block;
  width: unset;
  margin-right: 16px;
}
.user-list-wrapper {
  height: 336px;
  margin-top: 7px;
  overflow-y: auto;
}
.user-list {
  list-style: none;
  margin: 0;
  > li {
    margin-top: 7px;
    border-bottom: 1px solid #ddd;
    display: flex;
    padding: 8px 0;
    line-height: 28px;
    &:first-child {
      margin-top: 0;
    }
    > label {
      width: 28px;
      margin-right: 10px;
      text-align: center;
      background-color: #0899f9;
      border-radius: 50%;
      font-size: 16px;
      color: #fff;
    }
    > span {
      flex: 1;
      margin-right: 16px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    > em {
      font-style: normal;
      margin-right: 16px;
    }
    /deep/ .audio-level {
      margin-right: 16px;
      line-height: inherit;
      height: unset;
      > .icon-audio,
      > .icon-phone {
        color: inherit !important;
      }
      > .audio-level__indicator {
        background-color: unset !important;
      }
    }
    > i {
      margin-right: 16px;
      font-size: 24px;
    }
    > .icon-audio-off,
    > .icon-video-off {
      color: red;
    }
  }
}
.set-host {
  display: none;
  color: #0899f9;
  cursor: pointer;
}
li:hover > .set-host {
  display: block;
}
</style>
