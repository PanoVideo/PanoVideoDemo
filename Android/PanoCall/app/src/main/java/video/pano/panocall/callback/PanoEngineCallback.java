package video.pano.panocall.callback;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngineCallback;

import java.util.ArrayList;

import static video.pano.panocall.utils.ThreadUtils.runOnUiThread;


public class PanoEngineCallback implements RtcEngineCallback {
    private ArrayList<RtcEngineCallback> mListeners = new ArrayList<>();

    public void addListener(RtcEngineCallback listener) {
        mListeners.add(listener);
    }

    public void removeListener(RtcEngineCallback listener) {
        mListeners.remove(listener);
    }

    // -------------------------- RTC Engine Callbacks --------------------------
    public void onChannelJoinConfirm(Constants.QResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onChannelJoinConfirm(result);
            }
        });
    }

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
    public void onUserJoinIndication(long userId, String userName) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserJoinIndication(userId, userName);
            }
        });
    }
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
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoStart(userId, maxProfile);
            }
        });
    }
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
    public void onUserVideoSubscribe(long userId, Constants.MediaSubscribeResult result) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onUserVideoSubscribe(userId, result);
            }
        });
    }
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
    public void onActiveSpeakerListUpdated(long[] userIds) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcEngineCallback callback : mListeners) {
                callback.onActiveSpeakerListUpdated(userIds);
            }
        });
    }


}
