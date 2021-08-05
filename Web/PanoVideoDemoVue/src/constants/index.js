import { Constants } from '@pano.video/panortc';

export const VideoProfileType = {
  ...Constants.VideoProfileType
};

export const ChannelMode = {
  Mode_1v1: 0,
  Mode_Meeting: 1
};

export const routes = {
  login: '/',
  home: '/meeting'
};

export const localCacheKeyAppId = 'pano.democlient.appId';
export const localCacheKeyToken = 'pano.democlient.token';
export const localCacheKeyChannelId = 'pano.democlient.channelId';
export const localCacheKeyUserName = 'pano.democlient.userName';
export const localCacheKeyUserId = 'pano.democlient.userId';
export const localCacheKeyMuteAtStart = 'pano.democlient.muteatstart';
export const localCacheKeyAutoStart = 'pano.democlient.autostart';
export const localCacheKeyMuteMicAtStart = 'pano.democlient.mutemicatstart';
export const localCacheKeyMuteCamAtStart = 'pano.democlient.mutecamatstart';
export const localCacheKeySelectedCam = 'pano.democlient.selectedCam';
export const localCacheKeyWbNewShapeIndicator = 'pano.democlient.WbNewShapeIndicator';
export const localCacheKeySelectedMic = 'pano.democlient.selectedMic';
export const localCacheKeySelectedSpeaker = 'pano.democlient.selectedSpeaker';
export const localCacheKeyVideoProfileType = 'pano.democlient.localCacheKeyVideoProfileType';
export const localCacheKeyDefaultWb = 'pano.democlient.defaultWb';
export const MOMENT_FOR_UNSUBSCRIBE = 0;
/**
 * 最多同时订阅 8 个人
 */
export const subscribeVideoQuota = 8;

export const QResult = {
  /** @~english Execution succeed. @~chinese 执行成功。 */
  OK: 0,
  /** @~english Execution failed. @~chinese 执行失败。 */
  FAILED: -1,
  /** @~english Fatal error. @~chinese 致命错误。 */
  FATAL: -2,
  /** @~english Invalid argument. @~chinese 非法参数。 */
  INVALID_ARGS: -3,
  /** @~english Invalid state. @~chinese 非法状态。 */
  INVALID_STATE: -4,
  /** @~english Invalid index. @~chinese 无效索引。 */
  INVALID_INDEX: -5,
  /** @~english The object already exists. @~chinese 对象已存在。 */
  ALREADY_EXIST: -6,
  /** @~english The object does not exist. @~chinese 对象不存在。 */
  NOT_EXIST: -7,
  /** @~english The object is not found. @~chinese 对象没发现。 */
  NOT_FOUND: -8,
  NOT_SUPPORTED: -9,
  /** @~english The method is not implemented. @~chinese 方法未实现。 */
  NOT_IMPLEMENTED: -10,
  /** @~english The object is not initialized. @~chinese 对象未初始化。 */
  NOT_INITIALIZED: -11,
  /** @~english The resource limit is reached. @~chinese 已达上限。 */
  LIMIT_REACHED: -12,
  /** @~english No privilege to do. @~chinese 没有权限执行该操作。 */
  NO_PRIVILEGE: -13,
  /** @~english Operation in progress. @~chinese 操作正在进行中。 */
  IN_PROGRESS: -14,
  /** @~english The operation thread is wrong. @~chinese 操作的线程错误。 */
  WRONG_THREAD: -15,

  /** @~english Authentication failed. @~chinese 认证失败。 */
  AUTH_FAILED: -101,
  /** @~english The user is rejected. @~chinese 用户被拒绝。 */
  USER_REJECTED: -102,
  /** @~english The user is expelled. @~chinese 用户被驱逐。 */
  USER_EXPELED: -103,
  /** @~english The userId is duplicate. @~chinese 用户 ID 重复。 */
  USER_DUPLICATE: -104,

  /** @~english The channel is closed. @~chinese 频道被关闭。 */
  CHANNEL_CLOSED: -151,
  /** @~english The channel capacity is full. @~chinese 频道容量已满。 */
  CHANNEL_FULL: -152,
  /** @~english The channel is locked. @~chinese 频道被锁定。 */
  CHANNEL_LOCKED: -153,
  /** @~english The channel mode is mismatch. @~chinese 频道模式不匹配。 */
  CHANNEL_MODE: -154,

  /** @~english A network error occurred. @~chinese 出现网络错误。 */
  NETWORK_ERROR: -301
};
