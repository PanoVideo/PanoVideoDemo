import Vue from 'vue';
import router from './router';
import store from './store';
import 'ant-design-vue/dist/antd.css';
import '@/assets/css/global.less';
import '@/assets/css/icons/iconfont.css';
import { message, Modal, notification } from 'ant-design-vue';
import initPanoRts from '@/pano/panorts';
import clickoutside from '@/directives/clickoutside';

Vue.directive('clickoutside', clickoutside);
Vue.config.productionTip = false;
Vue.use(Modal);
Vue.prototype.$message = message;
Vue.prototype.$Modal = Modal;
Vue.prototype.$IS_ELECTRON = window.IS_ELECTRON;
Vue.prototype.$notification = notification;
message.config({ top: '130px' });

Vue.prototype.eventHub = new Vue();

export default function initApp() {
  (window as any).vm = new Vue({
    router,
    store,
    render: (h) => h('router-view'),
  }).$mount('#app');

  initPanoRts();
}
