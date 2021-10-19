package video.pano.panocall.callback;

import com.pano.rtc.api.RtcMessageService;

import java.util.ArrayList;

import static video.pano.panocall.utils.ThreadUtils.runOnUiThread;

public class PanoMessageCallback implements RtcMessageService.Callback {

    private ArrayList<RtcMessageService.Callback> mListeners = new ArrayList<>();

    public void addListener(RtcMessageService.Callback listener) {
        mListeners.add(listener);
    }

    public void removeListener(RtcMessageService.Callback listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onUserMessage(long userId, byte[] data) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMessageService.Callback callback : mListeners) {
                callback.onUserMessage(userId, data);
            }
        });
    }
}
