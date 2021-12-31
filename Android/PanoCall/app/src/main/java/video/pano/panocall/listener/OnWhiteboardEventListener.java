package video.pano.panocall.listener;

import com.pano.rtc.api.Constants;

public interface OnWhiteboardEventListener {
    void onCreateDoc(Constants.QResult result, String fileId);
    void onSwitchDoc(Constants.QResult result, String fileId);
    void onDeleteDoc(Constants.QResult result, String fileId);
    void onPageNumberChanged(int curPage, int totalPages);
    void onViewScaleChanged(float scale);
    void onRoleTypeChanged(Constants.WBRoleType newRole);
    void onWhiteboardStop();
}
