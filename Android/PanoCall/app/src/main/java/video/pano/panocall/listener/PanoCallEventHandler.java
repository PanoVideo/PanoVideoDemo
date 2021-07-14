package video.pano.panocall.listener;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.model.RtcAudioLevel;

import video.pano.panocall.model.UserInfo;

public interface PanoCallEventHandler {

    /**************  RtcEngineCallback  **************/
    void onChannelJoinConfirm(Constants.QResult result);
    void onChannelLeaveIndication(Constants.QResult result);
    void onUserJoinIndication(UserInfo user);
    void onUserLeaveIndication(UserInfo user, Constants.UserLeaveReason reason);
    void onUserAudioMute(UserInfo user);
    void onUserAudioUnmute(UserInfo user);
    void onUserVideoStart(UserInfo user);
    void onUserVideoStop(UserInfo user);
    void onUserScreenStart(UserInfo user);
    void onUserScreenStop(UserInfo user);
    void onNetworkQuality(long userId, Constants.QualityRating quality);
    /**************  RtcEngineCallback  **************/

    /**************  RtcWhiteboard  **************/
    void onPageNumberChanged(int curPage, int totalPages);
    void onViewScaleChanged(float scale);
    /**************  RtcWhiteboard  **************/

    /**************  RtcWhiteboard.Callback   **************/
    void onVideoAnnotationStart(long userId, int streamId);
    void onVideoAnnotationStop(long userId, int streamId);
    void onShareAnnotationStart(long userId);
    void onShareAnnotationStop(long userId);
    void onRoleTypeChanged(Constants.WBRoleType newRole);
    void onUserJoined(long userId, String userName);
    void onUserLeft(long userId);
    void onMessage(long userId, byte[] msg);
    void onWhiteboardStop();
    void onCreateDoc(Constants.QResult result, String fileId);
    void onSwitchDoc(Constants.QResult result, String fileId);
    void onDeleteDoc(Constants.QResult result, String fileId);
    /**************  RtcWhiteboard.Callback   **************/
}
