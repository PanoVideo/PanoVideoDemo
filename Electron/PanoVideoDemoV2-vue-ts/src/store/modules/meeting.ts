import {
  localCacheKeyOpenCamAtStart,
  localCacheKeyOpenMicAtStart,
} from '../../constants';
import { info, SAFARI_15_1 } from '@/utils/common';
import { cloneDeep } from 'lodash-es';
import * as mutations from '../mutations';

export enum MeetingStatus {
  'notstart' = 'notstart',
  'disconnected' = 'disconnected',
  'connected' = 'connected',
  'reconnecting' = 'reconnecting',
  'countdownover' = 'countdownover',
  'ended' = 'ended',
}

let countdownInterval: NodeJS.Timeout; // 倒计时计时器

const initialState = {
  poc: process.env.POC,
  appId: '1bc3397de34643958bd26ca0abf3f999',
  panoToken: '',
  channelId: '',
  autoOpenVideo: true,
  autoMuteAudio: false,
  closeConfirmVisible: false, // 离会确认框
  remainSeconds: 0, // 剩余时间
  screenPreviewList: [] as any[], // 视频共享预览
  meetingStatus: MeetingStatus.notstart, // 会议状态
  meetingEndReason: '',
  fullscreen: false,
};

function getInitialState() {
  const state = cloneDeep(initialState);
  if (
    localStorage.getItem(localCacheKeyOpenCamAtStart) === 'no' ||
    SAFARI_15_1
  ) {
    state.autoOpenVideo = false;
  }
  if (localStorage.getItem(localCacheKeyOpenMicAtStart) === 'no') {
    state.autoMuteAudio = true;
  }
  return state;
}

type MeetingState = typeof initialState;

export default {
  state: getInitialState(),
  getters: {
    poc: (state: MeetingState) => state.poc,
    isInMeeting: (state: MeetingState) =>
      state.meetingStatus === MeetingStatus.connected ||
      state.meetingStatus === MeetingStatus.reconnecting,
    appId: (state: MeetingState) => state.appId,
    channelId: (state: MeetingState) => state.channelId,
    autoOpenVideo: (state: MeetingState) => state.autoOpenVideo,
    autoMuteAudio: (state: MeetingState) => state.autoMuteAudio,
    closeConfirmVisible: (state: MeetingState) => state.closeConfirmVisible,
    screenPreviewList: (state: MeetingState) => state.screenPreviewList,
    meetingStatus: (state: MeetingState) => state.meetingStatus,
    meetingEndReason: (state: MeetingState) => state.meetingEndReason,
    remainSeconds: (state: MeetingState) => {
      return state.remainSeconds;
    },
    fullscreen: (state: MeetingState) => state.fullscreen,
  },
  mutations: {
    [mutations.BEGIN_COUNTDOWN](
      state: MeetingState,
      payload: { remainSeconds: number }
    ) {
      state.remainSeconds = payload.remainSeconds;
      clearInterval(countdownInterval);
      const joinTime = new Date().getTime();
      countdownInterval = setInterval(() => {
        const interval = Math.floor((new Date().getTime() - joinTime) / 1000);
        state.remainSeconds = payload.remainSeconds - interval;
        if (state.remainSeconds <= 0) {
          state.meetingStatus = MeetingStatus.countdownover;
          clearInterval(countdownInterval);
        }
        if (state.remainSeconds === 300) {
          info('房间限时还剩5分钟', true);
        }
      }, 1000);
    },
    [mutations.RESET_MEETING_STORE](state: MeetingState) {
      clearInterval(countdownInterval);
      Object.assign(state, getInitialState());
    },
    [mutations.UPDATE_FULLSCREEN](
      state: MeetingState,
      payload: { fullscreen: boolean }
    ) {
      state.fullscreen = payload.fullscreen;
    },
    [mutations.SET_CLOSE_CONFIRM_VISIBLE](
      state: MeetingState,
      visible: boolean
    ) {
      state.closeConfirmVisible = visible;
    },
    [mutations.SET_SCREEN_PREVIEW_LIST](
      state: MeetingState,
      screenPreviewList: any[]
    ) {
      state.screenPreviewList = screenPreviewList;
    },
    [mutations.SET_OPEN_VIDEO_JOIN](
      state: MeetingState,
      autoOpenVideo: boolean
    ) {
      state.autoOpenVideo = autoOpenVideo;
    },
    [mutations.SET_MUTE_AUDIO_JOIN](
      state: MeetingState,
      autoMuteAudio: boolean
    ) {
      state.autoMuteAudio = autoMuteAudio;
    },
    [mutations.SET_MEETING_STATUS](
      state: MeetingState,
      payload: { meetingStatus: MeetingStatus; meetingEndReason: '' }
    ) {
      state.meetingEndReason = payload.meetingEndReason;
      state.meetingStatus = payload.meetingStatus;
    },
    [mutations.SET_CHANNELID](state: MeetingState, channelId: string) {
      state.channelId = channelId;
    },
  },
};
