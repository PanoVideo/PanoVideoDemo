import PanoRtc from '@pano.video/panortc';
import { includes } from 'lodash-es';
import store from '../store';
import {
  VideoProfileType,
  subscribeVideoQuota,
  MOMENT_FOR_UNSUBSCRIBE
} from '../constants';
import { MessageBox, Message } from 'element-ui';
import { HmacSHA256, enc } from 'crypto-js';

// 初始化 panortc
// 全局单例
const rtcEngine = new PanoRtc.RtcEngine();
window.rtcEngine = rtcEngine;

/**
 * 获取用户合适的订阅视频分辨率，主视图用户720p，其他180P
 * @param user
 */
function getMatchingVideoProfile(user) {
  return user.showInMainView && !user.isScreenInMainView
    ? VideoProfileType.HD720P
    : VideoProfileType.Low;
}

function subSlotFull() {
  let usedSubSlot = 0;
  store.getters.userList.forEach(u => {
    if (u.lastMomentSubscribeVideo !== 0) {
      usedSubSlot++;
    }
    if (u.lastMomentSubscribeScreen !== 0) {
      usedSubSlot++;
    }
  });
  return usedSubSlot >= subscribeVideoQuota;
}

/**
 * 订阅用户视频，如果已经订阅了不会再订阅
 * @param user
 * @param raceful
 */
function subscribeUserVideo(user, raceful = false) {
  if (!user || user.videoMuted || user.userId === store.getters.userMe.userId)
    return false;
  if (
    getMatchingVideoProfile(user) === user.videoProfileType &&
    user.videoDomRef.getElementsByTagName('video').length
  ) {
    console.log('same profile video already subed, will not sub again.');
    return true;
  }
  if (raceful) {
    /**
     * 是否占用了订阅的槽位以发起订阅为准，假设订阅之后的video/screen一定能成功
     */
    if (user.lastMomentSubscribeVideo === 0 && subSlotFull()) {
      forceMakeOneSubSlot();
    }
  }
  const oldLastMoment = user.lastMomentSubscribeVideo;
  user.lastMomentSubscribeVideo = new Date().getTime();
  user.videoProfileType = getMatchingVideoProfile(user);
  console.log(
    `subscribeVideo of ${user.userName}, profile ${user.videoProfileType}`
  );
  const qresult = rtcEngine.subscribeVideo({
    userId: user.userId,
    quality: user.videoProfileType,
    userName: user.userName
  });
  console.log('qresult', qresult, {
    userId: user.userId,
    quality: user.videoProfileType,
    userName: user.userName
  });
  if (qresult !== 0 && qresult.code !== 'OK') {
    user.lastMomentSubscribeVideo = oldLastMoment;
    return false;
  }
  return true;
}

/**
 * 如果video订阅的slot已满，放弃订阅当前user
 * @param user
 * @returns 是否订阅成功
 */
export function trySubscribeUserVideo(user) {
  return subscribeUserVideo(user, false);
}

/**
 * 如果video订阅的slot已满，选择其中一个取消，然后订阅当前user
 * @param user
 * @returns 是否订阅成功
 */
export function forceSubscribeUserVideo(user) {
  return subscribeUserVideo(user, true);
}

export function subscribeUserScreen(user, raceful = false) {
  if (user.userId === store.getters.userMe.userId) return;
  if (raceful) {
    /**
     * 是否占用了订阅的槽位以发起订阅为准，假设订阅之后的video/screen一定能成功
     */
    if (
      user.lastMomentSubscribeScreen === MOMENT_FOR_UNSUBSCRIBE &&
      subSlotFull()
    ) {
      console.log('subslot is full to subscreen, force make one');
      forceMakeOneSubSlot();
    } else {
      console.log('subslot not full to subscreen');
    }
  }
  const oldLastMoment = user.lastMomentSubscribeScreen;
  user.lastMomentSubscribeScreen = new Date().getTime();
  const qresult = rtcEngine.subscribeScreen({
    userId: user.userId,
    quality: VideoProfileType.HD720P
  });
  console.log(
    `pvc: subscribescreen result=${qresult} of ${user.userName}@${user.userId}`
  );
  if (qresult !== 0 && qresult.code !== 'OK') {
    user.lastMomentSubscribeScreen = oldLastMoment;
  }
}

function forceMakeOneSubSlot() {
  const userVideoSlots = store.getters.userList.filter(u => {
    return u.lastMomentSubscribeVideo !== MOMENT_FOR_UNSUBSCRIBE;
  });
  if (userVideoSlots.length) {
    const sortedUserSlots = userVideoSlots.sort(
      (x, y) => x.lastMomentAsMainView - y.lastMomentAsMainView
    );
    sortedUserSlots[0].videoDomRef.innerHTML = '';
    sortedUserSlots[0].lastMomentSubscribeVideo = MOMENT_FOR_UNSUBSCRIBE;
    rtcEngine.unsubscribeVideo(sortedUserSlots[0].userId);
    return;
  }

  const userScreenSlots = store.getters.userList.filter(u => {
    return u.lastMomentSubscribeScreen !== MOMENT_FOR_UNSUBSCRIBE;
  });
  if (userScreenSlots.length) {
    const sortedUserSlots = userScreenSlots.sort(
      (x, y) => x.lastMomentAsMainView - y.lastMomentAsMainView
    );
    sortedUserSlots[0].screenDomRef.innerHTML = '';
    sortedUserSlots[0].lastMomentSubscribeScreen = MOMENT_FOR_UNSUBSCRIBE;
    rtcEngine.unsubscribeScreen(sortedUserSlots[0].userId);
  }
}

/**
 * 把 webrtc 返回的 video 标签插入到准备好的dom中
 * @param {HTMLVideoElement} videoTag
 * @param {HTMLElement} wrapper
 */
function insertVideoTagToDom(videoTag, wrapper) {
  videoTag.setAttribute(
    'style',
    'width: 100%; height: 100%; margin: auto; display: block; object-fit: contain;'
  );
  wrapper.style.width = '100%';
  wrapper.style.height = '100%';
  wrapper.style.display = 'flex';
  wrapper.style.justifyContent = 'center';
  wrapper.style.alignItems = 'center';
  wrapper.innerHTML = '';
  if (
    wrapper.firstChild === videoTag &&
    wrapper.firstChild.srcObject === videoTag.srcObject
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
  /**
   * 接收到别人的视频回调
   */
  rtcEngine.on(PanoRtc.RtcEngine.Events.userVideoReceived, data => {
    console.log('接收到别人的视频回调', data);
    if (data.data.videoTag) {
      data.data.videoTag.srcObject = data.data.srcObject;
      const userInfo = store.getters.getUserById(data.data.userId);
      if (userInfo) {
        insertVideoTagToDom(data.data.videoTag, userInfo.videoDomRef);
      }
    }
  });

  // 接收到别人的桌面共享回调
  rtcEngine.on(PanoRtc.RtcEngine.Events.userScreenReceived, data => {
    console.log('接收到别人的桌面共享回调', data);
    if (data.data.videoTag) {
      data.data.videoTag.srcObject = data.data.srcObject;
      const userInfo = store.getters.getUserById(data.data.userId);
      if (userInfo) {
        insertVideoTagToDom(data.data.videoTag, userInfo.screenDomRef);
      }
    }
  });

  // startVideo 打开自己的视频回调
  rtcEngine.on(PanoRtc.RtcEngine.Events.getLocalVideo, data => {
    console.log('startVideo 打开自己的视频回调', data);
    if (data.data.videoTag) {
      insertVideoTagToDom(data.data.videoTag, store.getters.userMe.videoDomRef);
    }
  });

  // 其他用户入会事件
  rtcEngine.on(PanoRtc.RtcEngine.Events.userJoin, data => {
    console.log('其他用户入会事件', data);
    store.commit('addUser', data.user);
    store.dispatch(
      'trySelectMainView',
      store.getters.getUserById(data.user.userId)
    );
    if (store.getters.wbAdminUser === store.getters.userMe) {
      window.rtcWhiteboard &&
        window.rtcWhiteboard.sendMessage(data.user.userId, {
          wbHostId: store.getters.userMe.userId
        });
    }
  });

  // 离会通知
  rtcEngine.on(PanoRtc.RtcEngine.Events.leaveChannelIndication, result => {
    console.log('channelLeaveIndication', result);
    if (store.getters.meetingStatus !== 'countdownover') {
      // const reasons = [
      //   'Kicked Out',
      //   'Force Failover',
      //   'Meeting Ended',
      //   'Login Elsewhere'
      // ];
      const reasons = [
        '已被踢出会议',
        '服务端强制重启会议',
        '会议结束',
        '已在其他地方登录'
      ];
      store.commit('setmeetingEndReason', reasons[result.reasonCode]);
      store.commit('setMeetingStatus', 'ended');
    }
  });

  // 倒计时
  rtcEngine.on(PanoRtc.RtcEngine.Events.channelCountDown, data => {
    console.log(`channelCountDown, remain: ${data.remainsec}`);
    store.commit('beginCountdown', data.remainsec);
  });

  // 用户离开通知
  rtcEngine.on(PanoRtc.RtcEngine.Events.userLeave, data => {
    const user = store.getters.getUserById(data.userId);
    if (user) {
      if (user.showInMainView) {
        store.dispatch('selectMainViewUser', { exceptUserId: data.userId });
      }
      store.commit('removeUser', data.userId);
    }
  });

  // video start
  rtcEngine.on(PanoRtc.RtcEngine.Events.userVideoStart, data => {
    const { userId } = data;
    console.log(`onUserVideoStart, userId: ${userId}`);
    const user = store.getters.getUserById(userId);
    if (user) {
      store.commit('updateUser', { userId, videoMuted: false });
      if (trySubscribeUserVideo(user)) {
        store.dispatch('trySelectMainView', { user });
      }
    }
  });

  // video stop
  rtcEngine.on(PanoRtc.RtcEngine.Events.userVideoStop, data => {
    const { userId } = data;
    console.log(`onUserVideoStop, userId: ${userId}`);
    const user = store.getters.getUserById(userId);
    if (user) {
      store.commit('updateUser', { userId, videoMuted: true });
      rtcEngine.unsubscribeVideo({ userId });
      user.videoDomRef.innerHTML = '';
    }
  });

  // audio start
  rtcEngine.on(PanoRtc.RtcEngine.Events.userAudioStart, data => {
    const { userId } = data;
    console.log(`onUserAudioStart, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: false });
  });

  // audio stop
  rtcEngine.on(PanoRtc.RtcEngine.Events.userAudioStop, data => {
    const { userId } = data;
    console.log(`onUserAudioStop, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: true });
  });

  // audio mute
  rtcEngine.on(PanoRtc.RtcEngine.Events.userAudioMute, data => {
    const { userId } = data;
    console.log(`userAudioMute, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: true });
  });

  // audio unmute
  rtcEngine.on(PanoRtc.RtcEngine.Events.userAudioUnmute, data => {
    const { userId } = data;
    console.log(`userAudioUnmute, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: false });
  });

  // 别的用户开启桌面共享
  rtcEngine.on(PanoRtc.RtcEngine.Events.userScreenStart, data => {
    const { userId } = data;
    console.log(`userScreenStart, userId: ${userId}`);
    const user = store.getters.getUserById(`${userId}`);
    if (user) {
      store.commit('updateUser', { userId, screenOpen: true });
      subscribeUserScreen(user, true);
      store.dispatch('trySelectMainView', {
        user,
        isScreen: true
      });
      store.commit('setWhiteboardOpenState', false);
    }
  });

  // 别的用户停止视频共享
  rtcEngine.on(PanoRtc.RtcEngine.Events.userScreenStop, data => {
    const { userId } = data;
    console.log(`userScreenStop, userId: ${userId}`);
    const user = store.getters.getUserById(`${userId}`);
    if (user) {
      store.commit('updateUser', {
        userId,
        screenOpen: false,
        isScreenInMainView: false,
        lastMomentSubscribeScreen: MOMENT_FOR_UNSUBSCRIBE
      });
      rtcEngine.unsubscribeScreen({ userId });
      if (user.showInMainView && user.isScreenInMainView) {
        store.dispatch('trySelectMainView');
      }
      user.screenDomRef.innerHTML = '';
    }
  });

  // 本地桌面共享结束
  rtcEngine.on(PanoRtc.RtcEngine.Events.localScreenEnded, () => {
    console.log('onLocalScreenEnded');
    store.commit('updateUserMe', { screenOpen: false });
  });

  // failover 重连事件
  rtcEngine.on(PanoRtc.RtcEngine.Events.channelFailover, data => {
    console.log('onChannelFailover', data);
    switch (data.state) {
      case 'Reconnecting':
        store.commit('setMeetingStatus', 'reconnecting');
        break;
      case 'Success':
        store.commit('setMeetingStatus', 'connected');
        break;
      case 'Failed':
        store.commit('setMeetingStatus', 'disconnected');
        break;
      default:
        store.commit('setMeetingStatus', 'ended');
    }
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.activeSpeakerListUpdate, data => {
    store.getters.allUsers.forEach(user => {
      store.commit('updateUser', {
        userId: user.userId,
        isSpeaking: includes(data.list, user.userId)
      });
    });
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.audioDeviceChange, () => {
    // 如果当前使用的设备被拔出，那么选择默认的设备
    if (store.getters.micId !== 'default') {
      rtcEngine.getMics(devices => {
        if (!devices.find(item => item.deviceId === store.getters.micId)) {
          store.commit('setMic', 'default');
          rtcEngine.selectMic('default');
        }
      });
    }
    if (store.getters.speakerId !== 'default') {
      rtcEngine.getSpeakers(devices => {
        if (!devices.find(item => item.deviceId === store.getters.speakerId)) {
          store.commit('setSpeaker', 'default');
          rtcEngine.selectSpeaker('default');
        }
      });
    }
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.videoDeviceChange, () => {
    // 如果当前使用的设备被拔出，那么选择默认的设备
    rtcEngine.getCams(cameras => {
      if (!cameras.find(item => item.deviceId === store.getters.cameraId)) {
        if (cameras[0]) {
          // rtcEngine.stopPreview();
          store.commit('setCamera', cameras[0].deviceId);
          rtcEngine.selectCam(cameras[0].deviceId);
        }
      }
    });
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.getVideoMediaFailed, data => {
    showStartMediaFailureMessage('video', data);
    store.commit('updateUserMe', { videoMuted: true });
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.getAudioMediaFailed, data => {
    showStartMediaFailureMessage('audio', data);
    store.commit('updateUserMe', { audioMuted: true });
  });

  rtcEngine.on(PanoRtc.RtcEngine.Events.getScreenMediaFailed, data => {
    showStartMediaFailureMessage('screen', data);
    store.commit('updateUserMe', { screenOpen: false });
  });

  const onLocalScreenEnd = () => {
    console.log('本地桌面共享结束');
    store.commit('updateUser', {
      userId: store.getters.userMe.userId,
      screenOpen: false
    });
  };

  // 本地共享桌面结束
  rtcEngine.on(PanoRtc.RtcEngine.Events.getScreenMediaFailed, onLocalScreenEnd);
  rtcEngine.on(PanoRtc.RtcEngine.Events.localScreenEnded, onLocalScreenEnd);

  // 没有默认选择的 camera
  // rtcEngine.getCams(cameras => {
  //   if (!store.getters.cameraId) {
  //     store.commit('setCamera', get(cameras, '0.deviceId'));
  //   }
  //   rtcEngine.selectCam(store.getters.cameraId);
  // });
  // rtcEngine.selectCam(store.getters.cameraId);
  // rtcEngine.selectMic(store.getters.micId);
  // rtcEngine.selectSpeaker(store.getters.speakerId);

  // 设置用户音量回调
  let noAudioInputCount = 0;
  const intervalMs = 200;
  // 自己的音量回调
  rtcEngine.setRecordingAudioIndication(record => {
    //  record: { level: number, userId: string, active: boolean }
    if (record.active && record.level < 0.00001) {
      noAudioInputCount++;
    } else {
      noAudioInputCount = 0;
    }
    // 3s 内一直检测到音频输入音量很低即弹框提示用户麦克风异常
    if (noAudioInputCount === Math.floor(3000 / intervalMs)) {
      MessageBox.alert(
        '检测到您的音频输入音量很低，可能影响通话质量，请检查您的麦克风是否正常',
        '提示'
      );
    }
    store.commit('updateUserAudioLevel', {
      userId: record.userId,
      level: record.level
    });
  }, intervalMs);
  // 其他用户的音量回调
  rtcEngine.setUserAudioIndication(records => {
    // records: { level: number, userId: string, active: boolean }[]
    records.forEach(r => {
      store.commit('updateUserAudioLevel', {
        userId: r.userId,
        level: r.level
      });
    });
  }, intervalMs);
}

export async function updateCaptureDeviceId() {
  if (!store.getters.cameraId) {
    return;
  }
  await rtcEngine.getCams(cameras => {
    if (!cameras.find(item => item.deviceId === store.getters.cameraId)) {
      if (cameras[0]) {
        store.commit('setCamera', cameras[0].deviceId);
      }
    }
    if (store.getters.cameraId) {
      rtcEngine.selectCam(store.getters.cameraId);
    }
  });
}

async function getCaptureDeviceList() {
  return new Promise((resolve, reject) => {
    rtcEngine.getCams(
      devices =>
        resolve(
          devices.map(d => ({ deviceName: d.label, deviceId: d.deviceId }))
        ),
      error => {
        console.log('getCaptureDeviceList failed', error);
        reject(error);
      }
    );
  });
}

async function getRecordDeviceList() {
  return new Promise((resolve, reject) => {
    window.rtcEngine.getMics(
      devices =>
        resolve(
          devices.map(d => ({ deviceName: d.label, deviceId: d.deviceId }))
        ),
      error => {
        console.log('getRecordDeviceList failed', error);
        reject(error);
      }
    );
  });
}

export async function enableOpenVideo() {
  const captureDeviceList = await getCaptureDeviceList();
  return captureDeviceList.length > 0;
}

export async function enableOpenAudio() {
  const recordDeviceList = await getRecordDeviceList();
  return recordDeviceList.length > 0;
}

function showStartMediaFailureMessage(type, data) {
  let device = '麦克风';
  if (type !== 'audio') {
    device = type === 'video' ? '摄像头' : '共享屏幕';
  }
  if (data.errorName === 'NotAllowedError') {
    Message.info(`没有权限使用${device}`);
  } else if (data.errorName === 'NotFoundError') {
    Message.info(`没有检测到${device}`);
  } else if (data.errorName === 'NotReadableError') {
    Message.info(`${device}被占用，无法访问`);
  } else if (type === 'audio' || type === 'video') {
    Message.info(`开启${device}失败`);
  }
}

export const genToken = (appId, appSecret, channelId, userId) => {
  console.error(
    'You must not use use appSecret in frontend code in product environment.'
  );
  console.error(
    'WARNING: 本项目只是为了简单演示 rtc 能力，在前端代码使用 appSecret。生产环境不应在前端代码直接使用appSecret'
  );
  const version = '02';
  const timestamp = Math.floor(Date.now() / 1000);
  const params = enc.Base64.stringify(
    enc.Utf8.parse(
      JSON.stringify({
        channelId,
        userId,
        channelDuration: 0,
        privileges: 0,
        duration: 86400,
        size: 25,
        delayClose: 60
      })
    )
  );
  const signContent = `${version}${appId}${timestamp}${params}`;
  const signatureValue = enc.Base64.stringify(
    HmacSHA256(signContent, appSecret)
  );
  return `${version}.${appId}.${timestamp}.${params}.${signatureValue}`;
};
