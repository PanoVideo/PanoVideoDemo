<template>
  <div class="audio-fault-wrapper">
    <ul>
      <li class="fill-item">
        <label>选择用户</label>
        <Select
          style="width: 300px"
          mode="multiple"
          v-model="selectedUsers"
          placeholder="选择同步开启dump的用户，最多5个"
        >
          <SelectOption
            v-for="user of userList"
            :value="user.userId"
            :key="user.userId"
            :disabled="
              !selectedUsers.includes(user.userId) && selectedUsers.length >= 5
            "
          >
            {{ user.userName }}
          </SelectOption>
        </Select>
      </li>
      <li class="fill-item">
        <label class="required">问题描述</label>
        <TextArea
          style="width: 300px"
          :autoSize="false"
          allowClear
          placeholder="请输入"
          :maxLength="256"
          rows="5"
          v-model="description"
          @input="descriptionInput"
        />
        <span class="invalid-msg" v-if="descriptionInvalid">{{
          descriptionInvalid
        }}</span>
      </li>
    </ul>
    <div class="button-wrapper">
      <a-button @click="cancel">取消</a-button>
      <a-button type="primary" @click="toggleDump">开始 dump</a-button>
    </div>
  </div>
</template>

<script>
import { Input, Select, Button } from 'ant-design-vue';
import { mapGetters } from 'vuex';
import { startAudioDump } from '@/pano/index';
import { RtcMessage } from '@pano.video/panorts';

export default {
  components: {
    Select,
    SelectOption: Select.Option,
    TextArea: Input.TextArea,
    'a-button': Button,
  },
  computed: {
    ...mapGetters(['userMe', 'userList']),
  },
  data() {
    return {
      selectedUsers: [],
      description: '',
      descriptionInvalid: '',
    };
  },
  methods: {
    descriptionInput(e) {
      if (e.target.value) {
        this.descriptionInvalid = '';
      } else {
        this.descriptionInvalid = '请输入问题描述';
      }
    },
    toggleDump() {
      if (this.descriptionInvalid) {
        return;
      }
      if (this.description) {
        this.descriptionInvalid = '';
      } else {
        this.descriptionInvalid = '请输入问题描述';
        return;
      }
      const msg = `'${this.userMe.userName || this.userMe.userId}'一键上报: ${
        this.description
      }`;
      this.selectedUsers.forEach((id) => {
        RtcMessage.getInstance().sendMessage(id, {
          type: 'command',
          description: msg,
          command: 'startDump',
        });
      });
      startAudioDump(msg, this.userMe.userName);
      this.$message.info('所选用户将会同时开始 dump 1分钟，完成后将自动上报');
      this.$emit('close');
    },
    cancel() {
      this.$emit('close');
    },
  },
};
</script>

<style lang="less" scoped>
ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.audio-fault-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  > ul {
    flex: 1;
    padding-top: 16px;
  }
}
.fill-item {
  display: flex;
  margin-bottom: 10px;
  > label {
    width: 140px;
    padding-right: 9px;
    line-height: 32px;
    text-align: right;
    &.required::before {
      content: '*';
      color: #f74340;
      font-size: 22px;
      line-height: 1;
      display: inline-block;
      vertical-align: top;
      margin: 8px 2px 0 0;
    }
    &::after {
      content: ':';
    }
  }
  /deep/ textarea {
    resize: none;
  }
  > .invalid-msg {
    flex: 1;
    flex-shrink: 0;
    padding: 0 8px;
    font-size: 13px;
    color: red;
  }
}
.button-wrapper {
  border-top: 1px solid #ddd;
  text-align: right;
  padding: 10px 16px 10px 0;
  button {
    margin-left: 10px;
  }
}
</style>
