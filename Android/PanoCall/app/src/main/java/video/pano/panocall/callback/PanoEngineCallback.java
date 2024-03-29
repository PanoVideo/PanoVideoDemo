package video.pano.panocall.callback;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngineCallback;

import java.util.ArrayList;

import static video.pano.panocall.utils.ThreadUtils.runOnUiThread;

import android.util.Log;


public class PanoEngineCallback implements RtcEngineCallback {
    private static final String TAG = "PanoEngineCallback";
    private ArrayList<RtcEngineCallback> mListeners = new ArrayList<>();

    public void addListener(RtcEngineCallback listener) {
        mListeners.add(listener);
    }

    public void removeListener(RtcEngineCallback listener) {
        mListeners.remove(listener);
    }

    // -------------------------- RTC Engine Callbacks --------------------------
    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        Log.i(TAG, "onChannelJoinConfirm result: " + result);
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onChannelJoinConfirm(result);
            }
        });
    }

    @Override
    public void onChannelLeaveIndication(Constants.QResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onChannelLeaveIndication(result);
            }
        });
    }

    @Override
    public void onChannelCountDown(long remain) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onChannelCountDown(remain);
            }
        });
    }

    @Override
    public void onUserJoinIndication(long userId, String userName) {
        Log.i(TAG, "onUserJoinIndication userId: " + userId+" , userName : "+userName);
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserJoinIndication(userId, userName);
            }
        });
    }

    @Override
    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserLeaveIndication(userId, reason);
            }
        });
    }

    @Override
    public void onUserAudioStart(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioStart(userId);
            }
        });
    }

    @Override
    public void onUserAudioStop(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioStop(userId);
            }
        });
    }

    @Override
    public void onUserAudioSubscribe(long userId, Constants.MediaSubscribeResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioSubscribe(userId, result);
            }
        });
    }

    @Override
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        Log.i(TAG, "onUserVideoStart userId: " + userId+" , maxProfile : "+maxProfile);
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoStart(userId, maxProfile);
            }
        });
    }

    @Override
    public void onUserVideoStop(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoStop(userId);
            }
        });
    }

    @Override
    public void onUserVideoSubscribe(long userId, Constants.MediaSubscribeResult result) {
        Log.i(TAG, "onUserVideoSubscribe userId: " + userId+" , result : "+result);
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoSubscribe(userId, result);
            }
        });
    }
    @Override
    public void onUserAudioMute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioMute(userId);
            }
        });
    }
    @Override
    public void onUserAudioUnmute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioUnmute(userId);
            }
        });
    }

    @Override
    public void onUserVideoMute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoMute(userId);
            }
        });
    }

    @Override
    public void onUserVideoUnmute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoUnmute(userId);
            }
        });
    }

    @Override
    public void onUserScreenStart(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserScreenStart(userId);
            }
        });
    }

    @Override
    public void onUserScreenStop(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserScreenStop(userId);
            }
        });
    }

    @Override
    public void onUserScreenSubscribe(long userId, Constants.MediaSubscribeResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserScreenSubscribe(userId, result);
            }
        });
    }

    @Override
    public void onUserScreenMute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserScreenMute(userId);
            }
        });
    }

    @Override
    public void onUserScreenUnmute(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserScreenUnmute(userId);
            }
        });
    }

    @Override
    public void onWhiteboardAvailable() {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onWhiteboardAvailable();
            }
        });
    }

    @Override
    public void onWhiteboardUnavailable() {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onWhiteboardUnavailable();
            }
        });
    }

    @Override
    public void onWhiteboardStart() {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onWhiteboardStart();
            }
        });
    }

    @Override
    public void onWhiteboardStop() {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onWhiteboardStop();
            }
        });
    }

    @Override
    public void onFirstAudioDataReceived(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onFirstAudioDataReceived(userId);
            }
        });
    }

    @Override
    public void onFirstVideoDataReceived(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onFirstVideoDataReceived(userId);
            }
        });
    }

    @Override
    public void onFirstScreenDataReceived(long userId) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onFirstScreenDataReceived(userId);
            }
        });
    }

    @Override
    public void onAudioDeviceStateChanged(String deviceId,
                                          Constants.AudioDeviceType deviceType,
                                          Constants.AudioDeviceState deviceState) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onAudioDeviceStateChanged(deviceId, deviceType, deviceState);
            }
        });
    }

    @Override
    public void onVideoDeviceStateChanged(String deviceId,
                                          Constants.VideoDeviceType deviceType,
                                          Constants.VideoDeviceState deviceState) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onVideoDeviceStateChanged(deviceId, deviceType, deviceState);
            }
        });
    }

    @Override
    public void onChannelFailover(Constants.FailoverState state) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onChannelFailover(state);
            }
        });
    }

    @Override
    public void onScreenStartResult(Constants.QResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onScreenStartResult(result);
            }
        });
    }

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onNetworkQuality(userId, quality);
            }
        });
    }

    @Override
    public void onUserAudioCallTypeChanged(long userId, Constants.AudioCallType type) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserAudioCallTypeChanged(userId, type);
            }
        });
    }
}
