package video.pano.panocall.listener;

public interface OnBottomControlPanelListener {
    void onBCPanelAudio(boolean muted);
    void onBCPanelVideo(boolean closed);
    void onBCPanelShare();
    void onBCPanelUserList();
    void onBCPanelMore();
}
