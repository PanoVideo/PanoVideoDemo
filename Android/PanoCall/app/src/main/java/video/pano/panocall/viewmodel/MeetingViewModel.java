package video.pano.panocall.viewmodel;

import static com.pano.rtc.api.Constants.MessageServiceState.Available;
import static video.pano.panocall.info.Config.DELAY_TIME;
import static video.pano.panocall.info.Config.MAX_AUDIO_DUMP_SIZE;
import static video.pano.panocall.info.Constant.HOST_ID_KEY;
import static video.pano.panocall.info.Constant.KEY_APP_UUID;
import static video.pano.panocall.info.Constant.KEY_AUDIO_UM_MUTE;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_CAMERA;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_SPEAKER;
import static video.pano.panocall.info.Constant.KEY_DEVICE_RATING;
import static video.pano.panocall.info.Constant.KEY_ENABLE_DEBUG_MODE;
import static video.pano.panocall.info.Constant.KEY_ENABLE_FACE_BEAUTY;
import static video.pano.panocall.info.Constant.KEY_FACE_BEAUTY_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_COMMAND;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_DESCRIPTION;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_TYPE;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_TYPE_COMMAND;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.pano.rtc.api.Constants;
import com.pano.rtc.api.PanoAnnotation;
import com.pano.rtc.api.PanoAnnotationManager;
import com.pano.rtc.api.RtcChannelConfig;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.RtcEngineCallback;
import com.pano.rtc.api.RtcMessageService;
import com.pano.rtc.api.RtcWhiteboard;
import com.pano.rtc.api.model.RtcAudioLevel;
import com.pano.rtc.api.model.RtcPropertyAction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnActEventListener;
import video.pano.panocall.listener.OnBottomEventListener;
import video.pano.panocall.listener.OnFrgEventListener;
import video.pano.panocall.listener.OnTopEventListener;
import video.pano.panocall.listener.OnWhiteboardEventListener;
import video.pano.panocall.model.PropertyData;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public class MeetingViewModel extends ViewModel implements LifecycleObserver,
        RtcEngineCallback,
        RtcWhiteboard.Callback,
        PanoAnnotationManager.Callback,
        RtcMessageService.Callback {

    private static final String TAG = "MeetingViewModel";

    private static final long TIME_OUT_DELAY = 60 * 1000;
    private static final long DUMP_SIZE = 200 * 1024 * 1024;

    private static final Gson sGson = new Gson();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public boolean mIsAudioMuted = false;
    public boolean mIsAudioSpeakerOpened = true;
    public boolean mAutoStartCamera = true;
    public boolean mIsRoomJoined = false;
    public boolean mWhiteboardStart = false;
    public boolean mWhiteboardContentUpdate = false;
    public boolean mPaused = false;
    public boolean mAnnotationStart = false;

    private boolean mFirstTakeHost = true ;
    private boolean mEnableDeviceRating = true;
    private boolean mAutoMuteAudio = false;
    private boolean mEnableDebugMode = false;
    private boolean mIsWhiteboardAvailable = false;
    private boolean mFbEnabled = false;

    public int mAnnotationActionId = 0;
    public Bundle mAnnotationExtra = null;
    public String mToken;
    public String mRoomId;
    public long mUserId;
    public String mUserName;
    public int mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT;
    public long mCurrentScreenUserId = 0L;

    private OnWhiteboardEventListener mWhiteboardEventListener;
    private OnBottomEventListener mBottomEventListener;
    private OnTopEventListener mTopEventListener;
    private OnActEventListener mActListener;
    private OnFrgEventListener mFrgListener;

    public List<RtcAudioLevel> mRtcAudioLevels = new ArrayList<>();
    public Constants.VideoProfileType mLocalProfile = Constants.VideoProfileType.Standard;

    private final MutableLiveData<String> mMessageEvent = new MutableLiveData<>();

    public MutableLiveData<String> getMessageEvent() {
        return mMessageEvent;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        initRtc();
        loadSettings();
        joinRoom();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        updateFaceBeautyData();
        if (mPaused) {
            mPaused = false;
            mFrgListener.onCheckState();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        mPaused = true;
        mAnnotationActionId = -1;
        mAnnotationExtra = null;
        mAnnotationStart = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        resetRtc();
        reset();
    }

    private void initRtc() {
        PanoRtcEngine.getIns().registerEventHandler(this);
        PanoRtcEngine.getIns().registerWhiteboardHandler(this);
        PanoRtcEngine.getIns().registerMessageCallback(this);
        PanoRtcEngine.getIns().getPanoEngine().getAnnotationMgr().setCallback(this);
    }

    private void resetRtc() {
        PanoRtcEngine.getIns().removeEventHandler(this);
        PanoRtcEngine.getIns().removeWhiteboardHandler(this);
        PanoRtcEngine.getIns().removeMessageCallback(this);
        PanoRtcEngine.getIns().getPanoEngine().getAnnotationMgr().setCallback(null);
        PanoRtcEngine.getIns().refresh();
    }

    private void loadSettings() {
        mIsAudioMuted = mAutoMuteAudio = !SPUtils.getBoolean(KEY_AUDIO_UM_MUTE, true);
        mIsAudioSpeakerOpened = SPUtils.getBoolean(KEY_AUTO_START_SPEAKER, true);
        mAutoStartCamera = SPUtils.getBoolean(KEY_AUTO_START_CAMERA, true);
        mEnableDebugMode = SPUtils.getBoolean(KEY_ENABLE_DEBUG_MODE, false);
        mLocalProfile = DeviceRatingTest.getIns()
                .getProfileType(SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2));
        mEnableDeviceRating = SPUtils.getBoolean(KEY_DEVICE_RATING, true);
        Config.sIsFrontCamera = true;
    }

    public void setIntentData(String roomId, long userId, String token, String userName) {
        mRoomId = roomId;
        mUserId = userId;
        mToken = token;
        mUserName = userName;
    }

    public void setOnWhiteboardEventListener(OnWhiteboardEventListener listener) {
        mWhiteboardEventListener = listener;
    }

    public void setOnBottomEventListener(OnBottomEventListener listener) {
        mBottomEventListener = listener;
    }

    public void setOnTopEventListener(OnTopEventListener listener) {
        mTopEventListener = listener;
    }

    public void setOnActivityRtcEventListener(OnActEventListener listener) {
        mActListener = listener;
    }

    public void setOnFragmentEventListener(OnFrgEventListener listener) {
        mFrgListener = listener;
    }

    public void reset() {
        UserManager.getIns().clear();
        Config.sIsFrontCamera = true;
        mIsRoomJoined = false;
        mEnableDebugMode = false;
        mHandler.removeCallbacksAndMessages(null);
        AnnotationHelper.getIns().setAnnotation(null);

        mFrgListener = null;
        mWhiteboardEventListener = null;
        mBottomEventListener = null;
        mTopEventListener = null;
        mActListener = null;
    }

    private RtcEngine rtcEngine() {
        return PanoRtcEngine.getIns().getPanoEngine();
    }

    private void updateFaceBeautyData() {
        mFbEnabled = SPUtils.getBoolean(KEY_ENABLE_FACE_BEAUTY, false);
        rtcEngine().setFaceBeautify(mFbEnabled);
        if (mFbEnabled) {
            float fbIntensity = SPUtils.getFloat(KEY_FACE_BEAUTY_INTENSITY, 0);
            rtcEngine().setFaceBeautifyIntensity(fbIntensity);
        }
    }

    public void refreshVideoProfile(Constants.VideoProfileType profile) {
        if (mLocalProfile != profile) {
            mLocalProfile = profile;
            if (Config.sIsLocalVideoStarted) {
                rtcEngine().startVideo(mLocalProfile, Config.sIsFrontCamera);
            }
        }
    }

    public void refreshVideoFrame(Constants.VideoFrameRateType rateType) {
        rtcEngine().setVideoFrameRate(rateType);
    }

    public void onDestinationChangedListener() {
        if (mFrgListener != null) {
            mFrgListener.onDestinationChangedListener();
        }
    }

    /**
     * 加入房间
     */
    private boolean joinRoom() {
        if (TextUtils.isEmpty(mToken)) {
            mMessageEvent.postValue("joinChannel failed ");
            return false;
        }
        // save room info
        Config.sIsLocalVideoStarted = false;
        UserManager.getIns().setLocalUser(new UserInfo(mUserId, mUserName));
        // reset audio speaker state
        rtcEngine().setLoudspeakerStatus(mIsAudioSpeakerOpened);

        RtcChannelConfig config = new RtcChannelConfig();
        config.userName = mUserName;
        config.mode_1v1 = false;
        // 设置自动订阅所有音频
        config.subscribeAudioAll = true;
        Constants.QResult ret = rtcEngine().joinChannel(mToken, mRoomId, mUserId, config);
        if (ret != Constants.QResult.OK) {
            mMessageEvent.postValue("joinChannel failed, result=" + ret);
            return false;
        }
        return true;
    }

    public void startPreview() {
        rtcEngine().startPreview(mLocalProfile, Config.sIsFrontCamera);
    }

    public PanoAnnotation getPanoVideoAnnotation(long userId, int streamId) {
        return rtcEngine().getAnnotationMgr().getVideoAnnotation(userId, streamId);
    }

    public PanoAnnotation getPanoShareAnnotation(long userId) {
        return rtcEngine().getAnnotationMgr().getShareAnnotation(userId);
    }

    public RtcWhiteboard getPanoWhiteboard() {
        return PanoRtcEngine.getIns().getPanoEngine().getWhiteboard();
    }

    public RtcMessageService getPanoMessageService() {
        return PanoRtcEngine.getIns().getPanoEngine().getMessageService();
    }

    public void onConfirmedBackPressed() {
        if (mFrgListener != null) {
            mFrgListener.onClickAnnotationStop();
        }
        if (mActListener != null) {
            mActListener.onClickAnnotationStop();
        }
        Config.sIsLocalVideoStarted = false;
        rtcEngine().leaveChannel();
    }

    private void updateProfileByDeviceRating(Constants.DeviceRating deviceRating) {
        int resolution = SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2);
        int maxProfile = DeviceRatingTest.getIns().updateProfileByDeviceRating(deviceRating);
        if (resolution > maxProfile) {
            resolution = maxProfile;
            DeviceRatingTest.getIns().showRatingToast(resolution);
        }
        mLocalProfile = DeviceRatingTest.getIns().getProfileType(resolution);
        SPUtils.put(KEY_VIDEO_SENDING_RESOLUTION, resolution);
    }

    /**************  OnAnnotationControlPanelListener  **************/
    /**************  OnAnnotationControlPanelListener  **************/

    /*********************RtcEngine Callback******************************/

    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        if (mEnableDeviceRating) {
            updateProfileByDeviceRating(rtcEngine()
                    .queryDeviceRating());
        }
        if (result == Constants.QResult.OK) {
            if (mEnableDebugMode) {
                rtcEngine().startAudioDump(MAX_AUDIO_DUMP_SIZE);
            }
            rtcEngine().stopPreview();
            rtcEngine().startAudio();
            // 启动本地视频
            if (mAutoStartCamera) {
                rtcEngine().startVideo(mLocalProfile, Config.sIsFrontCamera);
            }
            if (mAutoMuteAudio) {
                rtcEngine().muteAudio();
            }

            mIsRoomJoined = true;
            Config.sIsLocalVideoStarted = mAutoStartCamera;

            UserInfo localUser = UserManager.getIns().getLocalUser();
            localUser.setVideoStarted(mAutoStartCamera);
            localUser.setAudioMuted(mAutoMuteAudio);

            if (mFrgListener != null) {
                mFrgListener.subscribeLocalVideo();
                mFrgListener.updateUserAudioState(localUser);
            }
        } else {
            mIsRoomJoined = false;
            mMessageEvent.postValue("joinChannel failed, result=" + result);
        }

    }

    @Override
    public void onChannelLeaveIndication(Constants.QResult result) {
        if (mActListener != null) {
            mActListener.onChannelLeaveIndication();
        }
        mIsRoomJoined = false;
    }

    @Override
    public void onUserJoinIndication(long userId, String userName) {
        UserInfo joinUser = new UserInfo(userId, userName);
        UserManager.getIns().addRemoteUser(joinUser);
        if (mActListener != null) {
            mActListener.onUserJoinIndication();
        }
        if (mFrgListener != null) {
            mFrgListener.onUserJoinIndication(joinUser);
        }
    }

    @Override
    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setVideoStarted(false);
            user.setScreenStarted(false);
        }
        UserManager.getIns().removeScreenUser(user);
        UserManager.getIns().removeRemoteUser(userId);
        UserManager.getIns().removeVideoUser(userId);
        if (mActListener != null) {
            mActListener.onUserLeaveIndication(user);
        }
        if (mFrgListener != null) {
            mFrgListener.onUserLeaveIndication(user);
        }
    }

    @Override
    public void onUserAudioStart(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(false);
            if (mFrgListener != null) {
                mFrgListener.onUserAudioStart(user);
            }
        }
    }

    @Override
    public void onUserAudioStop(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(true);
            if (mFrgListener != null) {
                mFrgListener.onUserAudioStop(user);
            }
        }
    }

    @Override
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setVideoStarted(true);
            UserManager.getIns().addVideoUser(user);
            if (mFrgListener != null) {
                mFrgListener.onUserVideoStart(user);
            }
        }
    }

    @Override
    public void onUserVideoStop(long userId) {
        UserManager.getIns().removeVideoUser(userId);
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setVideoStarted(false);
            if (mFrgListener != null) {
                mFrgListener.onUserVideoStop(user);
            }
        }
    }

    @Override
    public void onUserAudioMute(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(true);
            if (mFrgListener != null) {
                mFrgListener.onUserAudioMute(user);
            }
        }
    }

    @Override
    public void onUserAudioUnmute(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(false);
            if (mFrgListener != null) {
                mFrgListener.onUserAudioUnmute(user);
            }
        }
    }

    @Override
    public void onUserScreenStart(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            user.setScreenStarted(true);
            if (user.isScreenStarted()) {
                UserManager.getIns().addScreenUser(user);
            }
            if (mActListener != null) {
                mActListener.onUserScreenStart(user);
            }
            if (mFrgListener != null) {
                mFrgListener.onUserScreenStart(userId);
            }
        }
    }

    @Override
    public void onUserScreenStop(long userId) {
        UserInfo user = UserManager.getIns().getRemoteUser(userId);
        if (user != null) {
            UserManager.getIns().removeScreenUser(user);
            user.setScreenStarted(false);
            if (mActListener != null) {
                mActListener.onUserScreenStop(user);
            }
            if (mFrgListener != null) {
                mFrgListener.onUserScreenStop(user);
            }
        }
    }

    @Override
    public void onWhiteboardStart() {
        if (!mIsWhiteboardAvailable) {
            mMessageEvent.postValue(Utils.getApp().getString(R.string.msg_whiteboard_unavailable));
            return;
        }
        if (mFrgListener != null) {
            mFrgListener.onWhiteboardStart();
        }
        if (mActListener != null) {
            mActListener.onWhiteboardStart();
        }
        mWhiteboardStart = true;
    }

    @Override
    public void onWhiteboardStop() {
        if (mWhiteboardEventListener != null) mWhiteboardEventListener.onWhiteboardStop();
        mWhiteboardStart = false;
    }

    @Override
    public void onWhiteboardAvailable() {
        mIsWhiteboardAvailable = true;
    }

    @Override
    public void onWhiteboardUnavailable() {
        mIsWhiteboardAvailable = false;
    }

    @Override
    public void onScreenStartResult(Constants.QResult result) {
    }

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        if (mFrgListener != null) {
            mFrgListener.onNetworkQuality(userId, quality);
        }
    }

    @Override
    public void onUserAudioCallTypeChanged(long userId, Constants.AudioCallType type) {
        UserInfo pstnUser = null;
        if (UserManager.getIns().isMySelf(userId)) {
            pstnUser = UserManager.getIns().getLocalUser();
        } else {
            pstnUser = UserManager.getIns().getRemoteUser(userId);
        }
        if (pstnUser != null) {
            pstnUser.setPSTNAudioType(type != Constants.AudioCallType.VoIP);
        }
        if (mBottomEventListener != null) {
            mBottomEventListener.updateCallButtonState(UserManager.getIns().isMySelf(userId)
                    && type != Constants.AudioCallType.VoIP);
        }
        if (mFrgListener != null) {
            mFrgListener.onUserAudioCallTypeChanged(pstnUser);
        }
    }

    @Override
    public void onChannelCountDown(long remain) {
        if (mTopEventListener != null) {
            mTopEventListener.onChannelCountDown(remain);
        }
    }

    @Override
    public void onChannelFailover(Constants.FailoverState state) {
        if (mActListener != null) {
            mActListener.onChannelFailover(state.equals(Constants.FailoverState.Reconnecting));
        }
    }

    /**************  RtcWhiteboard  **************/
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        if (mWhiteboardEventListener != null) {
            mWhiteboardEventListener.onPageNumberChanged(curPage, totalPages);
        }
    }

    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {

    }

    @Override
    public void onViewScaleChanged(float scale) {
        if (mWhiteboardEventListener != null) {
            mWhiteboardEventListener.onViewScaleChanged(scale);
        }
    }

    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        if (mWhiteboardEventListener != null) {
            mWhiteboardEventListener.onRoleTypeChanged(newRole);
        }
    }

    @Override
    public void onContentUpdated() {
        mWhiteboardContentUpdate = true;
        if (mBottomEventListener != null) mBottomEventListener.updateCallShareButtonState();
    }

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {
        if (result == Constants.QResult.OK) {
            if (mWhiteboardEventListener != null) {
                mWhiteboardEventListener.onCreateDoc(result, fileId);
            }
        }
    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {
        if (result == Constants.QResult.OK) {
            if (mWhiteboardEventListener != null) {
                mWhiteboardEventListener.onSwitchDoc(result, fileId);
            }
        }
    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {
        if (result == Constants.QResult.OK) {
            if (mWhiteboardEventListener != null) {
                mWhiteboardEventListener.onDeleteDoc(result, fileId);
            }
        }
    }

    @Override
    public void onUserJoined(long userId, String userName) {
        UserManager.getIns().addWhiteboardUser(new UserInfo(userId, userName));
    }

    @Override
    public void onUserLeft(long userId) {
        UserManager.getIns().removeWhiteboardUser(userId);
    }

    @Override
    public void onVisionShareStopped(long userId) {
        UserInfo userInfo = UserManager.getIns().getRemoteUser(userId);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getUserName())) {
            String name = userInfo.getUserName();
            mMessageEvent.postValue(Utils.getApp().getString(R.string.whiteboard_stopped_vision_share, name));
        }
        PanoRtcEngine.getIns().getPanoWhiteboard().stopFollowVision();
    }

    @Override
    public void onVisionShareStarted(long userId) {
        UserInfo userInfo = UserManager.getIns().getRemoteUser(userId);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getUserName())) {
            String name = userInfo.getUserName();
            mMessageEvent.postValue(Utils.getApp().getString(R.string.whiteboard_started_vision_share, name));
        }
        PanoRtcEngine.getIns().getPanoWhiteboard().startFollowVision();
    }

    @Override
    public void onMessage(long userId, byte[] msg) {
    }
    /**************  RtcWhiteboard  **************/

    /**************  PanoAnnotationManager.Callback  **************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        final PanoAnnotation annotation = rtcEngine().getAnnotationMgr()
                .getVideoAnnotation(userId, streamId);
        AnnotationHelper.getIns().setAnnotation(annotation);
        AnnotationHelper.getIns().setAnnotationUserId(userId);

        mHandler.postDelayed(() -> {
            if (mFrgListener != null) {
                mFrgListener.onVideoAnnotationStart(userId, streamId);
            }
            if (mActListener != null) {
                mActListener.onVideoAnnotationStart(userId);
            }
        }, DELAY_TIME);
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
        if (mActListener != null) {
            mActListener.onVideoAnnotationStop(userId);
        }
        if (mFrgListener != null) {
            mFrgListener.onVideoAnnotationStop(userId);
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        final PanoAnnotation annotation = rtcEngine().getAnnotationMgr().getShareAnnotation(userId);
        AnnotationHelper.getIns().setAnnotation(annotation);
        AnnotationHelper.getIns().setAnnotationUserId(userId);

        mHandler.postDelayed(() -> {
            if (mFrgListener != null) {
                mFrgListener.onShareAnnotationStart(userId);
            }
            if (mActListener != null) {
                mActListener.onShareAnnotationStart(userId);
            }
        }, DELAY_TIME);
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        if (mActListener != null) {
            mActListener.onShareAnnotationStop(userId);
        }
        if (mFrgListener != null) {
            mFrgListener.onShareAnnotationStop(userId);
        }
    }
    /**************  PanoAnnotationManager.Callback  **************/

    /**************  RtcMessageService.Callback  **************/

    @Override
    public void onServiceStateChanged(Constants.MessageServiceState state, Constants.QResult reason) {
        if (state == Available && reason == Constants.QResult.OK && mFirstTakeHost) {
            mHandler.postDelayed(() -> {
                long hostId = UserManager.getIns().getHostId();
                if (hostId == 0L || (UserManager.getIns().getRemoteUser(hostId) == null
                        && UserManager.getIns().getLocalUserId() != hostId)) {
                    sendProperty(new PropertyData(String.valueOf(UserManager.getIns().getLocalUserId())));
                }
            }, 1000);
            mFirstTakeHost = false ;
        }
    }

    @Override
    public void onUserMessage(long userId, byte[] data) {
        String jsonStr = new String(data);
        try {
            Log.d(TAG, "onUserMessage : userId = " + userId + " , message : " + jsonStr);
            JSONObject jsonObject = new JSONObject(jsonStr);
            String msgType = jsonObject.getString(KEY_SOUND_FEEDBACK_TYPE);
            String command = jsonObject.getString(KEY_SOUND_FEEDBACK_COMMAND);
            String desc = jsonObject.getString(KEY_SOUND_FEEDBACK_DESCRIPTION);

            if (VALUE_SOUND_FEEDBACK_TYPE_COMMAND.equals(msgType)
                    && VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP.equals(command)) {
                rtcEngine().startAudioDump(DUMP_SIZE);
                mHandler.postDelayed(() -> {
                    rtcEngine().stopAudioDump();

                    RtcEngine.FeedbackInfo info = new RtcEngine.FeedbackInfo();
                    info.type = Constants.FeedbackType.Audio;
                    info.productName = "PanoVideoCall";
                    info.description = desc;
                    info.extraInfo = SPUtils.getString(KEY_APP_UUID, "");
                    info.uploadLogs = true;
                    UserInfo localUser = UserManager.getIns().getLocalUser();
                    if (localUser != null && !TextUtils.isEmpty(localUser.userName)) {
                        info.contact = localUser.userName;
                    }
                    rtcEngine().sendFeedback(info);
                }, TIME_OUT_DELAY);

            }
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void onPropertyChanged(RtcPropertyAction[] props) {
        if (props == null || props.length <= 0) return;
        try {
            for (RtcPropertyAction propertyAction : props) {
                if (HOST_ID_KEY.equals(propertyAction.propName)) {
                    String dataJson = new String(propertyAction.propValue);
                    if (TextUtils.isEmpty(dataJson)) return;
                    PropertyData propertyData = sGson.fromJson(dataJson, PropertyData.class);
                    if (propertyData == null || TextUtils.isEmpty(propertyData.hostId)) return;
                    long hostId = Long.parseLong(propertyData.hostId);
                    if (UserManager.getIns().getLocalUserId() == hostId) {
                        getPanoWhiteboard().setRoleType(Constants.WBRoleType.Admin);
                    }
                    UserManager.getIns().setHostId(hostId);
                    if (mFrgListener != null) mFrgListener.onHostUserIdChanged(hostId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendProperty(PropertyData data) {
        if (data == null) return;
        getPanoMessageService().setProperty(HOST_ID_KEY, sGson.toJson(data).getBytes());
    }

    /**************  RtcMessageService.Callback  **************/

    /**************  OnControlEventListener  **************/
    public void onShowHideControlPanel() {
        if (mActListener != null) mActListener.onShowHideControlPanel();
    }
    /**************  OnControlEventListener  **************/
    /**************  OnTopControlPanelListener  **************/
    public void onTCPanelAudio(boolean isSpeaker) {
        rtcEngine().setLoudspeakerStatus(isSpeaker);
    }

    public void onTCPanelSwitchCamera() {
        rtcEngine().switchCamera();
        Config.sIsFrontCamera = !Config.sIsFrontCamera;
        if (mActListener != null) {
            mActListener.onTCPanelSwitchCamera();
        }
        if (mFrgListener != null) {
            mFrgListener.onSwitchCamera();
        }
    }

    public void onTCPanelExit() {
        if (mActListener != null) {
            mActListener.onTCPanelExit();
        }
    }
    /**************  OnTopControlPanelListener  **************/
    /**************  OnBottomControlPanelListener  **************/
    public void onBCPanelAudio(boolean muted) {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        localUser.setAudioMuted(muted);
        if (mFrgListener != null) {
            mFrgListener.updateUserAudioState(localUser);
        }
        if (muted) {
            rtcEngine().muteAudio();
        } else {
            rtcEngine().unmuteAudio();
        }

        mRtcAudioLevels.clear();
    }

    public void onBCPanelVideo(boolean start) {
        if (mTopEventListener != null) {
            mTopEventListener.onBCPanelVideo(!start);
        }
        if (start) {
            startLocalVideo();
        } else {
            stopLocalVideo();
        }
    }

    public void onBCPanelShare() {
        if (!mIsWhiteboardAvailable) {
            mMessageEvent.postValue(Utils.getApp().getString(R.string.msg_whiteboard_unavailable));
            return;
        }
        if (mActListener != null) {
            mActListener.onBCPanelShare();
        }
    }

    public void onBCPanelUserList() {
        if (mActListener != null) {
            mActListener.onBCPanelUserList();
        }
    }

    public void onBCPanelMore() {
        if (mActListener != null) {
            mActListener.onBCPanelMore();
        }
    }

    private void startLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mIsRoomJoined) {
            rtcEngine().startVideo(mLocalProfile, Config.sIsFrontCamera);
            localUser.setVideoStarted(true);
        } else {
            rtcEngine().startPreview(mLocalProfile, Config.sIsFrontCamera);
        }
        Config.sIsLocalVideoStarted = false;
        if (mFrgListener != null) {
            mFrgListener.startLocalVideo();
        }
    }

    private void stopLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mIsRoomJoined) {
            rtcEngine().stopVideo();
            localUser.setVideoStarted(false);
        } else {
            rtcEngine().stopPreview();
        }
        Config.sIsLocalVideoStarted = true;
        if (mFrgListener != null) {
            mFrgListener.stopLocalVideo();
        }
    }

    /**************  OnBottomControlPanelListener  **************/

    /**************  OnAnnotationControlPanelListener  **************/

    public void onClickAnnotationStop() {
        if (mActListener != null) {
            mActListener.onClickAnnotationStop();
        }
        if (mFrgListener != null) {
            mFrgListener.onClickAnnotationStop();
        }
    }

    public void onAnnotationToolsClick() {
        if (mFrgListener != null) {
            mFrgListener.onAnnotationToolsClick();
        }
    }

    /**************  OnAnnotationControlPanelListener  **************/


    public int getShowUserCount() {
        int userCount = UserManager.getIns().getRemoteSize() + 1;
        int screenUserSize = UserManager.getIns().getScreenSize();
        if (userCount < 4) {
            if (screenUserSize > 1) {
                userCount = userCount + screenUserSize - 1;
            } else {
                userCount += screenUserSize;
            }
        } else {
            if (screenUserSize > 0) {
                userCount = userCount + screenUserSize - 1;
            }
        }
        return userCount;
    }
}
