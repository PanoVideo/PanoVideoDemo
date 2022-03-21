import { RtcWhiteboard, RtsService } from '@pano.video/panorts';
import store from '@/store';
import { MediaType } from '@/store/modules/user';
import * as mutations from '@/store/mutations';
import * as actions from '@/store/actions';
import { LogUtil } from '@/utils';
import { message } from 'ant-design-vue';
import { some } from 'lodash-es';
import { localCacheKeyPlayoutVolume } from '@/constants';
import { info } from '@/utils/common';
import { RtsDelegate } from '@pano.video/panorts';

// 记录用户音频强度
window.userAudioLevel = {};

/**
 * promisefy rtcEngine.joinChannel
 * @param channelParam
 * @param optionalConfig
 * @returns
 */
export function rtcEngineJoinChannel(
  channelParam: {
    appId: string;
    channelId: string;
    userId: string;
    token: string;
    userName: string;
    subscribeAudioAll: boolean;
    attendeeId: string;
  },
  optionalConfig?: any
): Promise<void> {
  return new Promise((resolve, reject) => {
    const result = window.rtcEngine.joinChannel(channelParam, optionalConfig);
    if (result.code !== window.QResult.OK) {
      reject(getLeaveChannelReason(result.code));
      return;
    }
    window.rtcEngine.once(
      window.RtcEngine.Events.joinChannelConfirm,
      (data: {
        message: string;
        result: 'failed' | 'success';
        code: string;
      }) => {
        LogUtil('joinChannelCallback', data);
        if (data.result === 'success') {
          window.rtcEngine.setRtsDelegate(new RtsDelegate());
          resolve();
        } else {
          reject(getLeaveChannelReason(data.code));
        }
      }
    );
  });
}

function insertVideoTagToDom(videoTag: HTMLVideoElement, wrapper: HTMLElement) {
  videoTag.autoplay = true;
  videoTag.setAttribute(
    'style',
    `${videoTag.getAttribute(
      'style'
    )}width: 100%; height: 100%; margin: auto; display: block; object-fit: contain;`
  );
  wrapper.style.width = '100%';
  wrapper.style.height = '100%';
  wrapper.style.display = 'flex';
  wrapper.style.justifyContent = 'center';
  wrapper.style.alignItems = 'center';

  if (
    wrapper.firstChild === videoTag &&
    (wrapper.firstChild as HTMLVideoElement).srcObject === videoTag.srcObject
  ) {
    videoTag.play && videoTag.play();
  } else {
    videoTag.play && videoTag.play();
    wrapper.innerHTML = '';
    wrapper.appendChild(videoTag);
  }
}

/**
 * 初始化pano rtc相关逻辑，pano rtc 包括音、视频和桌面共享功能
 */
export default function initPanoRtc() {
  const { rtcEngine, RtcEngine } = window;

  RtcEngine.setServer(`https://api.${store.getters.panoServer}`);
  RtsService.setServer(`https://api.${store.getters.panoServer}`);

  // user join event
  rtcEngine.on(RtcEngine.Events.userJoin, (data: any) => {
    LogUtil('user join channel', data);
    store.dispatch(actions.ADD_USER, data.user);
  });

  // rtcEngine leave channel event
  rtcEngine.on(
    RtcEngine.Events.leaveChannelIndication,
    (result: { reason: string }) => {
      LogUtil('channelLeaveIndication', result);
      if (store.getters.meetingStatus !== 'countdownover') {
        store.commit(mutations.SET_MEETING_STATUS, {
          meetingEndReason: result.reason,
          meetingStatus: 'ended',
        });
      }
      window.rtcEngine.setRtsDelegate(null);
    }
  );

  // channel countdownover event
  rtcEngine.on(RtcEngine.Events.channelCountDown, (data: any) => {
    LogUtil('channel countdown remains', data.remainsec);
    store.commit(mutations.BEGIN_COUNTDOWN, { remainSeconds: data.remainsec });
  });

  // user leave event
  rtcEngine.on(RtcEngine.Events.userLeave, (data: any) => {
    LogUtil('onUserLeaveIndication', data);
    const user = store.getters.getUserById(data.userId);
    if (user) {
      store.dispatch(actions.REMOVE_USER, { userId: data.userId });
    }
  });

  // video start
  rtcEngine.on(RtcEngine.Events.userVideoStart, (data: any) => {
    const { userId } = data;
    LogUtil('onUserVideoStart', userId);
    store.commit(mutations.UPDATE_USER, { userId, videoMuted: false });
  });

  // video stop
  rtcEngine.on(RtcEngine.Events.userVideoStop, (data: any) => {
    const { userId } = data;
    LogUtil(`userVideoStop, userId: ${userId}`);
    const user = store.getters.getUserById(userId);
    if (user) {
      store.commit(mutations.UPDATE_USER, { userId, videoMuted: true });
      user.videoDomRef.innerHTML = '';
    }
  });

  // audio start
  rtcEngine.on(RtcEngine.Events.userAudioStart, (data: any) => {
    const { userId } = data;
    store.commit(mutations.UPDATE_USER, { userId, audioMuted: false });
  });

  // audio stop
  rtcEngine.on(RtcEngine.Events.userAudioStop, (data: any) => {
    const { userId } = data;
    store.commit(mutations.UPDATE_USER, { userId, audioMuted: true });
  });

  // audio mute
  rtcEngine.on(RtcEngine.Events.userAudioMute, (data: any) => {
    const { userId } = data;
    store.commit(mutations.UPDATE_USER, { userId, audioMuted: true });
  });

  // audio unmute
  rtcEngine.on(RtcEngine.Events.userAudioUnmute, (data: any) => {
    const { userId } = data;
    store.commit(mutations.UPDATE_USER, { userId, audioMuted: false });
  });

  rtcEngine.on(RtcEngine.Events.userAudioCallTypeChanged, (data: any) => {
    const userId = data.userId;
    const type = data.type;
    const audioType = type === 0 ? 'voip' : 'pstn';
    LogUtil('userAudioCallTypeChanged', userId, type);
    store.commit(mutations.UPDATE_USER, { userId, audioType });
  });

  // user video subscribe callback
  rtcEngine.on(RtcEngine.Events.userVideoReceived, (data: any) => {
    LogUtil('接收到别人的视频回调', data);
    if (data.data.videoTag) {
      data.data.videoTag.srcObject = data.data.srcObject;
      const userInfo = store.getters.getUserById(data.data.userId);
      if (userInfo) {
        insertVideoTagToDom(data.data.videoTag, userInfo.videoDomRef);
      }
    }
  });

  // user screen share subscribe callback
  rtcEngine.on(RtcEngine.Events.userScreenReceived, (data: any) => {
    LogUtil('接收到别人的桌面共享回调', data);
    if (data.data.videoTag) {
      data.data.videoTag.srcObject = data.data.srcObject;
      const userInfo = store.getters.getUserById(data.data.userId);
      if (userInfo) {
        insertVideoTagToDom(data.data.videoTag, userInfo.screenDomRef);
      }
    }
  });

  // start my video callback
  rtcEngine.on(RtcEngine.Events.getLocalVideo, (data: any) => {
    LogUtil('startVideo 打开自己的视频回调', data);
    if (data.data.videoTag) {
      insertVideoTagToDom(data.data.videoTag, store.getters.userMe.videoDomRef);
    }
  });

  // other user start screen share event
  rtcEngine.on(RtcEngine.Events.userScreenStart, (data: any) => {
    const { userId } = data;
    LogUtil(`userScreenStart, userId: ${userId}`);
    const user = store.getters.getUserById(`${userId}`);
    if (user) {
      store.commit(mutations.UPDATE_USER, { userId, screenOpen: true });
      if (store.getters.lockedUser) {
        store.commit(mutations.LOCK_USER, { userId: '' });
        message.info('已解除锁定');
      }
      if (store.getters.isUserAvailable(userId)) {
        if (store.getters.userMe.screenOpen) {
          store.dispatch(actions.STOP_SHARE_SCREEN);
          message.info('您的桌面共享已停止');
        }
      }
    }
  });

  // other user stop screen share event
  rtcEngine.on(RtcEngine.Events.userScreenStop, (data: any) => {
    const { userId } = data;
    LogUtil(`userScreenStop, userId: ${userId}`);
    const user = store.getters.getUserById(`${userId}`);
    if (user) {
      store.commit(mutations.UPDATE_USER, {
        userId,
        screenOpen: false,
        screenShareType: 'none',
      });
      store.dispatch(actions.UPDATE_PAGE_INDEX_ACTION);
      user.screenDomRef.innerHTML = '';
    }
  });

  // local screen share stop event
  rtcEngine.on(RtcEngine.Events.localScreenEnded, () => {
    LogUtil('onLocalScreenEnded');
    store.commit(mutations.UPDATE_USER_ME, { screenOpen: false });
  });

  // get local video media failed event
  rtcEngine.on(RtcEngine.Events.getVideoMediaFailed, (data: any) => {
    showStartMediaFailureMessage('video', data);
    store.commit(mutations.UPDATE_USER_ME, { videoMuted: true });
  });

  // get local audio media failed event
  rtcEngine.on(RtcEngine.Events.getAudioMediaFailed, (data: any) => {
    showStartMediaFailureMessage('audio', data);
    store.commit(mutations.UPDATE_USER_ME, { audioMuted: true });
  });

  // get local screen media source failed event
  rtcEngine.on(RtcEngine.Events.getScreenMediaFailed, (data: any) => {
    showStartMediaFailureMessage('screen', data);
    store.commit(mutations.UPDATE_USER_ME, { screenOpen: false });
  });

  rtcEngine.on(RtcEngine.Events.videoStartResult, (result: any) => {
    LogUtil('videoStartResult:', result);
    if (result !== 0) {
      showStartMediaFailureMessage('video', { errorName: '' });
      store.commit(mutations.UPDATE_USER_ME, { videoMuted: true });
    }
  });

  // rtcengine failover event
  rtcEngine.on(RtcEngine.Events.channelFailover, (data: any) => {
    LogUtil('RtcEngine onChannelFailover', data);
    let payload = {};
    switch (data.state) {
      case 'Reconnecting':
        payload = { meetingStatus: 'reconnecting' };
        break;
      case 'Success':
        payload = { meetingStatus: 'connected' };
        break;
      case 'Failed':
        payload = { meetingStatus: 'disconnected' };
        break;
      default:
        payload = { meetingStatus: 'ended' };
        break;
    }
    store.commit(mutations.SET_MEETING_STATUS, payload);
  });

  // rtcengine channel active speaker list
  rtcEngine.on(
    RtcEngine.Events.activeSpeakerListUpdate,
    (data: { list: string[] }) => {
      store.commit(mutations.UPDATE_ASL, data);
      const mostActiveUser = store.getters.mostActiveUser;
      if (!mostActiveUser) return;
      store.dispatch(actions.SORT_USERLIST_BY_ACTIVE_USER);
    }
  );

  rtcEngine.on(RtcEngine.Events.audioDeviceChange, getAudioDeviceList);

  rtcEngine.on(RtcEngine.Events.videoDeviceChange, getVideoDeviceList);

  // 设置用户音量回调
  let noAudioInputCount = 0;
  const intervalMs = 200;
  // 自己的音量回调
  rtcEngine.setRecordingAudioIndication(
    (record: { level: number; userId: string; active: boolean }) => {
      if (
        !store.getters.userMe.audioMuted &&
        record.active &&
        record.level < 0.00001
      ) {
        noAudioInputCount++;
      } else {
        noAudioInputCount = 0;
      }
      // 3s 内一直检测到音频输入音量很低即弹框提示用户麦克风异常
      if (noAudioInputCount === Math.floor(3000 / intervalMs)) {
        info(
          '检测到您的音频输入音量很低，可能影响通话质量，请检查您的麦克风是否正常',
          true
        );
      }
      window.userAudioLevel[record.userId] = record.level;
    },
    intervalMs
  );
  // 其他用户的音量回调
  rtcEngine.setUserAudioIndication(
    (records: { level: number; userId: string; active: boolean }[]) => {
      records.forEach((r) => {
        window.userAudioLevel[r.userId] = r.level;
      });
    },
    intervalMs
  );
  if (window.IS_ELECTRON) {
    window.rtcEngine.setVideoFrameRate(
      (store.state as any).settingStore.videoFrameRate
    );
    store.commit('setSysDefaultMicId', window.rtcEngine.getRecordDevice());
    store.commit('setSysDefaultSpeakerId', window.rtcEngine.getPlayoutDevice());
    window.rtcEngine.on(
      RtcEngine.Events.audioDefaultDeviceChanged,
      (deviceID, deviceType) => {
        if (deviceType === 1) {
          store.commit('setSysDefaultMicId', deviceID);
        } else if (deviceType === 2) {
          store.commit('setSysDefaultSpeakerId', deviceID);
        }
      }
    );
  } else {
    const playoutVolume = localStorage.getItem(localCacheKeyPlayoutVolume);
    if (playoutVolume) {
      window.rtcEngine.setAudioPlayoutVolume(parseFloat(playoutVolume));
    }
  }
}

function getAudioDeviceList() {
  window.rtcEngine.getMics(
    (deviceList: any[]) => {
      let selectedId: any;
      if (deviceList.length !== 0) {
        if (window.IS_ELECTRON) {
          deviceList = [{ deviceId: 'default', label: '系统默认' }].concat(
            deviceList
          );
        }
        selectedId = (store.state as any).settingStore.recordDeviceId;
        if (!deviceList.some((device) => device.deviceId === selectedId)) {
          selectedId = deviceList[0].deviceId;
          window.rtcEngine.selectMic(selectedId);
        }
      }
      store.commit('setMicList', {
        deviceList,
        selectedId,
        micAllowed: true,
      });
    },
    (error: any) => {
      store.commit('setMicList', {
        deviceList: [],
        selectedId: undefined,
        micAllowed: error.errorName === 'NotAllowedError' ? false : true,
      });
      console.log('getRecordDeviceList failed', error);
    }
  );
  window.rtcEngine.getSpeakers(
    (deviceList: any[]) => {
      let selectedId: any;
      if (deviceList.length !== 0) {
        if (window.IS_ELECTRON) {
          deviceList = [{ deviceId: 'default', label: '系统默认' }].concat(
            deviceList
          );
        }
        selectedId = (store.state as any).settingStore.playoutDeviceId;
        if (!deviceList.some((device) => device.deviceId === selectedId)) {
          selectedId = deviceList[0].deviceId;
          window.rtcEngine.selectSpeaker(selectedId);
        }
      }
      store.commit('setSpeakerList', {
        deviceList,
        selectedId,
      });
    },
    (error: any) => {
      store.commit('setSpeakerList', {
        deviceList: [],
        selectedId: undefined,
      });
      console.log('getRecordDeviceList failed', error);
    }
  );
}

function getVideoDeviceList() {
  window.rtcEngine.getCams(
    (deviceList: any[]) => {
      let selectedId: any = undefined;
      if (deviceList.length !== 0) {
        selectedId = (store.state as any).settingStore.captureDeviceId;
        if (!deviceList.some((device) => device.deviceId === selectedId)) {
          selectedId = deviceList[0].deviceId;
          window.rtcEngine.selectCam(selectedId);
        }
      }
      store.commit('setCameraList', {
        deviceList,
        selectedId,
        cameraAllowed: true,
      });
    },
    (error: any) => {
      store.commit('setCameraList', {
        deviceList: [],
        selectedId: undefined,
        cameraAllowed: error.errorName === 'NotAllowedError' ? false : true,
      });
      console.log('getCaptureDeviceList failed', error);
    }
  );
}

export function initAudioDeviceList() {
  if (!(store.state as any).audioDeviceReady) {
    getAudioDeviceList();
  }
}

export function initVideoDeviceList() {
  if (!(store.state as any).videoDeviceReady) {
    getVideoDeviceList();
  }
}

export async function updateCaptureDeviceId() {
  const deviceId = store.getters.captureDeviceId;
  if (!deviceId) {
    return;
  }
  const captureDeviceList: any[] = await getCaptureDeviceList();
  const has = some(captureDeviceList, (o: any) => o.deviceId === deviceId);
  if (has) {
    window.rtcEngine.selectCam(deviceId);
  } else {
    store.commit(mutations.UPDATE_SETTINGS, { captureDeviceId: '' });
  }
}

export function getCaptureDeviceList(): Promise<any[]> {
  return new Promise((resolve, reject) => {
    window.rtcEngine.getCams(
      (devices: any[]) =>
        resolve(
          devices.map((d) => ({ deviceName: d.label, deviceId: d.deviceId }))
        ),
      (error: any) => {
        console.log('getCaptureDeviceList failed', error);
        reject(error);
      }
    );
  });
}

export function getRecordDeviceList(): Promise<any[]> {
  return new Promise((resolve, reject) => {
    window.rtcEngine.getMics(
      (devices: any[]) =>
        resolve(
          devices.map((d) => ({ deviceName: d.label, deviceId: d.deviceId }))
        ),
      (error: any) => {
        console.log('getRecordDeviceList failed', error);
        reject(error);
      }
    );
  });
}
export function getPlayoutDeviceList(): Promise<any[]> {
  return new Promise((resolve, reject) => {
    window.rtcEngine.getSpeakers(
      (devices: any[]) =>
        resolve(
          devices.map((d) => ({ deviceName: d.label, deviceId: d.deviceId }))
        ),
      (error: any) => {
        console.log('getRecordDeviceList failed', error);
        reject(error);
      }
    );
  });
}

export async function enableOpenVideo() {
  const captureDeviceList: any[] = await getCaptureDeviceList();
  return captureDeviceList.length > 0;
}

export async function enableOpenAudio() {
  const recordDeviceList: any[] = await getRecordDeviceList();
  return recordDeviceList.length > 0;
}

export function getLeaveChannelReason(reason: any) {
  const { QResult } = window;
  switch (reason) {
    case QResult.AUTH_FAILED:
      return '认证失败';
    case QResult.USER_REJECTED:
      return '用户被拒绝';
    case QResult.USER_EXPELED:
      return '已被踢出会议';
    case QResult.USER_DUPLICATE:
      return '用户 ID 重复';
    case QResult.CHANNEL_CLOSED:
      return '频道被关闭';
    case QResult.CHANNEL_FULL:
      return '频道容量已满';
    case QResult.CHANNEL_LOCKED:
      return '频道被锁定';
    case QResult.CHANNEL_MODE:
      return '频道模式不匹配';
    case QResult.NETWORK_ERROR:
      return '出现网络错误';
    default:
      return `REASON_${reason}`;
  }
}

function showStartMediaFailureMessage(
  type: 'video' | 'audio' | 'screen',
  data: any
) {
  let device = '麦克风';
  if (type !== 'audio') {
    device = type === 'video' ? '摄像头' : '共享屏幕';
  }
  if (data.errorName === 'NotAllowedError') {
    message.error(`用户/系统已拒绝授权访问${device}`);
  } else if (data.errorName === 'NotFoundError') {
    message.error(`没有检测到${device}`);
  } else if (data.errorName === 'NotReadableError') {
    message.error(`${device}被占用，无法访问`);
  } else if (type === 'audio' || type === 'video') {
    message.error(`开启${device}失败`);
  }
  LogUtil('data:', data);
}

let audioDumpTimer: NodeJS.Timeout | undefined = undefined; // 音频dump计时器
/**
 * 开始音频Dump
 */
export function startAudioDump(description: string, contact: string) {
  if (window.IS_ELECTRON) {
    const fileName = `${store.getters.channelId}-${new Date().getTime()}.dump`;
    const path =
      (window.process as any).platform === 'darwin'
        ? `${window.process.env.TMPDIR!}${fileName}`
        : `${window.process.env.APPDATA!}\\${fileName}`;
    window.rtcEngine.startAudioDump(path, 200 * 1024 * 1024);
  }
  audioDumpTimer && clearTimeout(audioDumpTimer);
  audioDumpTimer = setTimeout(() => {
    window.rtcEngine.sendFeedback({
      contact,
      description,
      product: 'PanoVideoCall',
      type: '1',
      uploadLogs: true,
    });
    audioDumpTimer = undefined;
    if (window.IS_ELECTRON) {
      (window.rtcEngine as any).stopAudioDump();
      message.info('音频dump结束，将自动上传相关日志');
    }
  }, 1000 * 60);
}

/**
 * 停止音频Dump
 */
export function stopAudioDump() {
  if (window.IS_ELECTRON) {
    (window.rtcEngine as any).stopAudioDump();
  }
  audioDumpTimer && clearTimeout(audioDumpTimer);
  audioDumpTimer = undefined;
}

const unsubPendingMap = new Map<
  string,
  { video?: NodeJS.Timeout; screen?: NodeJS.Timeout }
>();

/**
 * 由于 subscribe 和 unsubscribe 可能发生在一前一后，导致 subscribe 被 unsubscribe 取消,
 * 因此 unsubscribe 时会 pending 一会，如果期间有重新 subscribe 则取消 unsubscribe
 * @param userId
 * @param type
 */
export function unsubscribe(userId: string, type: MediaType) {
  const unsubPending = unsubPendingMap.get(userId) || {};
  const user: UserInfo = store.getters.getUserById(userId);
  if (!user) {
    LogUtil('user not found, unsubscribe failed', userId);
    return;
  }
  LogUtil(`will unsubscribe ${user.userName} ${type}`, userId);
  const timeout = setTimeout(() => {
    if (type === MediaType.video) {
      window.rtcEngine.unsubscribeVideo({ userId });
      user.videoDomRef.innerHTML = '';
    } else {
      window.rtcEngine.unsubscribeScreen({ userId });
      user.screenDomRef.innerHTML = '';
    }
    LogUtil(`unsubscribe ${user.userName} ${type}`, userId);
  }, 500);
  if (type === MediaType.video) {
    unsubPending.video && clearTimeout(unsubPending.video);
    unsubPending.video = timeout;
  } else {
    unsubPending.screen && clearTimeout(unsubPending.screen);
    unsubPending.screen = timeout;
  }
  unsubPendingMap.set(userId, unsubPending);
}

/**
 * subscribe 某个用户的视频或桌面共享视频流，并清理 unsubscribe timeout;
 *
 * 获取用户合适的订阅视频分辨率，主视图用户1080p，其他180P
 * @param userId
 * @param type
 * @returns
 */
export function subscribe(userId: string, type: MediaType) {
  const unsubPending = unsubPendingMap.get(userId);
  const user: UserInfo = store.getters.getUserById(userId);
  if (!user) {
    LogUtil('user not found, subscribe failed');
    return;
  }
  const { VideoProfileType } = window;
  const mainViewUser = store.getters.mainViewUserData;
  const quality =
    !store.getters.isWhiteboardOpen &&
    mainViewUser &&
    mainViewUser.user.userId === user.userId &&
    mainViewUser.type === type
      ? VideoProfileType.HD1080P
      : VideoProfileType.Low;
  LogUtil('subscribe user', user.userName, type, quality, userId);
  if (type === MediaType.video) {
    window.rtcEngine.subscribeVideo({ userId, quality });
    if (unsubPending && unsubPending.video) {
      clearTimeout(unsubPending.video);
      unsubPending.video = undefined;
      LogUtil(`cancel unsubscribe ${user.userName} ${type}`, userId);
    }
  } else {
    window.rtcEngine.subscribeScreen({ userId, quality });
    if (unsubPending && unsubPending.screen) {
      clearTimeout(unsubPending.screen);
      unsubPending.screen = undefined;
      LogUtil(`cancel unsubscribe ${user.userName} ${type}`, userId);
    }
  }
}

declare global {
  interface Window {
    androidVersion: string;
    rtcWhiteboard: RtcWhiteboard;
    userAudioLevel: Record<string, number>;
  }
}
