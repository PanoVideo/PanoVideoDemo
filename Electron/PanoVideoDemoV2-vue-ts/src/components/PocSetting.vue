<template>
  <Modal
    :visible="visible"
    :footer="null"
    @cancel="$emit('cancel')"
    class="pocSetting"
    title="私有化配置"
  >
    <RadioGroup v-model="pocMode" style="margin-bottom: 8px">
      <RadioButton value="fast">快速设置</RadioButton>
      <RadioButton value="custom">自定义设置</RadioButton>
    </RadioGroup>
    <Form
      v-if="pocMode === 'fast'"
      layout="vertical"
      class="tableListForm"
      @submit="fastSet"
      :form="fastForm"
    >
      <FormItem name="config" label="Demo App Config" class="fast-config">
        <Input
          v-decorator="[
            'config',
            {
              initialValue: config,
              rules: [
                {
                  required: true,
                  message: '请输入 config',
                },
              ],
            },
          ]"
          class="roundBorder"
          placeholder="请通过 ./pano show demoappconfigs 命令查看"
        />
      </FormItem>
      <FormItem style="margin-top: 15px; text-align: center">
        <Button
          shape="round"
          type="primary"
          htmlType="submit"
          style="width: 60%"
        >
          确认
        </Button>
      </FormItem>
    </Form>
    <Form
      v-else
      layout="vertical"
      :initialValues="{ appId, serverAddr, appSecret }"
      @submit="detailSet"
      class="tableListForm"
      :form="detailForm"
    >
      <FormItem name="appId" label="App ID">
        <Input
          v-decorator="[
            'appId',
            {
              initialValue: appId,
              rules: [{ required: true, message: '请输入 App ID' }],
            },
          ]"
          class="roundBorder"
          placeholder="请通过 ./pano show appinfos 命令查看"
        />
      </FormItem>
      <FormItem name="appSecret" label="App Secret">
        <Input
          v-decorator="[
            'appSecret',
            {
              initialValue: appSecret,
              rules: [{ required: true, message: '请输入 App Secret' }],
            },
          ]"
          class="roundBorder"
          placeholder="请通过 ./pano show appinfos 命令查看"
        />
      </FormItem>
      <FormItem name="serverAddr" label="Api Server">
        <Input
          v-decorator="[
            'serverAddr',
            {
              initialValue: serverAddr,
              rules: [{ required: true, message: '请输入Api Server' }],
            },
          ]"
          class="roundBorder"
          placeholder="请通过 ./pano show apiserver 命令查看"
        />
      </FormItem>
      <FormItem style="margin-top: 15px; text-align: center">
        <Button
          shape="round"
          type="primary"
          htmlType="submit"
          style="width: 60%"
        >
          确认
        </Button>
      </FormItem>
    </Form>
  </Modal>
</template>

<script>
import { Modal, Radio, Form, Button, Input, message } from 'ant-design-vue';

const POC_CONFIG = 'POC_CONFIG';
const POC_APPID = 'POC_APPID';
const POC_SECRET = 'POC_SECRET';
const POC_SERVER = 'POC_SERVER';

export default {
  data() {
    return {
      pocMode: 'fast',
      config: localStorage.getItem(POC_CONFIG),
      appId: localStorage.getItem(POC_APPID),
      serverAddr: localStorage.getItem(POC_SERVER),
      appSecret: localStorage.getItem(POC_SECRET),
      fastForm: this.$form.createForm(this, { name: 'fastForm' }),
      detailForm: this.$form.createForm(this, { name: 'detailForm' }),
    };
  },
  props: {
    visible: Boolean,
  },
  components: {
    Modal,
    Form,
    Button,
    Input,
    RadioGroup: Radio.Group,
    FormItem: Form.Item,
    RadioButton: Radio.Button,
  },
  methods: {
    applyServer(values) {
      let serverAddr = values.serverAddr || '';
      if (!serverAddr.startsWith('http')) {
        serverAddr = 'https://' + serverAddr;
      }
      this.$emit('setPocConfig', serverAddr, values.appSecret, values.appId);
      localStorage.setItem(POC_APPID, values.appId);
      localStorage.setItem(POC_SERVER, serverAddr);
      localStorage.setItem(POC_SECRET, values.appSecret);
    },
    detailSet(e) {
      e.preventDefault();
      this.detailForm.validateFields((err, values) => {
        if (err) return;
        this.applyServer(values);
      });
    },
    fastSet(e) {
      e.preventDefault();
      this.fastForm.validateFields((err, values) => {
        if (err) return;
        try {
          const config = JSON.parse(atob(values.config));
          this.applyServer({
            appId: config.appId,
            serverAddr: config.apiServer,
            appSecret: config.appSecret,
          });
          localStorage.setItem(POC_CONFIG, values.config);
        } catch (error) {
          message.error('您输入的config有误，请重试');
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped></style>
