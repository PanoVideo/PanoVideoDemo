import Vue from 'vue';
import Toast from './plugins/Toast/index';
import App from './App.vue';
import store from './store';
import './less/common.less';

Vue.use(Toast);

Vue.config.productionTip = false;

new Vue({
  store,
  render: (h) => h(App),
}).$mount('#app');
