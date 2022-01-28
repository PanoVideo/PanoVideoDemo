import Vue from 'vue';
import Vuex from 'vuex';
import { Constants } from '@pano.video/panortc';
import { isOpen } from '../utils';

Vue.use(Vuex);

const { VideoProfileType } = Constants;

const statusMap = {
  start: 'open',
  stop: 'close',
  mute: 'mute',
  unmute: 'unmute',
};

const networkStatusLevelMap = {
  Unavailable: 'bad',
  VeryBad: 'bad',
  Bad: 'poor',
  Poor: 'poor',
  Good: 'good',
  Excellent: 'good',
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
    userIdList: [],
    userIdMap: {},
    userMap: {},
    audioLevelMap: {},
    screenUserIdList: [],
    screenVideo: null,
    settingVisible: false,
    userListVisible: false,
    userVideoMap: {},
    whiteboardAvailable: null,
    hostId: '',
    whiteboardVisible: false,
    networkStatusMap: {},
  };
}

export default new Vuex.Store({
  state: Object.assign(getInitState(), {
    setting: {
      videoProfile: VideoProfileType.HD720P,
    },
  }),
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
      const { userId } = payload;
      state.userMap = {
        ...state.userMap,
        [userId]: payload,
      };
      const existing = state.userIdMap[userId];
      if (!existing) {
        state.userIdMap = {
          ...state.userIdMap,
          [userId]: true,
        };
        state.userIdList.push(userId);
      }
    },
    removeUser(state, payload) {
      const { userId } = payload;
      const index = state.userIdList.indexOf(userId);
      if (index !== -1) {
        state.userMap = {
          ...state.userMap,
          [userId]: undefined,
        };
        state.userIdMap = {
          ...state.userIdMap,
          [userId]: undefined,
        };
        state.userIdList.splice(index, 1);
      }
    },
    updateUserStatus(state, payload) {
      const { userId } = payload;
      const user = userId === state.user.userId ? state.user : state.userMap[userId];
      if (user) {
        const strList = payload.event.split('_');
        user[`${strList[1]}Status`] = statusMap[strList[2]];
        state.userMap = {
          ...state.userMap,
          userId: user,
        };
      }
    },
    updateUserAudioLevel(state, payload) {
      if (Array.isArray(payload)) {
        const selfId = state.user.userId;
        const selfAudioLevel = state.audioLevelMap[selfId];
        state.audioLevelMap = payload.reduce((result, item) => {
          result[item.userId] = item.level;
          return result;
        }, selfAudioLevel !== undefined ? {
          [selfId]: selfAudioLevel,
        } : {});
      } else {
        state.audioLevelMap = {
          ...state.audioLevelMap,
          [payload.userId]: payload.level,
        };
      }
    },
    updateNetworkStatusMap(state, payload) {
      state.networkStatusMap = payload.reduce((result, item) => {
        result[item.userId] = networkStatusLevelMap[item.rating];
        return result;
      }, {});
    },
    addScreenUserId(state, payload) {
      state.screenUserIdList = [payload].concat(
        state.screenUserIdList.filter((userId) => userId !== payload),
      );
    },
    removeScreenUserId(state, payload) {
      state.screenUserIdList = state.screenUserIdList.filter((userId) => userId !== payload);
    },
    addUserVideo(state, payload) {
      state.userVideoMap = {
        ...state.userVideoMap,
        [payload.userId]: payload.videoTag,
      };
    },
    removeUserVideo(state, payload) {
      delete state.userVideoMap[payload];
    },
    resetStore(state) {
      Object.assign(state, getInitState());
    },
    openSetting(state) {
      state.settingVisible = true;
    },
    closeSetting(state) {
      state.settingVisible = false;
    },
    updateSetting(state, payload) {
      Object.assign(state.setting, payload);
    },
    openUserList(state) {
      state.userListVisible = true;
    },
    closeUserList(state) {
      state.userListVisible = false;
    },
    updateScreenVideo(state, payload) {
      state.screenVideo = payload;
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
    setHostId(state, payload) {
      state.hostId = payload;
    },
  },
  getters: {
    activeUserId(state) {
      return state.userIdList[0] || null;
    },
    activeUser(state, getters) {
      const userId = getters.activeUserId;
      if (userId === null) {
        return null;
      }
      return {
        ...state.userMap[userId],
        videoTag: state.userVideoMap[userId],
      };
    },
    screenUserId(state) {
      const userId = state.screenUserIdList.find((item) => state.userIdMap[item]);
      return userId || null;
    },
    userScreen(state, getters) {
      if (!state.screenVideo) {
        return null;
      }
      return {
        ...state.userMap[getters.screenUserId],
        videoTag: state.screenVideo,
      };
    },
    videoSubMap(state, getters) {
      const videoSubList = [];
      const { screenUserId, activeUserId } = getters;
      const { Low, HD1080P } = Constants.VideoProfileType;
      if (screenUserId) { // 开启了屏幕共享
        videoSubList.push([activeUserId, Low]);
      } else if (activeUserId) {
        videoSubList.push([activeUserId, HD1080P]);
      }
      return videoSubList.reduce((result, item) => {
        if (isOpen(state.userMap[item[0]].videoStatus)) { // 订阅前需要判断视频是否打开
          result[item[0]] = item[1];
        }
        return result;
      }, {});
    },
    hostUser(state) {
      if (!state.hostId) {
        return undefined;
      }
      if (state.hostId === state.user.userId) {
        return state.user;
      }
      return state.userMap[state.hostId];
    },
  },
});
