import { VideoProfileType } from '@pano.video/panortc-electron-sdk';
import * as Constants from '../constants';
import { find, findIndex } from 'lodash-es';
import {
  subscribeUserScreen,
  trySubscribeUserVideo,
  forceSubscribeUserVideo
} from '../utils/panortc';

const MOMENT_FOR_UNSUBSCRIBE = 0;

function makeVideoWrapperDom(id) {
  const divEl = document.createElement('div');
  divEl.id = id;
  divEl.style.height = '100%';
  divEl.style.width = '100%';
  return divEl;
}

function createUser(userId, userName) {
  return {
    userId,
    userName,
    videoDomRef: makeVideoWrapperDom(`videoDomRef-${userId}-${userName}`),
    screenDomRef: makeVideoWrapperDom(`screenDomRef-${userId}-${userName}`),
    subscribed: false,
    videoMuted: true,
    screenOpen: false,
    videoAnnotationOpen: false, // 视频标注是否打开
    shareAnnotationOpen: false, // 桌面共享标注是否打开
    audioMuted: true,
    isSpeaking: false,
    isMostActive: false,
    showInMainView: false,
    isScreenInMainView: false,
    locked: false,
    isWbAdmin: false,
    lastMomentAsMainView: 0,
    lastMomentSubscribeVideo: MOMENT_FOR_UNSUBSCRIBE,
    lastMomentSubscribeScreen: MOMENT_FOR_UNSUBSCRIBE,
    videoProfileType: VideoProfileType.Low
  };
}

function createUserMe() {
  return {
    ...createUser('1234', 'me'),
    showInMainView: true,
    isScreenInMainView: false,
    videoMuted:
      localStorage.getItem(Constants.localCacheKeyMuteCamAtStart) === 'yes',
    audioMuted:
      localStorage.getItem(Constants.localCacheKeyMuteMicAtStart) === 'yes',
    screenOpen: false
  };
}

export default {
  state: {
    userMe: createUserMe(),
    userList: []
  },
  mutations: {
    updateUserMe(state, payload) {
      state.userMe = { ...state.userMe, ...payload };
    },
    updateUser(state, userInfo) {
      const user = find([state.userMe, ...state.userList], {
        userId: userInfo.userId
      });
      Object.assign(user, userInfo);
    },
    addUser(state, { userId, userName }) {
      let user = this.getters.getUserById(userId);
      if (user) {
        user.userName = userName;
      } else {
        user = createUser(userId, userName);
        state.userList.push(user);
      }
      return user;
    },
    removeUser(state, userId) {
      state.userList.splice(findIndex(state.userList, { userId }), 1);
    },
    lockUser(state, userId) {
      console.log('lockUser', userId);
      state.userList.forEach(u => {
        u.locked = u.userId === userId;
      });
      state.userMe.locked = state.userMe.userId === userId;
    },
    // 设置白板 admin 用户
    setWbHost(state, userId) {
      [...state.userList, state.userMe].forEach(u => {
        u.isWbAdmin = u.userId === userId;
      });
    },
    resetUserStore(state) {
      state.userMe = createUserMe();
      state.userList = [];
    }
  },
  actions: {
    /**
     * 如果已经有mainview则返回，否则选择一个，如果指定
     */
    trySelectMainView({ getters, dispatch }, payload) {
      const candidateMainViewUser = payload.user;
      const isScreen = payload.isScreen;
      const mainViewUser = getters.mainViewUser;
      if (candidateMainViewUser) {
        if (
          mainViewUser &&
          mainViewUser.userId !== candidateMainViewUser.userId
        ) {
          /**
           * 在已经存在mainviewuser且不是当前candidate的情况下，
           * 只有mainViewUser的mainview没有视频，而目标video/screen已经开启情况才会抢占
           */
          if (
            ((!mainViewUser.screenOpen && mainViewUser.isScreenInMainView) ||
              (mainViewUser.videoMuted && !mainViewUser.isScreenInMainView)) &&
            ((candidateMainViewUser.screenOpen && isScreen) ||
              (!candidateMainViewUser.videoMuted && !isScreen)) &&
            !mainViewUser.locked
          ) {
            dispatch('setAsMainView', {
              user: candidateMainViewUser,
              screenShareAsMainView: !!isScreen
            });
          } else if (
            // 如果目标是screen，那么直接抢占
            !mainViewUser.locked &&
            candidateMainViewUser.screenOpen &&
            isScreen
          ) {
            dispatch('setAsMainView', {
              user: candidateMainViewUser,
              screenShareAsMainView: !!isScreen
            });
          }
        } else if (
          mainViewUser &&
          mainViewUser.userId === candidateMainViewUser.userId
        ) {
          if (!mainViewUser.locked && isScreen) {
            dispatch('setAsMainView', {
              user: candidateMainViewUser,
              screenShareAsMainView: !!isScreen
            });
          }
        } else {
          dispatch('setAsMainView', {
            user: candidateMainViewUser,
            screenShareAsMainView: !!isScreen
          });
        }
      } else if (!mainViewUser) {
        // 不指定candidate 的情况下，只有当前不存在mainviewuser的时候才会选择新的mainviewuser
        dispatch('selectMainViewUser');
      }
    },
    /**
     * 将用户设置成大图，设置成大图时会sub该用户的大图，同时取消前一个大图用户的大图订阅小图
     * @param user 用户
     * @param screenShareAsMainView 是否将桌面共享设置成大图
     */
    setAsMainView(
      { commit, getters },
      { user, screenShareAsMainView = false }
    ) {
      console.log('设置主视图用户', user, screenShareAsMainView);
      user.lastMomentAsMainView = new Date().getTime();
      if (getters.mainViewUser && user.userId !== getters.mainViewUser.userId) {
        const mainViewUser = getters.getUserById(getters.mainViewUser.userId);
        commit('updateUser', {
          userId: mainViewUser.userId,
          isScreenInMainView: false,
          showInMainView: false
        });
        trySubscribeUserVideo(mainViewUser);
      }
      commit('updateUser', {
        userId: user.userId,
        showInMainView: true,
        isScreenInMainView: !!screenShareAsMainView
      });
      // 如果不是screen占用mainview，那么必须是抢占式订阅video
      if (!screenShareAsMainView) {
        forceSubscribeUserVideo(user);
      }
      if (screenShareAsMainView) {
        subscribeUserScreen(user, true);
      }
    },
    /**
     * 选择一个用户作为大图用户，选择逻辑为：
     * 如果有新开共享用户，或者有别的开着共享的用户，有先展示他们的大图，
     * 如果没有大图用户或者大图用户视频没有开启，将 当前激励用户 > 开启视频的用户 > 用户列表里的第一个用户 >自己 设置成大图
     * @param screenUser 新开启桌面共享的用户
     */
    selectMainViewUser({ state, dispatch, getters }, payload) {
      const candidates = state.userList.filter(
        u => u.userId !== payload.exceptUserId
      );
      // 如果有新开共享用户，或者有别的开着共享的用户，有先展示他们的大图
      const userScreen = find(candidates, { screenOpen: true });
      if (userScreen) {
        dispatch('setAsMainView', {
          user: userScreen,
          screenShareAsMainView: true
        });
        return;
      }
      // 如果没有大图用户
      if (
        !getters.mainViewUser ||
        getters.mainViewUser.videoMuted ||
        getters.mainViewUser === getters.userMe ||
        getters.mainViewUser.userId === payload.exceptUserId
      ) {
        const nextMainViewUser =
          find(candidates, { videoMuted: false }) ||
          candidates[0] ||
          state.userMe;
        dispatch('setAsMainView', { user: nextMainViewUser });
      }
    }
  },
  getters: {
    allUsers: state => [state.userMe, ...state.userList],
    userList: state => state.userList,
    userMe: state => state.userMe,
    lockedUser: (state, getters) => find(getters.allUsers, { locked: true }),
    mainViewUser: (state, getters) =>
      find(getters.allUsers, { showInMainView: true }),
    wbAdminUser: (state, getters) =>
      find(getters.allUsers, { isWbAdmin: true }),
    mostActiveUser: (state, getters) =>
      find(getters.allUsers, { isMostActive: true }),
    getUserById: (state, getters) => userId =>
      find(getters.allUsers, { userId }),
    getUserIndexById: (state, getters) => userId =>
      findIndex(getters.allUsers, { userId })
  }
};
