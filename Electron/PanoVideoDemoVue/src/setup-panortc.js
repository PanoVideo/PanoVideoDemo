import {
  RtcEngine,
  VideoProfileType,
  QResult,
  FailoverState,
  VideoScalingMode
} from '@pano.video/panortc-electron-sdk';
import { RtcWhiteboard, Constants } from '@pano.video/whiteboard';
import { includes, get } from 'lodash-es';
import { Message } from 'element-ui';
import store from './store';
import { subscribeVideoQuota, MOMENT_FOR_UNSUBSCRIBE } from './constants';

// 初始化 panortc
const rtcEngine = new RtcEngine();

export const RtcEngineEvents = {
  channelJoinConfirm: 'channelJoinConfirm',
  userJoinIndication: 'userJoinIndication',
  channelLeaveIndication: 'channelLeaveIndication',
  channelCountDown: 'channelCountDown',
  userLeaveIndication: 'userLeaveIndication',
  userVideoStart: 'userVideoStart',
  userVideoStop: 'userVideoStop',
  userAudioStart: 'userAudioStart',
  userAudioStop: 'userAudioStop',
  userAudioMute: 'userAudioMute',
  userAudioUnmute: 'userAudioUnmute',
  userVideoMute: 'userVideoMute',
  userVideoUnmute: 'userVideoUnmute',
  userVideoReceived: 'userVideoReceived',
  channelFailover: 'channelFailover',
  activeSpeakerListUpdated: 'activeSpeakerListUpdated',
  audioDeviceStateChanged: 'audioDeviceStateChanged',
  videoDeviceStateChanged: 'videoDeviceStateChanged',
  userAudioSubscribe: 'userAudioSubscribe',
  userVideoSubscribe: 'userVideoSubscribe',
  userScreenStart: 'userScreenStart',
  userScreenStop: 'userScreenStop',
  userScreenSubscribe: 'userScreenSubscribe',
  userScreenMute: 'userScreenMute',
  userScreenUnmute: 'userScreenUnmute',
  userScreenReceived: 'userScreenReceived',
  getScreenMediaFailed: 'getScreenMediaFailed',
  localScreenEnded: 'localScreenEnded',
  whiteboardAvailable: 'whiteboardAvailable',
  whiteboardUnavailable: 'whiteboardUnavailable',
  whiteboardStart: 'whiteboardStart',
  whiteboardStop: 'whiteboardStop',
  firstAudioDataReceived: 'firstAudioDataReceived',
  firstVideoDataReceived: 'firstVideoDataReceived',
  firstScreenDataReceived: 'firstScreenDataReceived',
  audioDefaultDeviceChange: 'audioDefaultDeviceChange',
  audioMixingStateChanged: 'audioMixingStateChanged',
  videoSnapshotCompleted: 'videoSnapshotCompleted',
  networkQuality: 'networkQuality'
};

// 全局单例
window.rtcEngine = rtcEngine;

/**
 * 申请 admin 角色（演示权限）
 */
export function applyForWbAdmin() {
  console.log('applyForWbAdmin');
  return new Promise((resolve, reject) => {
    window.rtcWhiteboard.setRoleType(Constants.WBRoleType.Admin);
    let reqForAdminTimeout;
    const onRoleChanged = role => {
      console.log('onRoleChanged', role);
      clearTimeout(reqForAdminTimeout);
      if (role === Constants.WBRoleType.Admin) {
        window.rtcWhiteboard.broadcastMessage({
          wbHostId: store.getters.userMe.userId
        });
        resolve();
      } else {
        reject();
      }
      window.rtcWhiteboard.off(
        RtcWhiteboard.Events.userRoleTypeChanged,
        onRoleChanged
      );
    };
    // 2s 内获取不到权限提示失败
    reqForAdminTimeout = setTimeout(() => {
      window.rtcWhiteboard.off(
        RtcWhiteboard.Events.userRoleTypeChanged,
        onRoleChanged
      );
      reject();
      console.error('获取演示权限失败，请重试.');
    }, 2000);
    window.rtcWhiteboard.on(
      RtcWhiteboard.Events.userRoleTypeChanged,
      onRoleChanged
    );
  });
}

export function getLeaveChannelReason(reason) {
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
      return 'Unknown';
  }
}

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
  const qresult = rtcEngine.subscribeVideo(user.userId, user.videoDomRef, {
    profile: user.videoProfileType,
    mirror: false,
    scaling: VideoScalingMode.CropFill
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
  const qresult = rtcEngine.subscribeScreen(user.userId, user.screenDomRef);
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

export default function initPanoRtc() {
  // 其他用户入会事件
  rtcEngine.on(RtcEngineEvents.userJoinIndication, (userId, userName) => {
    console.log('其他用户入会事件', userId, userName);
    store.commit('addUser', { userId, userName });
    store.dispatch('trySelectMainView', store.getters.getUserById(userId));
    if (store.getters.wbAdminUser === store.getters.userMe) {
      window.rtcWhiteboard &&
        window.rtcWhiteboard.sendMessage(userId, {
          wbHostId: store.getters.userMe.userId
        });
    }
  });

  // 离会通知
  rtcEngine.on(RtcEngineEvents.channelLeaveIndication, result => {
    console.log('channelLeaveIndication', result);
    if (store.getters.meetingStatus !== 'countdownover') {
      store.commit('setmeetingEndReason', getLeaveChannelReason(result));
      store.commit('setMeetingStatus', 'ended');
    }
  });

  // 倒计时
  rtcEngine.on(RtcEngineEvents.channelCountDown, remain => {
    console.log(`channelCountDown, remain: ${remain}`);
    store.commit('beginCountdown', remain);
  });

  // 用户离开通知
  rtcEngine.on(RtcEngineEvents.userLeaveIndication, (userId, reason) => {
    console.log(`userLeaveIndication, userId: ${userId}, reason: ${reason}`);
    const user = store.getters.getUserById(userId);
    if (user) {
      store.commit('removeUser', userId);
      if (user.showInMainView) {
        store.dispatch('trySelectMainView');
      }
    }
  });

  // video start
  rtcEngine.on(RtcEngineEvents.userVideoStart, userId => {
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
  rtcEngine.on(RtcEngineEvents.userVideoStop, userId => {
    console.log(`onUserVideoStop, userId: ${userId}`);
    const user = store.getters.getUserById(userId);
    if (user) {
      store.commit('updateUser', { userId, videoMuted: true });
      rtcEngine.unsubscribeVideo(userId);
      user.videoDomRef.innerHTML = '';
    }
  });

  // audio start
  rtcEngine.on(RtcEngineEvents.userAudioStart, userId => {
    console.log(`onUserAudioStart, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: false });
  });

  // audio stop
  rtcEngine.on(RtcEngineEvents.userAudioStop, userId => {
    console.log(`onUserAudioStop, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: true });
  });

  // audio mute
  rtcEngine.on(RtcEngineEvents.userAudioMute, userId => {
    console.log(`userAudioMute, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: true });
  });

  // audio unmute
  rtcEngine.on(RtcEngineEvents.userAudioUnmute, userId => {
    console.log(`userAudioUnmute, userId: ${userId}`);
    store.commit('updateUser', { userId, audioMuted: false });
  });

  // 别的用户开启桌面共享
  rtcEngine.on(RtcEngineEvents.userScreenStart, userId => {
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
  rtcEngine.on(RtcEngineEvents.userScreenStop, userId => {
    console.log(`userScreenStop, userId: ${userId}`);
    const user = store.getters.getUserById(`${userId}`);
    if (user) {
      store.commit('updateUser', {
        userId,
        screenOpen: false,
        isScreenInMainView: false,
        lastMomentSubscribeScreen: MOMENT_FOR_UNSUBSCRIBE
      });
      rtcEngine.unsubscribeScreen(userId);
      if (user.showInMainView && user.isScreenInMainView) {
        store.dispatch('trySelectMainView');
      }
      user.screenDomRef.innerHTML = '';
    }
  });

  // 本地桌面共享结束
  rtcEngine.on(RtcEngineEvents.localScreenEnded, () => {
    console.log('onLocalScreenEnded');
    store.commit('updateUserMe', { screenOpen: false });
  });

  // failover 重连事件
  rtcEngine.on(RtcEngineEvents.channelFailover, state => {
    console.log('onChannelFailover', state);
    switch (state) {
      case FailoverState.Reconnecting:
        store.commit('setMeetingStatus', 'reconnecting');
        break;
      case FailoverState.Success:
        store.commit('setMeetingStatus', 'connected');
        break;
      case FailoverState.Failed:
        store.commit('setMeetingStatus', 'disconnected');
        break;
      default:
        store.commit('setMeetingStatus', 'ended');
    }
  });

  rtcEngine.on(RtcEngineEvents.activeSpeakerListUpdated, data => {
    store.getters.allUsers.forEach(user => {
      store.commit('updateUser', {
        userId: user.userId,
        isSpeaking: includes(data.list, user.userId)
      });
    });
  });

  rtcEngine.on(RtcEngineEvents.audioDeviceStateChanged, async () => {
    // 如果当前使用的设备被拔出，那么选择默认的设备
    if (store.getters.micId !== 'default') {
      const devices = await rtcEngine.audio.getRecordDeviceList();
      if (!devices.find(item => item.deviceId === store.getters.micId)) {
        store.commit('setMic', 'default');
        rtcEngine.audio.setRecordDevice('default');
      }
    }
    if (store.getters.speakerId !== 'default') {
      const devices = await rtcEngine.audio.getPlayoutDeviceList();
      if (!devices.find(item => item.deviceId === store.getters.speakerId)) {
        store.commit('setSpeaker', 'default');
        rtcEngine.audio.setPlayoutDevice('default');
      }
    }
  });

  rtcEngine.on(RtcEngineEvents.videoDeviceStateChanged, async () => {
    // 如果当前使用的设备被拔出，那么选择默认的设备
    const cameras = await rtcEngine.video.getCaptureDeviceList();
    if (!cameras.find(item => item.deviceId === store.getters.cameraId)) {
      if (cameras[0]) {
        rtcEngine.stopPreview(store.getters.cameraId);
        store.commit('setCamera', get(cameras, '0.deviceId'));
        rtcEngine.video.setCaptureDevice(store.getters.cameraId);
      }
    }
  });

  const onLocalScreenEnd = () => {
    console.log('本地桌面共享结束');
    store.commit('updateUser', {
      userId: store.getters.userMe.userId,
      screenOpen: false
    });
  };

  // 本地共享桌面结束
  rtcEngine.on(RtcEngineEvents.getScreenMediaFailed, onLocalScreenEnd);
  rtcEngine.on(RtcEngineEvents.localScreenEnded, onLocalScreenEnd);

  // 没有默认选择的 camera
  const cameras = rtcEngine.video.getCaptureDeviceList();
  if (!store.getters.cameraId) {
    store.commit('setCamera', get(cameras, '0.deviceId'));
  }
  rtcEngine.video.setCaptureDevice(store.getters.cameraId);
  rtcEngine.audio.setRecordDevice(store.getters.micId);
  rtcEngine.audio.setPlayoutDevice(store.getters.speakerId);

  const rtcWhiteboard = new RtcWhiteboard();
  window.rtcWhiteboard = rtcWhiteboard;

  rtcWhiteboard.on(RtcWhiteboard.Events.readyStateChanged, payload => {
    console.log('whiteboard ready state changed,', payload);
    store.commit('setWhiteboardAvailable', payload.ready);
  });

  rtcWhiteboard.on(RtcWhiteboard.Events.whiteboardFailover, payload => {
    console.error('got failover event', payload);
    if (payload.state === 'Failed') {
      console.log('已从白板房间断开，将尝试重新连接');
      // rtcWhiteboard.joinChannel(
      //   {
      //     appId: '',
      //     token: '',
      //     channelId: '',
      //     name: '',
      //     userId: '',
      //   },
      //   () => {},
      //   () => {}
      // );
    }
  });

  rtcWhiteboard.on(RtcWhiteboard.Events.userRoleTypeChanged, role => {
    if (role !== Constants.WBRoleType.Attendee) {
      // 仅admin角色可以通过键盘操作课件
      rtcWhiteboard.enableCoursewareInteraction();
    } else {
      rtcWhiteboard.disableCoursewareInteraction();
    }
  });

  rtcWhiteboard.on(RtcWhiteboard.Events.messageReceived, payload => {
    console.log('got whiteboard message:', payload);
    const msg = payload.message;
    if (msg.wbHostId) {
      if (
        msg.wbHostId.toString() !== store.getters.userMe.userId &&
        store.getters.getUserById(msg.wbHostId.toString())
      ) {
        Message.info(
          `${
            store.getters.getUserById(msg.wbHostId.toString())?.userName
          } 正在演示`
        );
      }
      store.commit('setWbHost', msg.wbHostId.toString());
    }
  });

  // 其他用户初次打开白板，本端收到事件直接打开白板
  rtcWhiteboard.on(RtcWhiteboard.Events.openStateChanged, () => {
    store.commit('setWhiteboardOpenState', true);
  });

  // 如果关闭白板时有新收到的绘制消息提示白板内容有更新
  rtcWhiteboard.on(RtcWhiteboard.Events.newShapeReceived, () => {
    if (!store.getters.isWhiteboardOpen) {
      store.commit('setWhiteboardUpdatedState', true);
    }
  });
}
