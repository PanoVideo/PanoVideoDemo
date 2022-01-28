<template>
<Dialog ref="feedbackDialog" width="80%">
  <h3>反馈与报障</h3>
  <ul class="field-wrapper">
    <li class="field-block">
      <label>问题描述</label>
      <div><textarea rows="4" placeholder="请输入(必填)，长度不超过200"
        v-model="feedbackInfo.description" maxlength="200" /></div>
    </li>
    <li class="field-block">
      <label>联系人/联系方式</label>
      <div><input placeholder="请输入"  v-model="feedbackInfo.contact" maxlength="100"/></div>
    </li>
    <li class="field-line">
      <label>上传日志</label>
      <div><input type="checkbox" v-model="feedbackInfo.uploadLogs" /></div>
    </li>
  </ul>
  <div class="dialog-btn-wrapper">
    <a class="cancel" href="javascript:;">取消</a>
    <a class="confirm" href="javascript:;" @click="feedback">提交</a>
  </div>
</Dialog>
</template>

<script>
import Dialog from './Dialog/index.vue';

export default {
  name: 'Feedback',
  components: {
    Dialog,
  },
  data() {
    return {
      feedbackInfo: {
        description: '',
        contact: '',
        uploadLogs: true,
      },
    };
  },
  methods: {
    open() {
      this.$refs.feedbackDialog.open();
    },
    feedback() {
      if (!this.feedbackInfo.description) {
        this.$toast('请输入问题描述');
        return;
      }
      window.panoSDK.sendFeedback({
        type: 0,
        product: 'pvch5',
        ...this.feedbackInfo,
        extraInfo: '',
      });
      this.$refs.feedbackDialog.close();
    },
  },
};
</script>

<style lang="less" scoped>
.field-wrapper {
  padding: 15px;
}
.field-block {
  margin-bottom: 15px;
  > label {
    display: block;
    margin-bottom: 5px;
  }
  > div {
    > textarea {
      resize: none;
      display: block;
      width: 100%;
      outline: none;
      padding: 7px 6px;
      border: 1px solid #dcdcdc;
      border-radius: 3px;
      font-size: inherit;
      line-height: 20px;
      color: inherit;
    }
    > input:not([type="checkbox"]) {
      display: block;
      width: 100%;
      height: 37px;
      padding: 0 6px;
      border: 1px solid #dcdcdc;
      border-radius: 3px;
    }
  }
}
.field-line {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  > label {
    flex: 1;
  }
}
</style>
