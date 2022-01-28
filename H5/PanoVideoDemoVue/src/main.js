import Vue from 'vue';
import Toast from 'vant/lib/toast';
import 'vant/lib/toast/style';
import { RtcEngine } from '@pano.video/panortc';
import store from './store';
import './less/common.less';
import router from './router';

/**
 * NOTE 必须在页面设计的交互之前创建SDK实例，为了尽可能利用可能很少的交互
 *
 * 在多个页面和组件中都会使用这个服务对象，也为了调试方便，直接挂在window上
 * 注意不要放到组件的生命周期中取构造，容易重复构造导致bug
 * 正常在整个app的使用周期中都不需要destroy，只需要leaveChannel即可，一直重复使用这个单例
 */
window.panoSDK = new RtcEngine();

Vue.use(Toast);

Vue.config.productionTip = false;

new Vue({
  store,
  router,
  render: (h) => h('router-view'),
}).$mount('#app');
