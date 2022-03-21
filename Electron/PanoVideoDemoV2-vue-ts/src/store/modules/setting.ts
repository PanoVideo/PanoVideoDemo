import {
  localCacheKeySelectedMic,
  localCacheKeySelectedSpeaker,
  localCacheKeySelectedCam,
  localCacheKeyVideoProfileType,
  localCacheKeyVideoFrameRate,
} from '../../constants';
import { hasProperty } from '@/utils';
import * as mutations from '../mutations';

const state = {
  panoServer: 'pano.video',
  envSettingVisible: false, // 需要去掉
  recordDeviceId: localStorage.getItem(localCacheKeySelectedMic) || 'default',
  playoutDeviceId:
    localStorage.getItem(localCacheKeySelectedSpeaker) || 'default',
  captureDeviceId: localStorage.getItem(localCacheKeySelectedCam) || '',
  videoProfileType:
    localStorage.getItem(localCacheKeyVideoProfileType) || 'HD720P', // VideoProfileType.HD720P
  videoFrameRate:
    parseInt(localStorage.getItem(localCacheKeyVideoFrameRate) || '', 10) || 1,
  enableAAGC: true,
  audioDeviceReady: false,
  videoDeviceReady: false,
  micList: [] as any[],
  speakerList: [] as any[],
  cameraList: [] as any[],
  micAllowed: true,
  cameraAllowed: true,
  sysDefaultMicId: '',
  sysDefaultSpeakerId: '',
  screenShareMode: 0,
};

type SettingState = typeof state;

export default {
  state,
  getters: {
    panoServer: (state: SettingState) => state.panoServer,
    envSettingVisible: (state: SettingState) => state.envSettingVisible,
    enableAAGC: (state: SettingState) => state.enableAAGC,
    recordDeviceId: (state: SettingState) => state.recordDeviceId,
    playoutDeviceId: (state: SettingState) => state.playoutDeviceId,
    captureDeviceId: (state: SettingState) => state.captureDeviceId,
    videoProfileType: (state: SettingState) => state.videoProfileType,
    micList: (state: SettingState) => {
      const { sysDefaultMicId, micList } = state;
      if (
        !sysDefaultMicId ||
        micList.length === 0 ||
        micList[0].deviceId !== 'default'
      ) {
        return micList;
      }
      const mic = micList.find(
        (item: any) => item.deviceId === sysDefaultMicId
      );
      if (mic) {
        return micList.map((item: any, i: number) =>
          i === 0
            ? {
                ...item,
                label: `${item.label} - ${mic.label}`,
              }
            : item
        );
      }
      return micList;
    },
    speakerList: (state: SettingState) => {
      const { sysDefaultSpeakerId, speakerList } = state;
      if (
        !sysDefaultSpeakerId ||
        speakerList.length === 0 ||
        speakerList[0].deviceId !== 'default'
      ) {
        return speakerList;
      }
      const speaker = speakerList.find(
        (item: any) => item.deviceId === sysDefaultSpeakerId
      );
      if (speaker) {
        return speakerList.map((item: any, i: number) =>
          i === 0
            ? {
                ...item,
                label: `${item.label} - ${speaker.label}`,
              }
            : item
        );
      }
      return speakerList;
    },
    screenShareMode: (state: SettingState) => state.screenShareMode,
  },
  mutations: {
    [mutations.SET_SCREEN_SHARE_MODE](
      state: SettingState,
      screenShareMode: number
    ) {
      state.screenShareMode = screenShareMode;
    },
    [mutations.UPDATE_SETTINGS](
      state: SettingState,
      payload: {
        envSettingVisible?: boolean;
        enableAAGC?: boolean;
        recordDeviceId?: string;
        playoutDeviceId?: string;
        captureDeviceId?: string;
        videoProfileType?: string;
        videoFrameRate?: number;
      }
    ) {
      if (hasProperty(payload, 'envSettingVisible')) {
        state.envSettingVisible = payload.envSettingVisible!;
      }
      if (hasProperty(payload, 'enableAAGC')) {
        state.enableAAGC = payload.enableAAGC!;
      }
      if (hasProperty(payload, 'recordDeviceId')) {
        state.recordDeviceId = payload.recordDeviceId!;
        localStorage.setItem(localCacheKeySelectedMic, state.recordDeviceId);
      }
      if (hasProperty(payload, 'playoutDeviceId')) {
        state.playoutDeviceId = payload.playoutDeviceId!;
        localStorage.setItem(
          localCacheKeySelectedSpeaker,
          state.playoutDeviceId
        );
      }
      if (hasProperty(payload, 'captureDeviceId')) {
        state.captureDeviceId = payload.captureDeviceId!;
        localStorage.setItem(localCacheKeySelectedCam, state.captureDeviceId);
      }
      if (hasProperty(payload, 'videoProfileType')) {
        state.videoProfileType = payload.videoProfileType!;
        localStorage.setItem(
          localCacheKeyVideoProfileType,
          state.videoProfileType
        );
      }
      if (hasProperty(payload, 'videoFrameRate')) {
        state.videoFrameRate = payload.videoFrameRate!;
        localStorage.setItem(
          localCacheKeyVideoFrameRate,
          `${state.videoFrameRate}`
        );
      }
    },
    setMicList(
      state: SettingState,
      payload: { deviceList: any[]; selectedId?: any; micAllowed?: any }
    ) {
      state.micList = payload.deviceList;
      state.recordDeviceId = payload.selectedId;
      state.micAllowed = payload.micAllowed;
      state.audioDeviceReady = true;
    },
    setSpeakerList(
      state: SettingState,
      payload: { deviceList: any[]; selectedId?: any }
    ) {
      state.speakerList = payload.deviceList;
      state.playoutDeviceId = payload.selectedId;
    },
    setCameraList(
      state: SettingState,
      payload: { deviceList: any[]; selectedId?: any; cameraAllowed?: any }
    ) {
      state.cameraList = payload.deviceList;
      state.captureDeviceId = payload.selectedId;
      state.cameraAllowed = payload.cameraAllowed;
      state.videoDeviceReady = true;
    },
    setSysDefaultMicId(state: SettingState, playout: any) {
      state.sysDefaultMicId = playout;
    },
    setSysDefaultSpeakerId(state: SettingState, playout: any) {
      state.sysDefaultSpeakerId = playout;
    },
    [mutations.RESET_SETTING_STORE](state: SettingState) {
      // 只需要重置需要的属性
      state.screenShareMode = 0;
    },
  },
};
