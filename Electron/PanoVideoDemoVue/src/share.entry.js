import Vue from 'vue';
import Share from './views/share/Share';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/css/icons/iconfont.css';
import './assets/css/reset.css';

Vue.config.productionTip = false;
Vue.use(ElementUI);

new Vue({
  render: h => h(Share)
}).$mount('#app');
