package video.pano.panocall.listener;

public interface PanoTopEventHandler {
    void onChannelCountDown(long remain);
    void onBCPanelVideo(boolean closed);
}
