import Vue from 'vue';
import VueRouter from 'vue-router';
import Join from '../pages/join/index.vue';
import Call from '../pages/call/index.vue';

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Join',
    component: Join,
  },
  {
    path: '/call',
    name: 'call',
    // 不能用动态加载，微信中不能自动播放
    component: Call,
  },
];

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes,
});

export default router;
