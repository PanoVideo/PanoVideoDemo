package video.pano.panocall.callback;


import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcWhiteboard;

import java.util.ArrayList;

import static video.pano.panocall.utils.ThreadUtils.runOnUiThread;


public class PanoWhiteboardCallback implements RtcWhiteboard.Callback {
    private ArrayList<RtcWhiteboard.Callback> mListeners = new ArrayList<>();

    public void addListener(RtcWhiteboard.Callback listener) {
        mListeners.add(listener);
    }

    public void removeListener(RtcWhiteboard.Callback listener) {
        mListeners.remove(listener);
    }

    // -------------------------- RTC Whiteboard Callbacks --------------------------
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onPageNumberChanged(curPage, totalPages);
            }
        });
    }
    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {
    }
    @Override
    public void onViewScaleChanged(float scale) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onViewScaleChanged(scale);
            }
        });
    }
    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onRoleTypeChanged(newRole);
            }
        });
    }
    @Override
    public void onContentUpdated() {
    }

    @Override
    public void onUserJoined(long userId, String userName) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onUserJoined(userId, userName);
            }
        });
    }

    @Override
    public void onUserLeft(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onUserLeft(userId);
            }
        });
    }

    @Override
    public void onMessage(long userId, byte[] bytes) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onMessage(userId, bytes);
            }
        });
    }

    @Override
    public void onVisionShareStopped(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onVisionShareStopped(userId);
            }
        });
    }

    @Override
    public void onVisionShareStarted(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onVisionShareStarted(userId);
            }
        });
    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onDeleteDoc(result, fileId);
            }
        });
    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onSwitchDoc(result, fileId);
            }
        });
    }

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcWhiteboard.Callback callback : mListeners) {
                callback.onCreateDoc(result, fileId);
            }
        });
    }
}
