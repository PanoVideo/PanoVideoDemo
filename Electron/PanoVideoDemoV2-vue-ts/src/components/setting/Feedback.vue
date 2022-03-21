<template>
  <div class="feedback-wrapper">
    <ul>
      <li class="fill-item">
        <label class="required">问题类型</label>
        <Select style="width: 300px" v-model="info.type">
          <SelectOption
            v-for="item in typeList"
            :key="item.value"
            :value="item.value"
          >
            {{ item.text }}
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
          v-model="info.description"
          @input="descriptionInput"
        />
        <span class="invalid-msg" v-if="descriptionInvalid">{{
          descriptionInvalid
        }}</span>
      </li>
      <li class="fill-item">
        <label>联系方式</label>
        <Input
          style="width: 300px"
          placeholder="请输入"
          v-model="info.contact"
        />
      </li>
      <li class="fill-item">
        <label>上传日志</label>
        <div class="switch-wrapper">
          <a-switch v-model="info.uploadLogs" />
        </div>
      </li>
    </ul>
    <div class="button-wrapper">
      <a-button @click="cancel">取消</a-button>
      <a-button type="primary" @click="submit">提交</a-button>
    </div>
  </div>
</template>

<script>
import { Input, Switch, Select, Button } from 'ant-design-vue';

export default {
  components: {
    Select,
    SelectOption: Select.Option,
    Input,
    TextArea: Input.TextArea,
    'a-switch': Switch,
    'a-button': Button,
  },
  data() {
    return {
      typeList: [
        { value: '0', text: '通用类型' },
        { value: '1', text: '语音问题' },
        { value: '2', text: '视频问题' },
        { value: '3', text: '白板问题' },
      ],
      info: {
        product: window.IS_ELECTRON ? 'PanoVideoCall' : 'webpvc',
        type: '0',
        description: '',
        contact: '',
        uploadLogs: true,
      },
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
    submit() {
      if (this.descriptionInvalid) {
        return;
      }
      if (this.info.description) {
        this.descriptionInvalid = '';
      } else {
        this.descriptionInvalid = '请输入问题描述';
        return;
      }
      window.rtcEngine.sendFeedback(this.info);
      // 白板也要保障
      window.rtcWhiteboard.sendFeedback(this.info);
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
.feedback-wrapper {
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
.switch-wrapper {
  padding-top: 5px;
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
