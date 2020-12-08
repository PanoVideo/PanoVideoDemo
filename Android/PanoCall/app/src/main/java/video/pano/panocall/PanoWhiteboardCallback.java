package video.pano.panocall;


import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcWhiteboard;

import java.util.ArrayList;

import static video.pano.panocall.thread.ThreadUtils.runOnUiThread;


public class PanoWhiteboardCallback implements RtcWhiteboard.Callback {
    private ArrayList<PanoWhiteboardHandler> mHandler = new ArrayList<>();

    public void addHandler(PanoWhiteboardHandler handler) {
        mHandler.add(handler);
    }

    public void removeHandler(PanoWhiteboardHandler handler) {
        mHandler.remove(handler);
    }

    // -------------------------- RTC Whiteboard Callbacks --------------------------
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onPageNumberChanged(curPage, totalPages);
            }
        });
    }
    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onImageStateChanged(url, state);
            }
        });
    }
    @Override
    public void onViewScaleChanged(float scale) {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onViewScaleChanged(scale);
            }
        });
    }
    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onRoleTypeChanged(newRole);
            }
        });
    }
    @Override
    public void onContentUpdated() {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onContentUpdated();
            }
        });
    }

    @Override
    public void onMessage(long userId, byte[] bytes) {
        runOnUiThread(()-> {
            for (PanoWhiteboardHandler handler : mHandler) {
                handler.onMessage(userId, bytes);
            }
        });
    }
}
