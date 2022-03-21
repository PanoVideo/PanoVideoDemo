import { genUserVideoDomId, hasProperty } from '@/utils';
import { find, findIndex, cloneDeep, minBy, remove } from 'lodash-es';
import * as mutations from '../mutations';
import * as actions from '../actions';
import { RtsService } from '@pano.video/panorts';

function makeVideoWrapperDom(id: string) {
  const divEl = document.createElement('div');
  divEl.className = 'pvc-video-wrapper';
  divEl.id = id;
  divEl.style.height = '100%';
  divEl.style.width = '100%';
  return divEl;
}

function createUser(userId: string, userName: string): UserInfo {
  return {
    userId,
    userName,
    videoDomRef: makeVideoWrapperDom(`videoDomRef-${userId}-${userName}`),
    screenDomRef: makeVideoWrapperDom(`screenDomRef-${userId}-${userName}`),
    videoMuted: true,
    screenOpen: false,
    shareAnnotationOn: false,
    videoAnnotationOn: false,
    externalAnnotationOn: false,
    audioMuted: true,
    screenShareType: 'none',
    lastActiveTime: 0,
  };
}

function createUserMe() {
  return {
    ...createUser('9527', 'me'),
    videoMuted: true,
    audioMuted: true,
    screenOpen: false,
  };
}

export const MaxHorizontalPageSize = 6;

export const MaxVerticalPageSize = 5;

export const MinPageSize = 1;

export enum MediaType {
  video = 'video',
  screen = 'screen',
}
export interface MediaData {
  readonly user: UserInfo;
  readonly type: MediaType;
}

/**
 * @param horizontal 水平小视频布局
 * @param vertical 垂直小视频布局
 * @param vertical_asl 垂直仅显示ASL布局
 */
export type MediaLayout = 'horizontal' | 'vertical' | 'vertical_asl';

let lastMainUserId = ''; // 上一个主视图用户ID

const initialState = {
  userMe: createUserMe(),
  userList: new Array<UserInfo>(),
  pageIndex: 0,
  pageSize: MaxHorizontalPageSize,
  mediaLayout: 'horizontal' as MediaLayout,
  hostId: '', // 主持人userId
  aslMap: new Map<string, number>(), // ASL map, key: userId , value: timestamp;
  mostActiveUserId: '', // asl 最活跃用户Id
  lockedMedia: { type: MediaType.video, userId: '' }, // 锁定的用户
  annotationMedia: { type: MediaType.video, userId: '' }, // 正在标注的
  screens: new Array<string>(), // 使用队列存储共享屏幕，优先显示最新的共享
};

function getInitialState() {
  const state = cloneDeep(initialState);
  return state;
}

type UserState = typeof initialState;

export default {
  state: getInitialState(),
  mutations: {
    [mutations.UPDATE_USER_ME](state: UserState, payload: UserInfo) {
      Object.assign(state.userMe, payload);
    },
    [mutations.UPDATE_USER_ME_DOM_ID](state: UserState) {
      state.userMe.videoDomRef.id = genUserVideoDomId(
        'video',
        state.userMe.userId,
        state.userMe.userName
      );
      state.userMe.screenDomRef.id = genUserVideoDomId(
        'screen',
        state.userMe.userId,
        state.userMe.userName
      );
    },
    [mutations.UPDATE_USER](state: UserState, payload: any) {
      const user = find([state.userMe, ...state.userList], {
        userId: payload.userId,
      });
      if (!user) return;
      const newV = Object.assign({}, user, payload);
      if (payload.userId === state.userMe.userId) {
        state.userMe = newV;
      } else {
        const index = findIndex(state.userList, { userId: payload.userId });
        state.userList.splice(index, 1, newV);
      }
      if (hasProperty(payload, 'screenOpen')) {
        const index = findIndex(state.screens, (i) => i === payload.userId);
        if (payload.screenOpen && index === -1) {
          state.screens.unshift(payload.userId);
        } else if (!payload.screenOpen && index !== -1) {
          state.screens.splice(index, 1);
        }
      }
    },
    [mutations.LOCK_USER](
      state: UserState,
      payload: { userId: string; type: MediaType }
    ) {
      state.lockedMedia = { ...state.lockedMedia, ...payload };
    },
    [mutations.SET_HOST_ID](state: UserState, payload: string) {
      state.hostId = payload;
    },
    [mutations.UPDATE_ASL](state: UserState, payload: { list: string[] }) {
      if (!payload.list || payload.list.length < 1) {
        return;
      }
      const timestamp = new Date().getTime();
      payload.list.forEach((userId, index: number) => {
        state.aslMap.set(userId, timestamp - index);
      });
      state.mostActiveUserId = payload.list[0];
    },
    [mutations.RESET_USER_STORE](state: UserState) {
      Object.assign(state, getInitialState());
      lastMainUserId = '';
    },
    [mutations.UPDATE_MEDIALAYOUT](
      state: UserState,
      payload: { fullscreen: boolean; layout: MediaLayout }
    ) {
      if (hasProperty(payload, 'fullscreen')) {
        state.mediaLayout = payload.fullscreen ? 'vertical' : 'horizontal';
      } else {
        state.mediaLayout = payload.layout;
      }
    },
    [mutations.UPDATE_PAGE_INDEX](
      state: UserState,
      payload: { pageIndex: number }
    ) {
      state.pageIndex = payload.pageIndex;
    },
    [mutations.UPDATE_PAGE_SIZE](
      state: UserState,
      payload: { pageSize: number }
    ) {
      state.pageSize = payload.pageSize;
    },
    [mutations.UPDATE_USER_LIST](state: UserState, payload: { userList: any }) {
      state.userList = [...payload.userList];
    },
    [mutations.UPDATE_ANNOTATION_STATUS](
      state: UserState,
      payload: { userId: string; type: MediaType; on: boolean }
    ) {
      if (payload.on) {
        state.annotationMedia = payload;
      } else {
        state.annotationMedia = {
          userId: '',
          type: MediaType.video,
        };
      }
    },
  },
  actions: {
    [actions.ADD_USER](
      ctx: {
        state: UserState;
        getters: any;
        commit: any;
        dispatch: any;
      },
      payload: { userId: string; userName: string }
    ) {
      const { userId, userName } = payload;
      let user = ctx.getters.getUserById(userId) as UserInfo;
      if (user) {
        ctx.commit(mutations.UPDATE_USER, { userId, userName });
      } else {
        user = createUser(userId, userName);
        const userList = ctx.state.userList;
        userList.push(user);
        ctx.commit(mutations.UPDATE_USER_LIST, { userList });
      }
      if (userId == ctx.state.mostActiveUserId) {
        ctx.dispatch(actions.SORT_USERLIST_BY_ACTIVE_USER);
      }
      return user;
    },
    [actions.REMOVE_USER](
      ctx: {
        state: UserState;
        getters: any;
        commit: any;
        dispatch: any;
      },
      payload: { userId: string }
    ) {
      ctx.state.userList.splice(
        findIndex(ctx.state.userList, { userId: payload.userId }),
        1
      );
      if (ctx.getters.mostActiveUser === payload.userId) {
        ctx.state.mostActiveUserId = '';
      }
      if (ctx.getters.lockedMedia.userId === payload.userId) {
        ctx.state.lockedMedia = { type: MediaType.video, userId: '' };
      }
      if (ctx.getters.annotationMedia.userId === payload.userId) {
        ctx.state.annotationMedia = { type: MediaType.video, userId: '' };
      }
      const screenIndex = findIndex(ctx.state.screens, payload.userId);
      if (screenIndex !== -1) {
        ctx.state.screens.splice(screenIndex, 1);
      }
      ctx.dispatch(actions.UPDATE_PAGE_INDEX_ACTION);
    },
    // 更新小视频列表当前的索引
    [actions.UPDATE_PAGE_INDEX_ACTION](ctx: {
      state: UserState;
      getters: any;
      commit: any;
    }) {
      while (
        ctx.state.pageIndex * ctx.state.pageSize >=
          ctx.getters.allPagesUsers.length ||
        ctx.state.pageIndex > ctx.getters.maxPageIndex
      ) {
        ctx.commit(mutations.UPDATE_PAGE_INDEX, {
          pageIndex: ctx.state.pageIndex - 1,
        });
      }
    },
    // 根据ActiveUser 对用户列表进行排序
    [actions.SORT_USERLIST_BY_ACTIVE_USER](ctx: {
      state: UserState;
      getters: any;
      commit: any;
      dispatch: any;
    }) {
      const mostActiveUser = ctx.getters.mostActiveUser;
      if (mostActiveUser === ctx.getters.userMe) {
        return;
      }
      // 1. 获取第一页所有视频
      const size = ctx.getters.pageSize - 1;
      const firstPage = ctx.getters.userList.slice(0, size) as UserInfo[];
      // 2. 检查第一页是否包含 ASL 用户
      const containActiveUser = firstPage.some(
        (u: UserInfo) => u === mostActiveUser
      );
      // 3. ASL用户如果不在第一页，找到当前页最不活跃的用户
      const aslMap = ctx.state.aslMap;
      if (!containActiveUser && mostActiveUser && firstPage.length) {
        const noActiveUser = minBy(firstPage, (item: UserInfo) => {
          return aslMap.get(item.userId) || -1;
        })!;
        const i = findIndex(ctx.state.userList, (u) => u === noActiveUser);
        // 3.1 删除最不活跃的用户
        const userList = ctx.state.userList;
        userList.splice(i, 1);
        // 3.2 删除ASL用户
        remove(userList, (u) => u === mostActiveUser);
        // 3.3 添加ASL用户
        userList.splice(i, 0, mostActiveUser);
        // 3.4 添加删除的不活跃用户
        userList.splice(size, 0, noActiveUser);
        ctx.commit(mutations.UPDATE_USER_LIST, { userList });
      }
    },
    // 更新小视频列表显示的视频的数量
    [actions.UPDATE_PAGE_SIZE_ACTION](
      ctx: { state: UserState; getters: any; commit: any; dispatch: any },
      payload: { size: number }
    ) {
      const { state } = ctx;
      const maxPageSize = ctx.getters.maxPageSize;
      const maxPageIndex = ctx.getters.maxPageIndex;
      const { pageSize, pageIndex } = state;
      const { size } = payload;
      if (pageSize === size) {
        return;
      }
      let tempSize = size;
      const incrementSize = tempSize > pageSize;
      if (tempSize < MinPageSize) {
        tempSize = MinPageSize;
      }
      if (size > maxPageSize) {
        tempSize = maxPageSize;
      }
      ctx.commit(mutations.UPDATE_PAGE_SIZE, {
        pageSize: tempSize,
      });
      if (
        incrementSize &&
        (pageIndex > maxPageIndex ||
          maxPageSize * pageIndex >= ctx.getters.allPagesUsers.length)
      ) {
        ctx.commit(mutations.UPDATE_PAGE_INDEX, {
          pageIndex: ctx.state.pageIndex - 1,
        });
      }
      ctx.dispatch(actions.UPDATE_PAGE_INDEX_ACTION);
    },
    // 翻到上一页
    [actions.GOTO_PREV_PAGE](ctx: {
      state: UserState;
      getters: any;
      commit: any;
      dispatch: any;
    }) {
      if (ctx.getters.enableGotoPrevPage) {
        ctx.commit(mutations.UPDATE_PAGE_INDEX, {
          pageIndex: ctx.state.pageIndex - 1,
        });
      }
    },
    // 翻到下一页
    [actions.GOTO_NEXT_PAGE](ctx: {
      state: UserState;
      getters: any;
      commit: any;
      dispatch: any;
    }) {
      if (ctx.getters.enableGotoNextPage) {
        ctx.commit(mutations.UPDATE_PAGE_INDEX, {
          pageIndex: ctx.state.pageIndex + 1,
        });
      }
    },
    [actions.STOP_VIDEO_ANNOTATION](
      ctx: { state: UserState; commit: any },
      userId: string
    ) {
      if (
        ctx.state.annotationMedia.userId === userId &&
        ctx.state.annotationMedia.type === MediaType.video
      ) {
        // 停止视频标注
        RtsService.getInstance().getAnnotation(userId, 'video').stop();
        ctx.commit(mutations.UPDATE_USER, { userId, videoAnnotationOn: false });
        ctx.commit(mutations.UPDATE_ANNOTATION_STATUS, { on: false });
      }
    },
    [actions.START_VIDEO_ANNOTATION](ctx: { commit: any }, userId: string) {
      ctx.commit(mutations.LOCK_USER, { userId: '' });
      ctx.commit(mutations.UPDATE_USER, {
        userId,
        videoAnnotationOn: true,
      });
      ctx.commit(mutations.UPDATE_ANNOTATION_STATUS, {
        userId,
        on: true,
        type: MediaType.video,
      });
    },
    [actions.STOP_SHARE_ANNOTATION](
      ctx: { state: UserState; commit: any },
      userId: string
    ) {
      if (
        ctx.state.annotationMedia.userId === userId &&
        ctx.state.annotationMedia.type === MediaType.screen
      ) {
        ctx.commit(mutations.UPDATE_USER, { userId, shareAnnotationOn: false });
        ctx.commit(mutations.UPDATE_ANNOTATION_STATUS, { on: false });
      }
    },
    [actions.START_SHARE_ANNOTATION](ctx: { commit: any }, userId: string) {
      ctx.commit(mutations.UPDATE_USER, { userId, shareAnnotationOn: true });
      ctx.commit(mutations.UPDATE_ANNOTATION_STATUS, {
        userId,
        on: true,
        type: MediaType.screen,
      });
    },
    [actions.STOP_SHARE_SCREEN](ctx: { commit: any; getters: any }) {
      if (ctx.getters.userMe.screenOpen) {
        window.rtcEngine.stopScreen();
        window.ipc?.sendToMainProcess({
          command: 'hideShareCtrlWindow',
        });
        window.ipc?.sendToMainProcess({
          command: 'destoryShareCtrlWin',
        });
        ctx.commit(mutations.UPDATE_USER_ME, { screenOpen: false });
      }
    },
  },
  getters: {
    /**
     * @returns 返回自己
     */
    userMe: (state: UserState): UserInfo => state.userMe,
    /**
     * 返回所有用户
     */
    userList: (state: UserState): UserInfo[] => {
      return state.userList;
    },
    /**
     * 返回所有用户
     */
    allUsers: (state: UserState, getters: any): UserInfo[] => [
      state.userMe,
      ...getters.userList,
    ],
    /**
     * @returns 返回锁定用户
     */
    lockedUser(state: UserState, getters: any): UserInfo | undefined {
      return find(getters.allUsers, { locked: true });
    },
    /**
     * @returns 所有用户 (不包含自己)
     */
    hostId: (state: UserState): string => state.hostId,
    /**
     * @returns 返回白板Admin用户
     */
    hostUser(state: UserState): UserInfo | undefined {
      return find([state.userMe, ...state.userList], { userId: state.hostId });
    },
    isHost: (state: UserState, getters: any): boolean =>
      state.hostId === getters.userMe.userId,
    /**
     * @returns 返回ASL用户
     */
    mostActiveUser(state: UserState, getters: any) {
      const user = find([state.userMe, ...getters.userList], {
        userId: state.mostActiveUserId,
      });
      return user && !user.audioMuted ? user : undefined;
    },
    getUserData: (state: UserState, getters: any) => (userId: string) =>
      getters.getUserById(userId).userData,
    /**
     * @returns 根据UserId获得用户
     */
    getUserById: (state: UserState) => (userId: string) =>
      find([state.userMe, ...state.userList], { userId }),
    /**
     * 查询用户是否存在（如果开启分组，不在当前分组内的用户返回false）
     */
    isUserAvailable: (state: UserState, getters: any) => (userId: string) => {
      return !!find(getters.allUsers, { userId });
    },
    /**
     * @returns 返回UserId获得用户的索引
     */
    getUserIndexById: (state: UserState, getters: any) => (userId: string) =>
      findIndex(getters.allUsers, { userId }),
    /**
     * @returns 小视频列表布局模式
     */
    mediaLayout: (state: UserState): MediaLayout =>
      state.mediaLayout as MediaLayout,
    /**
     * @returns 能否向前翻页
     */
    enableGotoPrevPage: (state: UserState): boolean => state.pageIndex >= 1,
    /**
     * @returns 能否向后翻页
     */
    enableGotoNextPage: (state: UserState, getters: any): boolean =>
      getters.allPagesUsers.length > (state.pageIndex + 1) * state.pageSize,
    /**
     * @returns 返回主视图用户
     */
    mainViewUser: (state: UserState, getters: any): UserInfo =>
      getters.mainViewUserData.user,
    /**
     * @returns 返回主视图用户Data
     */
    mainViewUserData: (
      state: UserState,
      getters: any
    ): MediaData | undefined => {
      /**
       * 1. 正在远程控制的共享
       * 2. 显示Lock的视频/共享 (需要区分视频和共享)
       * 3. 显示视频标注
       * 4. 显示桌面共享 (后共享优先显示)
       * 5. 显示ASL用户 (ASL不是自己)
       * 6. 显示其他开视频的用户
       * 7. 显示其他未开视频的用户
       * 8. 显示自己
       */
      let mainUserId = '';
      let type = MediaType.video;
      if (
        getters.isWhiteboardOpen ||
        (window.IS_ELECTRON && getters.userMe.screenOpen)
      ) {
        return undefined;
      } else if (getters.lockedMedia.userId) {
        mainUserId = getters.lockedMedia.userId;
        type = getters.lockedMedia.type;
      } else if (getters.videoAnnotationUser) {
        mainUserId = getters.videoAnnotationUser.userId;
        type = MediaType.video;
      } else if (getters.screens.length > 0) {
        mainUserId = getters.screens[0];
        type = MediaType.screen;
      } else if (
        getters.mostActiveUser &&
        getters.mostActiveUser !== getters.userMe
      ) {
        mainUserId = getters.mostActiveUser.userId;
      } else if (getters.userList.length > 0) {
        const u = find(getters.userList, { videoMuted: false });
        const last = find(getters.userList, { userId: lastMainUserId });
        if (last && !last.videoMuted) {
          mainUserId = lastMainUserId;
        } else if (u) {
          mainUserId = u.userId;
        } else if (last) {
          mainUserId = lastMainUserId;
        } else {
          mainUserId = getters.userList[0].userId;
        }
      }
      const user = getters.getUserById(mainUserId);
      if (user) {
        // 如果不是自己，缓存上一次主视图
        if (getters.userMe.userId !== mainUserId) {
          lastMainUserId = mainUserId;
        }
        return { type, user };
      }
      return { type: MediaType.video, user: getters.userMe };
    },
    isLocked:
      (state: UserState, getters: any) =>
      (data: MediaData): boolean => {
        return (
          getters.lockedMedia.type === data.type &&
          getters.lockedMedia.userId === data.user.userId
        );
      },
    /**
     * @returns 锁定的视频/共享桌面
     */
    lockedMedia: (state: UserState) => state.lockedMedia,
    /**
     * @returns 正在标注的视频或共享
     */
    annotationMedia: (state: UserState) => state.annotationMedia,
    /**
     * @returns 使用队列存储共享屏幕，优先显示最新的共享
     */
    screens: (state: UserState, getters: any) => {
      return state.screens.filter((userId) =>
        find(getters.userList, (user: UserInfo) => user.userId === userId)
      );
    },
    screenSharingUser: (state: UserState, getters: any) =>
      find(getters.allUsers, { screenOpen: true }),
    /**
     * @returns 返回最大页数
     */
    maxPageIndex: (state: UserState, getters: any): number =>
      Math.floor((getters.allPagesUsers.length - 1) / state.pageSize),
    /**
     * @returns 小视频列表最多显示几个小视频
     */
    maxPageSize: (state: UserState, getters: any): number =>
      getters.mediaLayout === 'horizontal'
        ? MaxHorizontalPageSize
        : MaxVerticalPageSize,
    /**
     *
     * @returns 返回当前页的大小
     */
    pageSize: (state: UserState) => state.pageSize,
    /**
     * @returns 返回当前小视频列表所有数据
     */
    allPagesUsers(state: UserState, getters: any): MediaData[] {
      // 0. 如果当前布局是 vertical_asl
      const me: MediaData = { user: getters.userMe, type: MediaType.video };
      const mainUser = getters.mainViewUserData as MediaData;
      const flag =
        (mainUser && mainUser.type === MediaType.screen) ||
        (mainUser && mainUser.user.userId === getters.lockedMedia.userId) ||
        getters.isWhiteboardOpen ||
        (getters.userMe.screenOpen && window.IS_ELECTRON);
      // 如果正在是Electron共享优先显示asl
      if (getters.mediaLayout === 'asl_text') {
        return [];
      } else if (getters.mediaLayout === 'vertical_asl') {
        // 0.1 主视图是共享屏幕 或者 主视图被锁定
        const mostActiveUser = getters.mostActiveUser;
        if (flag && mostActiveUser) {
          return [{ user: mostActiveUser, type: MediaType.video }];
        }
        // 0.2 直接返回自己
        return [me];
      }
      // const screenData = getters.userList
      //   .filter((u: UserInfo) => u.screenOpen && u !== getters.userMe)
      //   .map((u: UserInfo): MediaData => ({ user: u, type: MediaType.screen }));
      // 4. 组合新的视频MediaData
      const videoData = getters.userList.map(
        (u: UserInfo): MediaData => ({ user: u, type: MediaType.video })
      );
      const allUsers = [me, ...videoData];
      return allUsers;
    },
    /**
     * 当前页需要显示的用户
     */
    currentPageUsers(state: UserState, getters: any): MediaData[] {
      const allUsers = getters.allPagesUsers;
      let res = allUsers.slice(
        state.pageIndex * state.pageSize,
        state.pageIndex * state.pageSize + state.pageSize
      );
      if (allUsers.length >= state.pageSize && res.length !== state.pageSize) {
        res = allUsers.slice(-state.pageSize);
      }
      return res;
    },
    videoAnnotationUser(state: UserState, getters: any) {
      return find(getters.allUsers, { videoAnnotationOn: true });
    },
    /**
     *  所有需要被 subscribe 的用户列表
     */
    videoSubscribeList(state: UserState, getters: any) {
      const currentPageUsers = getters.currentPageUsers as MediaData[];
      const mainUserData = getters.mainViewUserData as MediaData;
      const smallVideos = currentPageUsers
        .filter((data) => {
          if (data.user.userId === state.userMe.userId) {
            return false;
          }
          if (data.type === MediaType.screen && data.user.screenOpen) {
            return true;
          }
          if (data.type === MediaType.video && !data.user.videoMuted) {
            return true;
          }
          return false;
        })
        .map((data) => {
          return {
            userId: data.user.userId,
            videoType: data.type,
            showInMainView: false,
          };
        });
      /**
       * 1. 没有打开白板
       * 2. Electron 版本自己没有开启共享
       */
      if (
        !getters.isWhiteboardOpen &&
        (!window.IS_ELECTRON || !state.userMe.screenOpen) &&
        mainUserData &&
        mainUserData.user !== state.userMe
      ) {
        // 主视图用户需要开着桌面共享或者视频
        if (
          mainUserData.type === MediaType.screen ||
          !mainUserData.user.videoMuted
        ) {
          return [
            {
              userId: mainUserData.user.userId,
              videoType: mainUserData.type,
              showInMainView: true,
            },
            ...smallVideos.filter(
              (data) =>
                data.userId !== mainUserData.user.userId ||
                data.videoType !== mainUserData.type
            ),
          ];
        }
      }
      return smallVideos;
    },
  },
};
