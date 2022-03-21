/* eslint-disable @typescript-eslint/no-unused-vars */
import {
  RtcEngine as ElectronRctEngine,
  AudioAecType,
  ChannelMode,
  kChannelServiceMedia,
  QResult as PanoRtcResult,
  VideoProfileType as PanoRtcVideoProfileType,
  VideoScalingMode,
  OptionType,
} from '@pano.video/panortc-electron-sdk';
import store from '@/store';
import { remove, find } from 'lodash-es';

enum FailoverState {
  Reconnecting,
  Success,
  Failed,
}

function getLeaveChannelReason(reason: PanoRtcResult) {
  switch (reason) {
    case PanoRtcResult.AUTH_FAILED:
      return '认证失败';
    case PanoRtcResult.USER_REJECTED:
      return '用户被拒绝';
    case PanoRtcResult.USER_EXPELED:
      return '已被踢出会议';
    case PanoRtcResult.USER_DUPLICATE:
      return '用户 ID 重复';
    case PanoRtcResult.CHANNEL_CLOSED:
      return '频道被关闭';
    case PanoRtcResult.CHANNEL_FULL:
      return '频道容量已满';
    case PanoRtcResult.CHANNEL_LOCKED:
      return '频道被锁定';
    case PanoRtcResult.CHANNEL_MODE:
      return '频道模式不匹配';
    case PanoRtcResult.NETWORK_ERROR:
      return '出现网络错误';
    default:
      return `REASON_${reason}`;
  }
}

export const QResult = {
  OK: 'OK',
  FAILED: 'FAILED',
  FATAL: 'FATAL',
  INVALID_ARGS: 'INVALID_ARGS',
  LACK_OF_ARGS: 'LACK_OF_ARGS',
  INVALID_STATE: 'INVALID_STATE',
  INVALID_INDEX: 'INVALID_INDEX',
  ALREADY_EXIST: 'ALREADY_EXIST',
  NOT_EXIST: 'NOT_EXIST',
  NOT_FOUND: 'NOT_FOUND',
  NOT_SUPPORTED: 'NOT_SUPPORTED',
  NOT_IMPLEMENTED: 'NOT_IMPLEMENTED',
  NOT_INITIALIZED: 'NOT_INITIALIZED',
  LIMIT_REACHED: 'LIMIT_REACHED',
  NO_PRIVILEGE: 'NO_PRIVILEGE',
  AUTH_FAILED: 'AUTH_FAILED',
  USER_REJECTED: 'USER_REJECTED',
  USER_EXPELED: 'USER_EXPELED',
  USER_DUPLICATE: 'USER_DUPLICATE',
  CHANNEL_CLOSED: 'CHANNEL_CLOSED',
  CHANNEL_FULL: 'CHANNEL_FULL',
  CHANNEL_LOCKED: 'CHANNEL_LOCKED',
  CHANNEL_MODE: 'CHANNEL_MODE',
  NETWORK_ERROR: 'NETWORK_ERROR',
  INVALID_TOKEN: 'INVALID_TOKEN',
  TIMEOUT: 'TIMEOUT',
};
export const VideoProfileType = {
  Lowest: 'Lowest',
  Low: 'Low',
  Standard: 'Standard',
  HD720P: 'HD720P',
  HD1080P: 'HD1080P',
};
export const JoinChannelType = {
  mediaOnly: 'mediaOnly',
  whiteboardOnly: 'whiteboardOnly',
  mediaAndWhiteboard: 'mediaAndWhiteboard',
  mediaAndRtcMessage: 'mediaAndRtcMessage',
};

type VideoProfileType = string;
type QResult = string;

function getProfile(videoProfile: VideoProfileType) {
  let profile;
  switch (videoProfile) {
    case VideoProfileType.HD1080P:
      profile = PanoRtcVideoProfileType.HD1080P;
      break;
    case VideoProfileType.HD720P:
      profile = PanoRtcVideoProfileType.HD720P;
      break;
    case VideoProfileType.Standard:
      profile = PanoRtcVideoProfileType.Standard;
      break;
    case VideoProfileType.Low:
      profile = PanoRtcVideoProfileType.Low;
      break;
    case VideoProfileType.Lowest:
      profile = PanoRtcVideoProfileType.Lowest;
      break;
    default:
      profile = PanoRtcVideoProfileType.HD720P;
      break;
  }
  return profile;
}

function getResult(reason: PanoRtcResult): QResult {
  return reason === PanoRtcResult.OK ? QResult.OK : QResult.FAILED;
}
/**
 * Adapter for pano electron sdk, all apis are same as websdk.
 */

const rtcEngine = new ElectronRctEngine();

export class RtcEngineAdapter {
  private rtcEngine = rtcEngine;
  static Events = {
    joinChannelConfirm: 'channelJoinConfirm',
    userJoin: 'userJoinIndication',
    userLeave: 'userLeaveIndication',
    activeSpeakerListUpdate: 'activeSpeakerListUpdated',
    channelFailover: 'channelFailover',
    leaveChannelIndication: 'channelLeaveIndication',
    channelCountDown: 'channelCountDown',
    firstAudioDataReceived: 'firstAudioDataReceived',
    audioDeviceChange: 'audioDeviceStateChanged',
    audioDefaultDeviceChanged: 'audioDefaultDeviceChanged',
    userAudioMute: 'userAudioMute',
    userAudioUnmute: 'userAudioUnmute',
    userAudioStart: 'userAudioStart',
    userAudioStop: 'whiteboardStop',
    userAudioCallTypeChanged: 'userAudioCallTypeChanged',
    calloutResult: 'calloutResult',
    userVideoMute: 'userVideoMute',
    userVideoUnmute: 'userVideoUnmute',
    userVideoStart: 'userVideoStart',
    userVideoStop: 'userVideoStop',
    userVideoTransform: 'userVideoTransform',
    userVideoReceived: 'userVideoReceived',
    videoDeviceChange: 'videoDeviceStateChanged',
    userScreenStart: 'userScreenStart',
    userScreenStop: 'userScreenStop',
    userScreenMute: 'userScreenMute',
    userScreenUnmute: 'userScreenUnmute',
    videoStartResult: 'videoStartResult',
    audioStartResult: 'audioStartResult',
    screenStartResult: 'screenStartResult',
    whiteboardAvailable: 'whiteboardAvailable',
    whiteboardUnavailable: 'whiteboardUnavailable',
    whiteboardStart: 'whiteboardStart',
    whiteboardStop: 'whiteboard_stop',
    startScreenTimeout: '',
    getLocalScreen: '',
    userScreenReceived: '',
    getLocalVideo: '',
    localScreenEnded: '',
    getVideoMediaFailed: '',
    getAudioMediaFailed: '',
    getScreenMediaFailed: '',
    groupJoinConfirm: 'groupJoinConfirm',
    groupLeaveIndication: 'groupLeaveIndication',
    groupInviteIndication: 'groupInviteIndication',
    groupDismissConfirm: 'groupDismissConfirm',
    groupUserLeaveIndication: 'groupUserLeaveIndication',
    groupUserJoinIndication: 'groupUserJoinIndication',
    groupDefaultUpdateIndication: 'groupDefaultUpdateIndication',
  };

  static setServer(serverAddr: string) {
    serverAddr = serverAddr.startsWith('http')
      ? serverAddr.split('//')[1]
      : serverAddr;
    RtcEngineAdapter.serverAddr = serverAddr;
  }

  private static serverAddr?: string;

  eventListeners = new Map<
    string,
    {
      originanlListener: (...params: any[]) => any;
      wrapperedListener: (...params: any[]) => any;
    }[]
  >();
  audioLevelObservers: any[] = [];
  statsObservers: any[] = [];

  /**
   * 将 electron sdk 的事件回调包装成和对应的web sdk的回调参数一致
   */
  private makeupListener(
    event: string,
    originanlListener: (...params: any[]) => any
  ) {
    let wrapperedListener = originanlListener;
    const Events = RtcEngineAdapter.Events;
    switch (event) {
      case Events.userJoin:
        wrapperedListener = (userId: string, userName: string) => {
          originanlListener({ userId, user: { userId, userName } });
        };
        break;
      case Events.leaveChannelIndication:
        wrapperedListener = (reason: PanoRtcResult) => {
          originanlListener({ reason: getLeaveChannelReason(reason) });
        };
        break;
      case Events.channelCountDown:
        wrapperedListener = (remain: number) => {
          originanlListener({ remainsec: remain });
        };
        break;
      case Events.userLeave:
        wrapperedListener = (userId: number) => {
          originanlListener({ userId });
        };
        break;
      case Events.joinChannelConfirm:
        wrapperedListener = (reason: PanoRtcResult) => {
          console.log('joinChannelConfirm', reason);
          switch (reason) {
            case PanoRtcResult.OK:
              originanlListener({ result: 'success' });
              break;
            case PanoRtcResult.CHANNEL_MODE:
              originanlListener({ result: 'failed', code: 1 });
              break;
            case PanoRtcResult.AUTH_FAILED:
              originanlListener({ result: 'failed', code: 2 });
              break;
            case PanoRtcResult.CHANNEL_CLOSED:
              originanlListener({ result: 'failed', code: 3 });
              break;
            case PanoRtcResult.CHANNEL_FULL:
              originanlListener({ result: 'failed', code: 5 });
              break;
            case PanoRtcResult.CHANNEL_LOCKED:
              originanlListener({ result: 'failed', code: 6 });
              break;
            default:
              originanlListener({ result: 'failed', code: 2 });
              break;
          }
        };
        break;
      case Events.userVideoStart:
      case Events.userVideoStop:
      case Events.userVideoMute:
      case Events.userVideoUnmute:
      case Events.userAudioStart:
      case Events.userAudioStop:
      case Events.userAudioMute:
      case Events.userAudioUnmute:
      case Events.userScreenStart:
      case Events.userScreenStop:
      case Events.userScreenMute:
      case Events.userScreenUnmute:
        wrapperedListener = (userId: string) => originanlListener({ userId });
        break;
      case Events.channelFailover:
        wrapperedListener = (state: FailoverState) => {
          if (state === FailoverState.Reconnecting) {
            originanlListener({ state: 'Reconnecting' });
          } else if (state === FailoverState.Success) {
            originanlListener({ state: 'Success' });
          } else {
            originanlListener({ state: 'Failed' });
          }
        };
        break;
      case Events.activeSpeakerListUpdate:
        wrapperedListener = (list: string[]) => {
          originanlListener({
            list: list,
          });
        };
        break;
      case Events.userAudioCallTypeChanged:
        //TODO:
        wrapperedListener = (userId: string, type: any) =>
          originanlListener({ userId, type });
        break;
      default:
        wrapperedListener = originanlListener;
        break;
    }
    const eventsList = this.eventListeners.get(event) || [];
    eventsList.push({ wrapperedListener, originanlListener });
    this.eventListeners.set(event, eventsList);
    return wrapperedListener;
  }

  joinChannel(
    channelParam: {
      appId: string;
      channelId: string;
      userId: string;
      token: string;
      userName: string;
      subscribeAudioAll: boolean;
      attendeeId?: string;
    },
    optionalConfig?: any
  ) {
    const option = {
      videoHwAccel: false,
      audioAecType: AudioAecType.Default,
      audioScenario: 0,
    } as any;
    if (RtcEngineAdapter.serverAddr) {
      option.panoServer = RtcEngineAdapter.serverAddr;
    }
    rtcEngine.initialize(channelParam.appId, option);
    const {
      channelId,
      userId,
      token,
      userName,
      subscribeAudioAll,
      attendeeId,
    } = channelParam;
    const res = this.rtcEngine.joinChannel(token, channelId, userId, {
      channelMode: ChannelMode.Mode_Meeting,
      serviceFlags: kChannelServiceMedia,
      subscribeAudioAll,
      userName,
      attendeeId,
    });
    return { code: getResult(res) };
  }

  on(event: string, listener: (...params: any[]) => any) {
    const wrapperedListener = this.makeupListener(event, listener);
    return this.rtcEngine.on(event, wrapperedListener);
  }

  once(event: string, listener: (...params: any[]) => any) {
    const wrapperedListener = this.makeupListener(event, listener);
    return this.rtcEngine.once(event, wrapperedListener);
  }

  off(event: string, listener: (...params: any[]) => any) {
    const listeners = this.eventListeners.get(event) || [];
    const removedListeners = remove(
      listeners,
      (item) => item.originanlListener === listener
    );
    this.eventListeners.set(event, listeners);
    removedListeners.forEach((item) => {
      this.rtcEngine.off(event, item.wrapperedListener);
    });
  }

  /**
   * 离开频道
   */
  leaveChannel(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.leaveChannel();
    return { code: getResult(res), message: '' };
  }

  /**
   * 销毁实例
   */
  destroy(): {
    code: QResult;
    message: string;
  } {
    return { code: QResult.OK, message: '' };
  }

  getGroupManager() {
    return rtcEngine.getGroupManager();
  }

  /**
   * 保存日志到本地
   */
  saveLogs(): void {
    console.error('NOT_IMPLEMENTED');
  }

  /**
   * 获取 VideoStreamManager
   */
  getVideoStreamManager(): any {
    return {};
  }

  /**
   * 获取白板控制接口
   */
  getWhiteboard() {
    console.error('NOT_IMPLEMENTED');
  }

  /**
   * 获取 mic 列表
   * @param successCb
   * @param failCb
   */
  getMics(successCb: any, failCb: any): void {
    const res = this.rtcEngine.audio.getRecordDeviceList();
    successCb(
      res.map((item: any) => ({
        deviceId: item.deviceId,
        label: item.deviceName,
      }))
    );
  }

  /**
   * 获取 speaker 设备列表
   * @param successCb
   * @param failCb
   */
  getSpeakers(successCb: any, failCb: any): void {
    const res = this.rtcEngine.audio.getPlayoutDeviceList();
    successCb(
      res.map((item: any) => ({
        deviceId: item.deviceId,
        label: item.deviceName,
      }))
    );
  }

  /**
   * 获取摄像头设备列表
   * @param successCb
   * @param failCb
   */
  getCams(successCb: any, failCb: any): void {
    const res = this.rtcEngine.video.getCaptureDeviceList();
    successCb(
      res.map((item: any) => ({
        deviceId: item.deviceId,
        label: item.deviceName,
      }))
    );
  }

  /**
   * 设置 mic
   * @param deviceId mic 设备id
   */
  selectMic(deviceId: any): {
    code: QResult;
    message: string;
  } {
    let res = PanoRtcResult.OK;
    if (deviceId === 'default') {
      res = this.rtcEngine.audio.setDefaultRecordDevice();
    } else {
      res = this.rtcEngine.audio.setRecordDevice(deviceId);
    }
    return { code: getResult(res), message: '' };
  }

  /**
   * 设置 speaker
   * @param deviceId speaker 设备id
   */
  selectSpeaker(deviceId: any): {
    code: QResult;
    message: string;
  } {
    let res = PanoRtcResult.OK;
    if (deviceId === 'default') {
      res = this.rtcEngine.audio.setDefaultPlayoutDevice();
    } else {
      res = this.rtcEngine.audio.setPlayoutDevice(deviceId);
    }
    return { code: getResult(res), message: '' };
  }

  /**
   * 选择摄像头
   * @param deviceId cam 设备id
   */
  selectCam(deviceId: string): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.video.setCaptureDevice(deviceId);
    return { code: getResult(res), message: '' };
  }

  /**
   * 静音
   */
  muteMic(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.muteAudio();
    return { code: getResult(res), message: '' };
  }

  /**
   * 取消静音
   */
  unmuteMic(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.unmuteAudio();
    return { code: getResult(res), message: '' };
  }

  /**
   * 静默视频
   */
  muteCam(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.muteVideo();
    return { code: getResult(res), message: '' };
  }

  /**
   * 取消视频静默
   */
  unmuteCam(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.unmuteVideo();
    return { code: getResult(res), message: '' };
  }

  /**
   * 开启音频
   */
  startAudio(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.startAudio();
    return { code: getResult(res), message: '' };
  }
  /**
   * 挂断电话
   */
  dropCall(phoneNo: string): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.dropCall(phoneNo);
    return { code: getResult(res), message: '' };
  }

  /**
   * 停止音频
   */
  stopAudio(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.stopAudio();
    return { code: getResult(res), message: '' };
  }

  /**
   * 继续播放音频
   */
  playAudio(): {
    code: QResult;
    message: string;
  } {
    return { code: QResult.OK, message: '' };
  }

  /**
   * 控制音频播放音量（仅控制web音频控件音量）
   * @param volume
   */
  setAudioPlayoutVolume(volume: number): {
    code: QResult;
    message: string;
  } {
    const res = rtcEngine.audioDeviceMgr().setPlayoutDeviceVolume(volume);
    return { code: getResult(res), message: '' };
  }

  /**
   * 获取音频播放音量
   */
  getAudioPlayoutVolume(): number {
    return rtcEngine.audioDeviceMgr().getPlayoutDeviceVolume();
  }

  /**
   * audio播放静音（别人的声音）
   */
  muteAudioPlayout(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.audioDeviceMgr().setPlayoutDataMuteStatus(true);
    return { code: getResult(res), message: '' };
  }

  /**
   * 取消audio播放静音（别人的声音）
   */
  unmuteAudioPlayout(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.audioDeviceMgr().setPlayoutDataMuteStatus(false);
    return { code: getResult(res), message: '' };
  }

  /**
   * 开启视频, mirror
   */
  startVideo(
    videoProfile: VideoProfileType,
    mirror = true
  ): {
    code: QResult;
    message: string;
  } {
    const profile = getProfile(videoProfile);
    const res = this.rtcEngine.startVideo(store.getters.userMe.videoDomRef, {
      profile: profile,
      scaling: VideoScalingMode.Fit,
      mirror,
    });
    return { code: getResult(res), message: '' };
  }

  /**
   * 关闭视频
   */
  stopVideo(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.stopVideo();
    return { code: getResult(res), message: '' };
  }

  /**
   * 开始屏幕共享
   * @param option audio: 是否共享音频；video.frameRate: 视频帧率；video.height|width: 视频分辨率
   */
  startScreen(option?: {
    audio?: boolean;
    video: { frameRate?: number; height?: number; width?: number };
  }): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.startScreen();
    return { code: getResult(res), message: '' };
  }

  /**
   * 结束屏幕共享
   */
  stopScreen(): {
    code: QResult;
    message: string;
  } {
    this.rtcEngine.stopSoundCardShare();
    const res = this.rtcEngine.stopScreen();
    return { code: getResult(res), message: '' };
  }

  /**
   * 暂停屏幕共享
   */
  muteScreen(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.muteScreen();
    return { code: getResult(res), message: '' };
  }

  /**
   * 恢复屏幕共享
   */
  unmuteScreen(): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.unmuteScreen();
    return { code: getResult(res), message: '' };
  }

  // /**
  //  *  获取网络状态
  //  * @param onFinish
  //  */
  // getNetworkStatus(
  //   onFinish: (status: {
  //     rating: string;
  //     txLoss: number;
  //     rxLoss: number;
  //     rtt: number;
  //   }) => any
  // ): void {}

  /**
   * 返回 PANO SDK 的版本信息
   */
  // getSdkVersion(): string {
  //   return '';
  // }

  /**
   * 反馈、报障
   * @param option
   */
  sendFeedback(option: {
    contact: string;
    product: string;
    description: string;
    type: string;
    token?: string;
    tokenV2?: string;
    uploadLogs: boolean;
    extraInfo?: any;
  }): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.sendFeedback(option);
    return { code: getResult(res), message: '' };
  }

  /**
   * 订阅某个用户音频
   * @param userId
   */
  subscribeAudio(userId: string): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.subscribeAudio(userId);
    return { code: getResult(res), message: '' };
  }

  /**
   * 取消订阅某个用户音频
   * @param userId
   */
  unsubscribeAudio(userId: any): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.unsubscribeAudio(userId);
    return { code: getResult(res), message: '' };
  }

  /**
   * 订阅视频
   * @param params
   */
  subscribeVideo(params: { userId: string; quality: VideoProfileType }): {
    code: QResult;
    message: string;
  } {
    const view = store.getters.getUserById(params.userId).videoDomRef;
    const profile = getProfile(params.quality);
    this.rtcEngine.subscribeVideo(params.userId, view, {
      profile,
      mirror: false,
      scaling: VideoScalingMode.Fit,
    });
    this.rtcEngine.refreshView(view);
    return { code: QResult.OK, message: '' };
  }

  /**
   * 取消订阅视频
   * @param params
   */
  unsubscribeVideo(params: { userId: string }): {
    code: QResult;
    message: string;
  } {
    const res = this.rtcEngine.unsubscribeVideo(params.userId);
    return { code: getResult(res), message: '' };
  }

  /**
   * 订阅屏幕共享
   * @param params
   */
  subscribeScreen(params: { userId: string; quality: VideoProfileType }) {
    const view = store.getters.getUserById(params.userId).screenDomRef;
    return this.rtcEngine.subscribeScreen(params.userId, view);
  }

  /**
   * 取消订阅屏幕共享
   * @param params
   */
  unsubscribeScreen(params: { userId: string }): void {
    this.rtcEngine.unsubscribeScreen(params.userId);
  }

  // /**
  //  * 设置音频前置处理
  //  * @param processorType
  //  * @param mediaProcessor
  //  * @param params
  //  */
  // setMediaProcessor(
  //   processorType: any,
  //   mediaProcessor: any,
  //   params: any
  // ): {
  //   code: QResult;
  //   message: string;
  // } {
  //   return { code: QResult.OK, message: '' };
  // }
  /**
   * 设置用户音频能量回调，监听其他用户音频输入音量大小
   * @param indicationHandler
   * @param intervalMs 回调触发间隔，毫秒
   */
  setUserAudioIndication(
    indicationHandler: (
      records: {
        level: number;
        userId: string;
        active: boolean;
      }[]
    ) => any,
    intervalMs: number
  ) {
    this.onUserAudioLevelChanged(
      (record: { level: number; userId: string; active: boolean }) => {
        indicationHandler([record]);
      },
      intervalMs
    );
    return { code: QResult.OK, message: '' };
  }

  setRecordingAudioIndication(
    indicationHandler: (records: {
      level: number;
      userId: string;
      active: boolean;
    }) => any,
    intervalMs: number
  ) {
    return this.onUserAudioLevelChanged(
      (record: { level: number; userId: string; active: boolean }) => {
        indicationHandler(record);
      },
      intervalMs
    );
    return { code: QResult.OK, message: '' };
  }

  onUserAudioLevelChanged(
    callback: (records: {
      level: number;
      userId: string;
      active: boolean;
    }) => any,
    intervalMs: number
  ) {
    if (callback) {
      this.audioLevelObservers.push(callback);
    }
    if (this.audioLevelObservers.length === 1) {
      this.rtcEngine.setAudioIndicator(
        (level: { active: boolean; level: number; userId: string }) => {
          // 转换level值 为 0-1
          level.level /= 32767;
          this.audioLevelObservers.forEach((cb) => cb(level));
        },
        intervalMs
      );
    }
  }

  /**
   * 预览本地视频
   * @param deviceId
   * @param onSuccess
   * @param onError
   */
  startPreview(
    deviceId: string,
    onSuccess: (videoTag: HTMLVideoElement) => any,
    onError: (error: Error) => any,
    videoProfile: VideoProfileType,
    view: HTMLElement
  ) {
    const profile = getProfile(videoProfile);
    this.rtcEngine.video.startPreview(deviceId, view, {
      profile: profile,
      mirror: true,
      scaling: VideoScalingMode.CropFill,
    });
  }

  /**
   * 停止预览本地视频
   * @param videoTag
   */
  stopPreview(
    videoTag: HTMLVideoElement,
    deviceId: string
  ): {
    code: QResult;
    message: string;
  } {
    if (!deviceId) {
      return { code: QResult.FAILED, message: '' };
    }
    const res = this.rtcEngine.video.stopPreview(deviceId);
    return { code: getResult(res), message: '' };
  }

  /**
   * 设置麦克风音量
   * @param volume
   */
  setAudioRecordVolume(volume: number): {
    code: QResult;
    message: string;
  } {
    const res = rtcEngine.audioDeviceMgr().setRecordDeviceVolume(volume);
    return { code: getResult(res), message: '' };
  }

  /**
   * 获取音频播放音量
   */
  getAudioRecordVolume(): number {
    return rtcEngine.audioDeviceMgr().getRecordDeviceVolume();
  }

  /**
   * 测试音频输入设备
   * @param deviceId
   * @param onSuccess
   * @param onError
   */
  startRecordDeviceTest(
    deviceId: string,
    onSuccess: (result: { audioLevel: number; micTest: number }) => any,
    onError: (error: { message: string }) => any
  ) {
    if (
      rtcEngine.audioDeviceMgr().startRecordDeviceTest(deviceId) ===
      PanoRtcResult.OK
    ) {
      const getAudioLevel = () => {
        const micTest = requestAnimationFrame(getAudioLevel);
        typeof onSuccess === 'function' &&
          onSuccess({
            audioLevel: rtcEngine
              .audioDeviceMgr()
              .getTestRecordingLevel() as number,
            micTest,
          });
      };
      getAudioLevel();
    } else {
      typeof onError === 'function' &&
        onError({
          message: 'Failed',
        });
    }
  }

  /**
   * 停止音频检测
   * @param micTest
   */
  stopRecordDeviceTest(micTest: number): any {
    cancelAnimationFrame(micTest);
    rtcEngine.audioDeviceMgr().stopRecordDeviceTest();
  }

  /**
   * 检测浏览器支持情况
   */
  checkEnvRequirement(): boolean {
    return true;
  }

  screenSourceMgr() {
    return this.rtcEngine.screenSourceMgr();
  }

  startSoundCardShare() {
    return this.rtcEngine.startSoundCardShare();
  }

  setOption(option: { type: OptionType; value: any }) {
    return this.rtcEngine.setOption(option);
  }

  /**
   * @~english
   * @brief stop sound card share
   * @return QResult value to indicate the status
   * @~chinese
   * @brief 停止声卡音频共享
   * @return 返回QResult来指示调用状态
   */
  stopSoundCardShare() {
    return this.rtcEngine.stopSoundCardShare();
  }

  setRtsDelegate(service: any): void {
    this.rtcEngine.setExternalInterface(service);
  }

  addMediaStatsObserver(callback: any) {
    if (callback) {
      this.statsObservers.push(callback);
    }
    if (this.statsObservers.length === 1) {
      this.rtcEngine.setMediaStatsObserver((stats: any) => {
        this.statsObservers.forEach((c) => c(stats));
      });
    }
  }

  removeMediaStatsObserver(callback: any) {
    const index = this.statsObservers.indexOf(callback);
    if (index > -1) {
      remove(this.statsObservers, (item: any) => item === callback);
      if (this.statsObservers.length === 0) {
        this.rtcEngine.setMediaStatsObserver(null);
      }
    }
  }

  acceptUserControl(userId: string) {
    return this.rtcEngine.acceptUserControl(userId);
  }

  cancelUserControl(userId: string) {
    return this.rtcEngine.cancelUserControl(userId);
  }

  /**
   * @~english
   * @brief Start audio dump.
   * @param filePath      The dump file path.
   * @param maxFileSize   The max dump file size. If the value is -1, the file size is unlimited.
   * @return
   *   - OK: Success
   *   - others: Failure
   * @~chinese
   * @brief 开启音频转储。
   * @param filePath      转储文件路径.
   * @param maxFileSize   最大转储文件大小. 如果值为-1，则文件大小不受限制。
   * @return
   *   - OK: 成功
   *   - 其他: 失败
   */
  startAudioDump(filePath: string, maxFileSize: number) {
    return this.rtcEngine.startAudioDump(filePath, maxFileSize);
  }

  /**
   * @~english
   * @brief Stop audio dump.
   * @return
   *   - OK: Success
   *   - others: Failure
   * @~chinese
   * @brief 停止音频转储。
   * @return
   *   - OK: 成功
   *   - 其他: 失败
   */
  stopAudioDump() {
    return this.rtcEngine.stopAudioDump();
  }

  enableAAGC(enable: boolean) {
    window.rtcEngine.setOption({
      type: OptionType.EnableAudioAnalogAgc,
      value: enable,
    });
  }

  disableAV1(disable: boolean) {
    const result = window.rtcEngine.setOption({
      type: OptionType.DisableAV1Encoding,
      value: disable,
    });
  }

  setVideoFrameRate(videoFrameRate: number) {
    window.rtcEngine.setOption({
      type: OptionType.VideoFrameRateType,
      value: videoFrameRate,
    });
  }

  startPlayoutDeviceTest(deviceId: string, filename: string) {
    rtcEngine.audioDeviceMgr().startPlayoutDeviceTest(deviceId, filename);
  }

  getTestPlayoutLevel() {
    return rtcEngine.audioDeviceMgr().getTestPlayoutLevel();
  }

  stopPlayoutDeviceTest() {
    rtcEngine.audioDeviceMgr().stopPlayoutDeviceTest();
  }

  getRecordDevice() {
    return rtcEngine.audioDeviceMgr().getRecordDevice();
  }

  getPlayoutDevice() {
    return rtcEngine.audioDeviceMgr().getPlayoutDevice();
  }
}
