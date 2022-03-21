// declare const panoElectronSdk: { rtcEngine: any };

declare module '@pano.video/panortc-electron-sdk' {
  namespace panortc {
    export class RtcEngine {
      audio: {
        getRecordDeviceList(): { deviceId: string; deviceName: string }[];
        getPlayoutDeviceList(): { deviceId: string; deviceName: string }[];
        setDefaultRecordDevice(): QResult;
        setDefaultPlayoutDevice(): QResult;
        setRecordDevice(deviceId: string): QResult;
        setPlayoutDevice(deviceId: string): QResult;
      };

      video: {
        getCaptureDeviceList(): { deviceId: string; deviceName: string }[];
        setCaptureDevice(deviceId: string): QResult;
        startPreview(
          deviceId: string,
          view: HTMLElement,
          option: {
            profile: VideoProfileType;
            mirror: boolean;
            scaling: VideoScalingMode;
          }
        ): QResult;
        stopPreview(deviceId: string): QResult;
      };

      audioDeviceMgr(): {
        setPlayoutDeviceVolume(volume: number): QResult;
        getPlayoutDeviceVolume(): number;
        setPlayoutDataMuteStatus(status: boolean): QResult;
        setRecordDeviceVolume(volume: number): QResult;
        getRecordDeviceVolume(): QResult;
        startRecordDeviceTest(deviceId: string): QResult;
        getTestRecordingLevel(): number;
        stopRecordDeviceTest(): QResult;
        stopPlayoutDeviceTest(): QResult;
        getRecordDevice(): string;
        getPlayoutDevice(): string;
        startPlayoutDeviceTest(deviceId: string, filePath: string);
        getTestPlayoutLevel(): number;
      };

      screenSourceMgr(): {
        beginConfiguration(commit: boolean): QResult;
        commitConfiguration(): QResult;
        enableFilterWindows(enable: boolean): QResult;
        getApplicationList(): any[];
        getDisplayList(): any[];
        addUnsharedScreenSource(
          sourceType: ScreenSourceType,
          pid: string
        ): QResult;
        addSharedScreenSource(
          sourceType: ScreenSourceType,
          sid: string
        ): QResult;
        selectSharedDisplay(displayId: string): QResult;
        setScreenOptimization(optimized: boolean): QResult;
      };

      startVideo(
        videoDomRef: HTMLElement,
        options: {
          profile: VideoProfileType;
          scaling: VideoScalingMode;
          mirror: boolean;
        }
      ): QResult;

      stopVideo(): QResult;

      startScreen(): QResult;

      stopScreen(): QResult;

      muteScreen(): QResult;

      unmuteScreen(): QResult;

      startSoundCardShare(): QResult;

      setOption(option: any): QResult;

      acceptUserControl(userId: string): QResult;

      cancelUserControl(userId: string): QResult;

      setMediaStatsObserver(cb: ((stats: any) => any) | null): QResult;

      setExternalInterface(delegate: any): QResult;

      stopSoundCardShare(): QResult;

      subscribeVideo: (...key: any) => any;

      leaveChannel(): QResult;

      getGroupManager(): any;

      refreshView(view: any): any;

      muteVideo(): QResult;

      unmuteVideo(): QResult;

      startAudio(): QResult;

      stopAudio(): QResult;

      subscribeAudio(userId: string): QResult;

      unsubscribeAudio(userId: string): QResult;

      unsubscribeVideo(userId: string): QResult;

      subscribeScreen(userId: string, view: HTMLElement): QResult;

      unsubscribeScreen(userId: string): QResult;

      setAudioIndicator(
        onChange: (level: {
          active: boolean;
          level: number;
          userId: string;
        }) => any,
        intervalMs: number
      ): QResult;

      sendFeedback(option: {
        contact: string;
        product: string;
        description: string;
        type: string;
        token?: string;
        tokenV2?: string;
        uploadLogs: boolean;
        extraInfo?: any;
      }): QResult;

      dropCall(phoneNo: string): QResult;

      on(event: string, callback: (...params: any[]) => any): void;

      once(event: string, callback: (...params: any[]) => any): void;

      off(event: string, callback: (...params: any[]) => any): void;

      initialize: (...key: any) => any;

      joinChannel(
        token: string,
        channelId: string,
        userId: string,
        option: {
          channelMode: ChannelMode;
          serviceFlags: number;
          subscribeAudioAll: boolean;
          userName: string;
          attendeeId?: string;
        }
      ): QResult;

      /**
       * 停止音频 dump (only for electron)
       */
      stopAudioDump(): any;

      /**
       * 停止音频 dump (only for electron)
       * @param path 文件存放路径
       * @param maxFileSize 文件最大大小
       */
      startAudioDump(path: string, maxFileSize: number): any;

      muteAudio(): any;

      unmuteAudio(): any;
    }

    export enum ChannelMode {
      Mode_1v1 = 0,
      Mode_Meeting = 1,
    }

    enum QResult {
      /** @~english Execution succeed. @~chinese 执行成功。 */
      OK = 0,
      /** @~english Execution failed. @~chinese 执行失败。 */
      FAILED = -1,
      /** @~english Fatal error. @~chinese 致命错误。 */
      FATAL = -2,
      /** @~english Invalid argument. @~chinese 非法参数。 */
      INVALID_ARGS = -3,
      /** @~english Invalid state. @~chinese 非法状态。 */
      INVALID_STATE = -4,
      /** @~english Invalid index. @~chinese 无效索引。 */
      INVALID_INDEX = -5,
      /** @~english The object already exists. @~chinese 对象已存在。 */
      ALREADY_EXIST = -6,
      /** @~english The object does not exist. @~chinese 对象不存在。 */
      NOT_EXIST = -7,
      /** @~english The object is not found. @~chinese 对象没发现。 */
      NOT_FOUND = -8,
      NOT_SUPPORTED = -9,
      /** @~english The method is not implemented. @~chinese 方法未实现。 */
      NOT_IMPLEMENTED = -10,
      /** @~english The object is not initialized. @~chinese 对象未初始化。 */
      NOT_INITIALIZED = -11,
      /** @~english The resource limit is reached. @~chinese 已达上限。 */
      LIMIT_REACHED = -12,
      /** @~english No privilege to do. @~chinese 没有权限执行该操作。 */
      NO_PRIVILEGE = -13,
      /** @~english Operation in progress. @~chinese 操作正在进行中。 */
      IN_PROGRESS = -14,
      /** @~english The operation thread is wrong. @~chinese 操作的线程错误。 */
      WRONG_THREAD = -15,

      /** @~english Authentication failed. @~chinese 认证失败。 */
      AUTH_FAILED = -101,
      /** @~english The user is rejected. @~chinese 用户被拒绝。 */
      USER_REJECTED = -102,
      /** @~english The user is expelled. @~chinese 用户被驱逐。 */
      USER_EXPELED = -103,
      /** @~english The userId is duplicate. @~chinese 用户 ID 重复。 */
      USER_DUPLICATE = -104,

      /** @~english The channel is closed. @~chinese 频道被关闭。 */
      CHANNEL_CLOSED = -151,
      /** @~english The channel capacity is full. @~chinese 频道容量已满。 */
      CHANNEL_FULL = -152,
      /** @~english The channel is locked. @~chinese 频道被锁定。 */
      CHANNEL_LOCKED = -153,
      /** @~english The channel mode is mismatch. @~chinese 频道模式不匹配。 */
      CHANNEL_MODE = -154,

      /** @~english A network error occurred. @~chinese 出现网络错误。 */
      NETWORK_ERROR = -301,
    }

    /** @~english Channel service flag, media is enable. @~chinese 频道标志位，媒体。 */
    export const kChannelServiceMedia = 0x01;
    /** @~english Channel service flag, whiteboard is enable. @~chinese 频道标志位，白板。 */
    const kChannelServiceWhiteboard = 0x02;

    const UserLeaveReason: {
      Normal: number;
      Expelled: number;
      Disconnected: number;
      ChannelEnd: number;
      DuplicateUserID: number;
    };

    const MediaSubscribeResult: {
      Success: number;
      UserNotFound: number;
      LimitReached: number;
    };

    enum FailoverState {
      Reconnecting,
      Success,
      Failed,
    }

    export enum VideoProfileType {
      Lowest = 0,
      Low = 1,
      Standard = 2,
      HD720P = 3,
      HD1080P = 4,
      None = 5,
      MAX = 4,
    }

    enum VideoScalingMode {
      Fit = 0,
      FullFill = 1,
      CropFill = 2,
    }

    enum AudioAecType {
      Off = 0,
      Default = 1,
      Software = 2,
      BuiltIn = 3,
    }

    const AudioDeviceType: {
      Unknown: number;
      Record: number;
      Playout: number;
    };

    const AudioDeviceState: {
      Active: number;
      Inactive: number;
    };

    const VideoDeviceType: {
      Unknown: number;
      Capture: number;
    };

    const VideoDeviceState: {
      Added: number;
      Removed: number;
    };

    const ScreenSourceType: {
      Display: number;
      Application: number;
      Window: number;
    };

    enum OptionType {
      FaceBeautify = 0,
      EnableUploadDebugLogs = 1,
      EnableUploadAudioDump = 2,
      AudioEqualizationMode = 3,
      AudioReverbMode = 4,
      VideoFrameRateType = 5,
      EnableAudioEarMonitoring = 6,
      EnableAudioAnalogAgc = 21,
      DisableAV1Encoding = 25,
    }

    const VideoFrameRateType: {
      Low: number;
      Standard: number;
    };

    const AudioEqualizationOption: {
      None: number;
      Bass: number;
      Loud: number;
      VocalMusic: number;
      Strong: number;
      Pop: number;
      Live: number;
      DanceMusic: number;
      Club: number;
      Soft: number;
      Rock: number;
      Party: number;
      Classical: number;
      Test: number;
    };
    const AudioReverbOption: {
      None: number;
      VocalI: number;
      VocalII: number;
      Bathroom: number;
      SmallRoomBright: number;
      SmallRoomDark: number;
      MediumRoom: number;
      LargeRoom: number;
      ChurchHall: number;
      Cathedral: number;
    };
    const AudioMixingState: {
      Started: number;
      Finished: number;
    };
    const ImageFileFormat: {
      JPEG: number;
      PNG: number;
      BMP: number;
    };
    const QualityRating: {
      Unavailable: number;
      VeryBad: number;
      Bad: number;
      Poor: number;
      Good: number;
      Excellent: number;
    };
    const WBRoleType: {
      ADMIN: number;
      ATTENDEE: number;
      VIEWER: number;
    };
    const WBImageScalingMode: {
      FIT: number;
      AUTO_FILL: number;
      FILL_WIDTH: number;
      FILL_HEIGHT: number;
    };
    const WBToolType: {
      SELECT: number;
      PATH: number;
      LINE: number;
      RECT: number;
      ELLIPSE: number;
      IMAGE: number;
      TEXT: number;
      ERASER: number;
      BRUSH: number;
      ARROW: number;
      POLYLINE: number;
      POLYGON: number;
      ARC: number;
      CURVE: number;
    };
    const WBFontStyle: {
      NORMAL: number;
      BOLD: number;
      ITALIC: number;
      BOLD_ITALIC: number;
    };
  }
  export = panortc;
}
