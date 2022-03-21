import Vue from 'vue';
import VueRouter from 'vue-router';
import Login from '../views/login/Login.vue';

import store from '@/store';

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login,
    },
    {
      path: '/Meeting',
      name: 'Meeting',
      component: () => import('../views/meeting/Meeting.vue'),
    },
    {
      path: '/device-test',
      name: 'DeviceTest',
      component: () => import('../views/devicetest/DeviceTest.vue'),
    },
  ],
});

router.beforeEach((to, from, next) => {
  // go to login page if not join
  if (to.name === 'Meeting' && !store.getters.isInMeeting) {
    next({ name: 'Login' });
  } else {
    next();
  }
});

export default router;
