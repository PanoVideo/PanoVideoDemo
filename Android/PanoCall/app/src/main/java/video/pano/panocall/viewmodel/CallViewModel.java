package video.pano.panocall.viewmodel;

import androidx.lifecycle.ViewModel;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.model.RtcAudioLevel;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.PanoAnnotationHandler;
import video.pano.panocall.listener.PanoCallEventHandler;
import video.pano.panocall.listener.PanoTopEventHandler;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.model.WhiteboardState;


public class CallViewModel extends ViewModel{

    private RtcEngine mRtcEngine;

    private PanoCallEventHandler mEventHandler;
    private PanoTopEventHandler mTopHandler ;
    private PanoAnnotationHandler mAnnotationHandler ;

    public boolean mIsRoomJoined = false;
    public boolean mIsFrontCamera = true;

    public boolean mIsAudioMuted = false;
    public boolean mIsVideoClosed = false;
    public boolean mIsAudioSpeakerOpened = true;
    public boolean mAutoMuteAudio = false;
    public boolean mAutoStartCamera = true;
    public boolean mEnableDebugMode = false;

    public WhiteboardState mWhiteboardState = new WhiteboardState();

    public Constants.VideoProfileType mLocalProfile = Constants.VideoProfileType.Standard;

    public long mLargeViewUserId = 0;

    private final UserManager mUserMgr = new UserManager();

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void reset() {
        mEventHandler = null;
        mTopHandler = null ;
        mAnnotationHandler = null ;
        mUserMgr.clear();
        mIsRoomJoined = false;
        mIsFrontCamera = true;
        mEnableDebugMode = false;
    }

    public void setPanoRtcEngine(RtcEngine engine) {
        mRtcEngine = engine;
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public void setCallEventHandler(PanoCallEventHandler handler) {
        mEventHandler = handler;
    }

    public void seTopEventHandler(PanoTopEventHandler handler) {
        mTopHandler = handler;
    }

    public void seAnnotationEventHandler(PanoAnnotationHandler handler) {
        mAnnotationHandler = handler;
    }

    public UserManager getUserManager() {
        return mUserMgr;
    }

    public long getLocalUserId() {
        UserInfo localUser = mUserMgr.getLocalUser();
        return localUser != null ? localUser.userId : 0;
    }

    void addVideoUser(UserInfo user) {
        if (user.isVideoStarted()) {
            mUserMgr.addVideoUser(user);
        }
    }

    void addScreenUser(UserInfo user) {
        if (user.isScreenStarted()) {
            mUserMgr.addScreenUser(user);
        }
    }

    /**************  RtcEngineCallback  *************************/
    public void onChannelJoinConfirm(Constants.QResult result) {
        if (mEventHandler != null) {
            mEventHandler.onChannelJoinConfirm(result);
        }
    }

    public void onChannelLeaveIndication(Constants.QResult result) {
        if (mEventHandler != null) {
            mEventHandler.onChannelLeaveIndication(result);
        }
    }

    public void onChannelCountDown(long remain) {
        if(mTopHandler != null){
            mTopHandler.onChannelCountDown(remain);
        }
    }

    public void onUserJoinIndication(long userId, String userName) {
        UserInfo user = new UserInfo(userId, userName);
        mUserMgr.addRemoteUser(user);
        if (mEventHandler != null) {
            mEventHandler.onUserJoinIndication(user);
        }
    }

    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setVideoStarted(false);
            user.setScreenStarted(false);
        }
        mUserMgr.removeRemoteUser(userId);
        mUserMgr.removeScreenUser(userId);
        mUserMgr.removeVideoUser(userId);
        if (mEventHandler != null) {
            mEventHandler.onUserLeaveIndication(user, reason);
        }
    }

    public void onUserAudioStart(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setAudioStared(true);
        }
    }

    public void onUserAudioStop(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setAudioStared(false);
        }
    }

    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.maxProfile = maxProfile;
            user.setVideoStarted(true);
            addVideoUser(user);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoStart(user);
            }
        }
    }

    public void onUserVideoStop(long userId) {
        mUserMgr.removeVideoUser(userId);
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setVideoStarted(false);
            if (mEventHandler != null) {
                mEventHandler.onUserVideoStop(user);
            }
        }
    }

    public void onUserAudioMute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(true);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioMute(user);
            }
        }
    }

    public void onUserAudioUnmute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setAudioMuted(false);
            if (mEventHandler != null) {
                mEventHandler.onUserAudioUnmute(user);
            }
        }
    }

    public void onUserVideoMute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setVideoMuted(true);
        }
    }

    public void onUserVideoUnmute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setVideoMuted(false);
        }
    }

    public void onUserScreenStart(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setScreenStarted(true);
            addScreenUser(user);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenStart(user);
            }
        }
    }

    public void onUserScreenStop(long userId) {
        mUserMgr.removeScreenUser(userId);
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setScreenStarted(false);
            if (mEventHandler != null) {
                mEventHandler.onUserScreenStop(user);
            }
        }
    }

    public void onUserScreenMute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setScreenMuted(true);
        }
    }

    public void onUserScreenUnmute(long userId) {
        UserInfo user = mUserMgr.getRemoteUser(userId);
        if (user != null) {
            user.setScreenMuted(false);
        }
    }

    public void onWhiteboardAvailable() {
        mWhiteboardState.setAvailable(true);
    }

    public void onWhiteboardUnavailable() {
        mWhiteboardState.setAvailable(false);
    }

    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        if (mEventHandler != null) {
            mEventHandler.onNetworkQuality(userId, quality);
        }
    }
    /**************  RtcEngineCallback  **************/

    /**************  RtcWhiteboard  **************/
    public void onPageNumberChanged(int curPage, int totalPages) {
        if (mEventHandler != null) {
            mEventHandler.onPageNumberChanged(curPage,totalPages);
        }
    }

    public void onViewScaleChanged(float scale) {
        if (mEventHandler != null) {
            mEventHandler.onViewScaleChanged(scale);
        }
    }

    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        if (mEventHandler != null) {
            mEventHandler.onRoleTypeChanged(newRole);
        }
    }

    public void onMessage(long userId, byte[] msg) {
        if(mEventHandler != null){
            mEventHandler.onMessage(userId, msg);
        }
    }

    public void onUserJoined(long userId, String userName){
        mUserMgr.addWhiteboardUser(new UserInfo(userId, userName));
        if(mEventHandler != null){
            mEventHandler.onUserJoined(userId, userName);
        }
    }

    public void onUserLeft(long userId) {
        mUserMgr.removeWhiteboardUser(userId);
        if(mEventHandler != null){
            mEventHandler.onUserLeft(userId);
        }
    }

    public void onWhiteboardStop() {
        if(mEventHandler != null){
            mEventHandler.onWhiteboardStop();
        }
    }

    public void onCreateDoc(Constants.QResult result, String fileId) {
        if(mEventHandler != null){
            mEventHandler.onCreateDoc(result, fileId);
        }
    }

    public void onSwitchDoc(Constants.QResult result, String fileId) {
        if(mEventHandler != null){
            mEventHandler.onSwitchDoc(result, fileId);
        }
    }

    public void onDeleteDoc(Constants.QResult result, String fileId) {
        if(mEventHandler != null){
            mEventHandler.onDeleteDoc(result, fileId);
        }
    }

    /**************  RtcWhiteboard  **************/

    /**************  RtcWhiteboard.Callback   **************/
    public void onVideoAnnotationStart(long userId, int streamId) {
        if(mEventHandler != null){
            mEventHandler.onVideoAnnotationStart(userId, streamId);
        }
    }

    public void onVideoAnnotationStop(long userId, int streamId) {
        if(mEventHandler != null){
            mEventHandler.onVideoAnnotationStop(userId, streamId);
        }
    }

    public void onShareAnnotationStart(long userId) {
        if(mEventHandler != null){
            mEventHandler.onShareAnnotationStart(userId);
        }
    }

    public void onShareAnnotationStop(long userId) {
        if(mEventHandler != null){
            mEventHandler.onShareAnnotationStop(userId);
        }
    }

    /**************  RtcWhiteboard.Callback   **************/

    /**************** Annotation ****************/
    public void onAnnotationStart(){
        if(mAnnotationHandler != null){
            mAnnotationHandler.onAnnotationStart();
        }
    }

    public void onAnnotationStop() {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onAnnotationStop();
        }
    }

    public void onClickPencil(boolean checked) {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onClickPencil(checked);
        }
    }

    public void onClickArrow(boolean checked) {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onClickArrow(checked);
        }
    }

    public void onClickEraser(boolean checked) {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onClickEraser(checked);
        }
    }

    public void onProgressChange(int progress) {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onProgressChange(progress);
        }
    }

    public void onCheckedColor(int index, int colorId) {
        if(mAnnotationHandler != null){
            mAnnotationHandler.onCheckedColor(index,colorId);
        }
    }
    /**************** Annotation ****************/

    public void onBCPanelVideo(boolean closed){
        if(mTopHandler != null){
            mTopHandler.onBCPanelVideo(closed);
        }
    }

}
