package video.pano.panocall.listener;

import video.pano.panocall.model.UserInfo;

public interface OnActEventListener {
    void onChannelLeaveIndication();
    void onUserJoinIndication();
    void onUserLeaveIndication(UserInfo userInfo);
    void onWhiteboardStart();
    void onChannelFailover(boolean show);
    void onShowHideControlPanel();
    void onVideoAnnotationStart(long userId);
    void onVideoAnnotationStop(long userId);
    void onShareAnnotationStart(long userId);
    void onShareAnnotationStop(long userId);
    void onUserScreenStop(UserInfo user);
    void onUserScreenStart(UserInfo user);
    void onClickAnnotationStop();
    void onTCPanelSwitchCamera();
    void onTCPanelExit();
    void onBCPanelShare();
    void onBCPanelUserList();
    void onBCPanelMore();

}
