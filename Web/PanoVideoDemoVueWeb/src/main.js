import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import './assets/css/app.global.css';
import router from './router';
import store from './store';
import './assets/css/icons/iconfont.css';
import initPanoRtc from './utils/panortc';
import initPanoRts from './utils/panorts';

initPanoRtc();
initPanoRts();
Vue.config.productionTip = false;
Vue.use(ElementUI);

new Vue({
  router,
  store,
  components: { App },
  template: '<App />'
}).$mount('#app');
