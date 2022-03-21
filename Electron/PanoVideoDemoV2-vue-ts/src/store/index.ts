import Vue from 'vue';
import Vuex from 'vuex';
import userStore from './modules/user';
import wbStore from './modules/whiteboard';
import meetingStore from './modules/meeting';
import settingStore from './modules/setting';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {},
  mutations: {},
  actions: {},
  modules: {
    userStore,
    meetingStore,
    settingStore,
    wbStore,
  },
});
