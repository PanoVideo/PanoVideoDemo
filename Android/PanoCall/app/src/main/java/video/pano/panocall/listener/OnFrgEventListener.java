package video.pano.panocall.listener;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.model.RtcAudioLevel;
import com.pano.rtc.api.model.RtcPropertyAction;

import video.pano.panocall.model.UserInfo;

public interface OnFrgEventListener {
    void onUserJoinIndication(UserInfo user);
    void onUserLeaveIndication(UserInfo user);
    void subscribeLocalVideo();
    void onUserAudioMute(UserInfo user);
    void onUserAudioUnmute(UserInfo user);
    void updateUserAudioState(UserInfo user);
    void onUserAudioStart(UserInfo user);
    void onUserAudioStop(UserInfo user);
    void onUserVideoStart(UserInfo user);
    void onUserVideoStop(UserInfo user);
    void onUserScreenStart(long userId);
    void onUserScreenStop(UserInfo user);
    void onNetworkQuality(long userId, Constants.QualityRating quality);
    void onUserAudioCallTypeChanged(UserInfo user);
    void onDestinationChangedListener();
    void onVideoAnnotationStart(long userId, int streamId);
    void onVideoAnnotationStop(long userId);
    void onShareAnnotationStart(long userId);
    void onShareAnnotationStop(long userId);
    void onCheckState();
    void stopLocalVideo();
    void startLocalVideo();
    void onSwitchCamera();

    default void onHostUserIdChanged(long hostId){}
    default void onClickAnnotationStop(){}
    default void onAnnotationToolsClick(){}
    default void onWhiteboardStart(){}

}
