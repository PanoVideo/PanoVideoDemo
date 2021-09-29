import Vue from 'vue';
import Vuex from 'vuex';
import meetingStore from './meeting.store';
import userStore from './user.store';

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    userStore,
    meetingStore
  }
});
