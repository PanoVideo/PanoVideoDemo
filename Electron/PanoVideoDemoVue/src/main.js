import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import './assets/css/app.global.css';
import router from './router';
import store from './store';
import './assets/css/icons/iconfont.css';
import initPanoRtc from './setup-panortc';

Vue.config.productionTip = false;

Vue.use(ElementUI);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');

initPanoRtc();
