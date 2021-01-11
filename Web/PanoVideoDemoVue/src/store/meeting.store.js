import * as Constants from '../constants';

let countdownInterval;

function getQueryValue(queryName) {
  const query = decodeURI(window.location.href.split('?')[1]);
  const vars = query.split('&');
  for (let i = 0; i < vars.length; i++) {
    const pair = vars[i].split('=');
    if (pair[0] === queryName) {
      return pair[1];
    }
  }
  return null;
}

function getInitialState() {
  return {
    channelId:
      getQueryValue('channelId') ||
      localStorage.getItem(Constants.localCacheKeyChannelId) ||
      `${Math.random()}`.substring(2, 7),
    meetingStatus: '',
    videoPorfile:
      localStorage.getItem(Constants.localCacheKeyVideoProfileType) ||
      Constants.VideoProfileType.HD720P, // 自己的分辨率
    meetingEndReason: '',
    whiteboardAvailable: false, // 白板是否已经连接
    isWhiteboardOpen: false,
    whiteboardUpdated: false, // 关闭白板的状态下收到新的内容更新，显示红点提示
    remainSeconds: -1,
    micId:
      localStorage.getItem(Constants.localCacheKeySelectedMic) || 'default', // 麦克风
    speakerId:
      localStorage.getItem(Constants.localCacheKeySelectedSpeaker) || 'default', // 扬声器
    cameraId: localStorage.getItem(Constants.localCacheKeySelectedCam), // 摄像头
    settingVisible: false
  };
}

export default {
  state: getInitialState(),
  mutations: {
    setWhiteboardAvailable(state, whiteboardAvailable) {
      state.whiteboardAvailable = whiteboardAvailable;
    },
    setSettingVisible(state, settingVisible) {
      state.settingVisible = settingVisible;
    },
    updateChannelId(state, channelId) {
      state.channelId = channelId;
    },
    setMeetingStatus(state, status) {
      state.meetingStatus = status;
    },
    setmeetingEndReason(state, payload) {
      state.meetingEndReason = payload;
    },
    setRemainSeconds(state, remainSeconds) {
      state.remainSeconds = remainSeconds;
    },
    setWhiteboardOpenState(state, isWhiteboardOpen) {
      state.isWhiteboardOpen = isWhiteboardOpen;
      if (isWhiteboardOpen) {
        // 如果打开白板，清空白板更新状态
        state.whiteboardUpdated = false;
      }
    },
    setWhiteboardUpdatedState(state, wbUpdatedState) {
      state.whiteboardUpdated = wbUpdatedState;
    },
    setMic(state, id) {
      state.micId = id;
      localStorage.setItem(Constants.localCacheKeySelectedMic, id);
    },
    setSpeaker(state, id) {
      state.speakerId = id;
      localStorage.setItem(Constants.localCacheKeySelectedSpeaker, id);
    },
    setCamera(state, id) {
      state.cameraId = id;
      localStorage.setItem(Constants.localCacheKeySelectedCam, id);
    },
    setVideoProfile(state, videoProfileType) {
      state.videoPorfile = videoProfileType;
      localStorage.setItem(
        Constants.localCacheKeyVideoProfileType,
        videoProfileType
      );
    },
    resetMeetingStore(state) {
      Object.assign(state, getInitialState());
      clearInterval(countdownInterval);
    },
    beginCountdown(state, remainSeconds) {
      state.remainSeconds = remainSeconds;
      clearInterval(countdownInterval);
      countdownInterval = setInterval(() => {
        state.remainSeconds = state.remainSeconds - 1;
        if (state.remainSeconds <= 0) {
          state.meetingStatus = 'countdownover';
          clearInterval(countdownInterval);
        }
      }, 1000);
    }
  },
  actions: {},
  getters: {
    micId: state => state.micId,
    speakerId: state => state.speakerId,
    cameraId: state => state.cameraId,
    channelId: state => state.channelId,
    isWhiteboardOpen: state => state.isWhiteboardOpen,
    meetingStatus: state => state.meetingStatus,
    meetingEndReason: state => state.meetingEndReason,
    remainSeconds: state => state.remainSeconds,
    videoPorfile: state => state.videoPorfile,
    whiteboardUpdated: state => state.whiteboardUpdated,
    whiteboardAvailable: state => state.whiteboardAvailable,
    settingVisible: state => state.settingVisible
  }
};
