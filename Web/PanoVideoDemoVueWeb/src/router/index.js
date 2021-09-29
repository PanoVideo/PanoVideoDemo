import Vue from 'vue';
import VueRouter from 'vue-router';
import Login from '../views/Login.vue';

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/meeting',
    name: 'Meeting',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "meeting" */ '../views/Meeting.vue')
  }
];

const router = new VueRouter({
  routes
});

// 默认进入 login 页面
router.push({ name: 'Login' });

export default router;
