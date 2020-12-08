package video.pano.panocall;

import android.util.LongSparseArray;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcEngine;


public class CallViewModel extends ViewModel implements PanoEventHandler {

    private RtcEngine mRtcEngine;
    private PanoEngineCallback mRtcCallback;
    private CallEventHandler mEventHandler;

    boolean mIsRoomJoined = false;
    boolean mIsFrontCamera = true;

    boolean mIsAudioMuted = false;
    boolean mIsVideoClosed = false;
    boolean mIsAudioSpeakerOpened = true;
    boolean mAutoMuteAudio = false;
    boolean mAutoStartCamera = true;
    boolean mEnableDebugMode = false;
    boolean mIsHost = false;

    WhiteboardState mWhiteboardState = new WhiteboardState();

    Constants.VideoProfileType mLocalProfile = Constants.VideoProfileType.Standard;
    Constants.VideoProfileType mRemoteProfile = Constants.VideoProfileType.HD1080P;
    IVideoRender.ScalingType mScalingType = IVideoRender.ScalingType.SCALE_ASPECT_FILL;

    private UserManager mUserMgr = new  UserManager();


    static class VideoUserInfo {
        long userId;
        String userName;
        Constants.VideoProfileType maxProfile;
    }
    LongSparseArray<VideoUserInfo> mVideoUsers = new LongSparseArray<>();
    LongSparseArray<VideoUserInfo> mScreenUsers = new LongSparseArray<>();
    long mLargeViewUserId = 0;


    @Override
    protected void onCleared() {
        super.onCleared();
    }

    void reset() {
        if (mRtcCallback != null) {
            mRtcCallback.removeHandler(this);
            mRtcCallback = null;
        }
        mEventHandler = null;
        mVideoUsers.clear();
        mScreenUsers.clear();
        mUserMgr.clear();
        mIsRoomJoined = false;
        mIsFrontCamera = true;
        mEnableDebugMode = false;
    }

    void setPanoRtcEngine(RtcEngine engine) {
        mRtcEngine = engine;
    }
    RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    void setPanoEngineCallback(PanoEngineCallback callback) {
        if (mRtcCallback != null) {
            mRtcCallback.removeHandler(this);
        }
        mRtcCallback = callback;
        if (mRtcCallback != null) {
            mRtcCallback.addHandler(this);
        }
    }

    void setCallEventHandler(CallEventHandler handler) {
        mEventHandler = handler;
    }

    UserManager getUserManager() {
        return mUserMgr;
    }

    long getLocalUserId() {
        UserManager.UserInfo localUser = mUserMgr.getLocalUser();
        return localUser != null ? localUser.userId : 0;
    }

    void addVideoUser(UserManager.UserInfo user) {
        if (user.isVideoStarted()) {
            CallViewModel.VideoUserInfo vui = new CallViewModel.VideoUserInfo();
            vui.userId = user.userId;
            vui.userName = user.userName;
            vui.maxProfile = user.maxProfile;
            mVideoUsers.put(user.userId, vui);
        }
    }

    void addScreenUser(UserManager.UserInfo user) {
        if (user.isScreenStarted()) {
            CallViewModel.VideoUserInfo vui = new CallViewModel.VideoUserInfo();
            vui.userId = user.userId;
            vui.userName = user.userName;
            vui.maxProfile = user.maxProfile;
            mScreenUsers.put(user.userId, vui);
        }
    }


    // -------------------------- PANO Event Handler --------------------------
    public void onChannelJoinConfirm(Constants.QResult result) {
        if (mEventHandler != null) {
            mEventHandler.onRoomJoinConfirm(result);
        }
    }

    public void onChannelLeaveIndication(Constants.QResult result) {
        if (mEventHandler != null) {
            mEventHandler.onRoomLeaveIndication(result);
        }
    }
    public void onChannelCountDown(long remain) {}
    public void onUserJoinIndication(long userId, String userName) {
        UserManager.UserInfo user = new UserManager.UserInfo(userId, userName);
        mUserMgr.addRemoteUser(user);
        if (mEventHandler != null) {
            mEventHandler.onUserJoinIndication(user);
        }
    }
    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateVideoState(false);
            user.updateScreenState(false);
        }
        mUserMgr.removeRemoteUser(userId);
        mVideoUsers.remove(userId);
        mScreenUsers.remove(userId);
        if (mEventHandler != null) {
            mEventHandler.onUserLeaveIndication(user, reason);
        }
    }
    public void onUserAudioStart(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateAudioState(true);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioStart(user);
            }
        }
    }
    public void onUserAudioStop(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateAudioState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioStop(user);
            }
        }
    }
    public void onUserAudioSubscribe(long userId, Constants.MediaSubscribeResult result) {

    }
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.maxProfile = maxProfile;
            user.updateVideoState(true);
            addVideoUser(user);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoStart(user);
            }
        }
    }
    public void onUserVideoStop(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateVideoState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoStop(user);
            }
        }
        mVideoUsers.remove(userId);
    }
    public void onUserVideoSubscribe(long userId, Constants.MediaSubscribeResult result) {

    }
    public void onUserAudioMute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateAudioMuteState(true);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioMute(user);
            }
        }
    }
    public void onUserAudioUnmute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateAudioMuteState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioUnmute(user);
            }
        }
    }
    public void onUserVideoMute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateVideoMuteState(true);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoMute(user);
            }
        }
    }
    public void onUserVideoUnmute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateVideoMuteState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoUnmute(user);
            }
        }
    }

    public void onUserScreenStart(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateScreenState(true);
            addScreenUser(user);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenStart(user);
            }
        }
    }
    public void onUserScreenStop(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateScreenState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenStop(user);
            }
        }
        mScreenUsers.remove(userId);
    }
    public void onUserScreenSubscribe(long userId, Constants.MediaSubscribeResult result) {

    }
    public void onUserScreenMute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateScreenMuteState(true);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenMute(user);
            }
        }
    }
    public void onUserScreenUnmute(long userId) {
        UserManager.UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.updateScreenMuteState(false);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenUnmute(user);
            }
        }
    }

    @Override
    public void onWhiteboardAvailable() {
        mWhiteboardState.setAvailable(true);
    }

    @Override
    public void onWhiteboardUnavailable() {
        mWhiteboardState.setAvailable(false);
    }

    @Override
    public void onWhiteboardStart() {
        mWhiteboardState.setStarted(true);
    }

    @Override
    public void onWhiteboardStop() {
        mWhiteboardState.setStarted(false);
    }

    @Override
    public void onFirstAudioDataReceived(long userId) {

    }

    @Override
    public void onFirstVideoDataReceived(long userId) {

    }

    @Override
    public void onFirstScreenDataReceived(long userId) {

    }

    @Override
    public void onAudioDeviceStateChanged(String deviceId,
                                          Constants.AudioDeviceType deviceType,
                                          Constants.AudioDeviceState deviceState) {

    }

    @Override
    public void onVideoDeviceStateChanged(String deviceId,
                                          Constants.VideoDeviceType deviceType,
                                          Constants.VideoDeviceState deviceState) {

    }

    @Override
    public void onChannelFailover(Constants.FailoverState state) {

    }


    static class WhiteboardState extends LiveData<WhiteboardState> {
        private boolean mIsAvailable = false;
        private boolean mIsStarted = false;
        private boolean mIsContentUpdated = false;
        private Constants.WBRoleType mRoleType = Constants.WBRoleType.Attendee;
        Constants.WBToolType mToolType = Constants.WBToolType.Select;

        void setAvailable(boolean available) {
            mIsAvailable = available;
            postValue(this);
        }
        void setStarted(boolean started) {
            mIsStarted = started;
            postValue(this);
        }
        void setContentUpdated(boolean updated) {
            mIsContentUpdated = updated;
            postValue(this);
        }
        void setRoleType(Constants.WBRoleType type) {
            mRoleType = type;
            postValue(this);
        }

        boolean isAvailable() {
            return mIsAvailable && (mRoleType == Constants.WBRoleType.Admin || mIsStarted);
        }
        boolean isStarted() {
            return mIsStarted;
        }
        boolean isContentUpdated() {
            return mIsContentUpdated;
        }
        Constants.WBRoleType getRoleType() {
            return mRoleType;
        }
    }

    public interface CallEventHandler {
        void onRoomJoinConfirm(Constants.QResult result);
        void onRoomLeaveIndication(Constants.QResult result);
        void onUserJoinIndication(UserManager.UserInfo user);
        void onUserLeaveIndication(UserManager.UserInfo user, Constants.UserLeaveReason reason);
        void onUserAudioStart(UserManager.UserInfo user);
        void onUserAudioStop(UserManager.UserInfo user);
        void onUserAudioMute(UserManager.UserInfo user);
        void onUserAudioUnmute(UserManager.UserInfo user);
        void onUserVideoStart(UserManager.UserInfo user);
        void onUserVideoStop(UserManager.UserInfo user);
        void onUserVideoMute(UserManager.UserInfo user);
        void onUserVideoUnmute(UserManager.UserInfo user);
        void onUserScreenStart(UserManager.UserInfo user);
        void onUserScreenStop(UserManager.UserInfo user);
        void onUserScreenMute(UserManager.UserInfo user);
        void onUserScreenUnmute(UserManager.UserInfo user);
        void onWhiteboardUnavailable();
    }
}
