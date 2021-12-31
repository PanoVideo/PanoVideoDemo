package video.pano.panocall.callback;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcMessageService;
import com.pano.rtc.api.model.RtcPropertyAction;

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
    public void onServiceStateChanged(Constants.MessageServiceState state, Constants.QResult reason) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMessageService.Callback callback : mListeners) {
                callback.onServiceStateChanged(state, reason);
            }
        });
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

    @Override
    public void onPropertyChanged(RtcPropertyAction[] props) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMessageService.Callback callback : mListeners) {
                callback.onPropertyChanged(props);
            }
        });
    }
}
