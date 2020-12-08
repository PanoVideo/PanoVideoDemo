package video.pano.panocall;


import com.pano.rtc.api.Constants;

public interface PanoWhiteboardHandler {
    void onPageNumberChanged(int curPage, int totalPages);

    void onImageStateChanged(String url, Constants.WBImageState state);

    void onViewScaleChanged(float scale);

    void onRoleTypeChanged(Constants.WBRoleType newRole);

    void onContentUpdated();

    void onMessage(long userId, byte[] bytes);
}
