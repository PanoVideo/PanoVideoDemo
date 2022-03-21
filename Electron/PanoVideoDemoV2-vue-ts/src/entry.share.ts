/**
 * 共享桌面相关控制窗口入口
 */
import Vue from 'vue';
import Share from '@/views/screenshare/Share.vue';
import '@/assets/css/icons/iconfont.css';
import '@/assets/css/global.less';
import 'ant-design-vue/dist/antd.css';
import ipc from '@/utils/ipc';
import * as remote from '@electron/remote';
import electron from 'electron';

Vue.config.productionTip = false;

window.electron = electron;
window.remote = remote;
window.ipc = ipc;

new Vue({
  render: (h) => h(Share),
}).$mount('#app');
