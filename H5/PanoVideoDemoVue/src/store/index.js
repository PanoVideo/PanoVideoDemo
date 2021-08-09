import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

const statusMap = {
  start: 'open',
  stop: 'close',
  mute: 'mute',
  unmute: 'unmute',
};

function getInitState() {
  return {
    user: {
      channelId: '',
      userId: '',
      userName: '',
      audioStatus: 'close',
      videoStatus: 'close',
      srcObj: null,
    },
    joinLoading: false,
    remainTime: '',
    userList: [],
    userListVisible: false,
    whiteboardAvailable: null,
    whiteboardVisible: false,
  };
}

export default new Vuex.Store({
  state: getInitState(),
  mutations: {
    startJoinLoading(state) {
      state.joinLoading = true;
    },
    endJoinLoading(state) {
      state.joinLoading = false;
    },
    updateUser(state, payload) {
      state.user = {
        ...state.user,
        ...payload,
      };
    },
    updateRemainTime(state, payload) {
      state.remainTime = payload;
    },
    addUser(state, payload) {
      state.userList.push(payload);
    },
    removeUser(state, payload) {
      state.userList = state.userList.filter((user) => payload.userId !== user.userId);
    },
    updateUserStatus(state, payload) {
      const strList = payload.event.split('_');
      const statusName = `${strList[1]}Status`;
      const status = statusMap[strList[2]];
      if (payload.userId === state.user.userId) {
        state.user[statusName] = status;
        return;
      }
      state.userList = state.userList.map((user) => payload.userId === user.userId ? ({
        ...user,
        [statusName]: status,
        ...payload.options,
      }) : user);
    },
    updateUserList(state, payload) {
      state.userList = state.userList.map((user) => payload.userId === user.userId ? ({
        ...user,
        ...payload,
      }) : user);
    },
    resetStore(state) {
      Object.assign(state, getInitState());
    },
    openUserList(state) {
      state.userListVisible = true;
    },
    closeUserList(state) {
      state.userListVisible = false;
    },
    setWhiteboardAvailable(state, payload) {
      state.whiteboardAvailable = payload;
    },
    openWhiteboard(state) {
      state.whiteboardVisible = true;
    },
    closeWhiteboard(state) {
      state.whiteboardVisible = false;
    },
  },
  actions: {
  },
  modules: {
  },
});
