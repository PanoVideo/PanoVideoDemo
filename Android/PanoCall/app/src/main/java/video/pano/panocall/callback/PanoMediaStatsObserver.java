package video.pano.panocall.callback;

import static video.pano.panocall.utils.ThreadUtils.runOnUiThread;

import com.pano.rtc.api.RtcMediaStatsObserver;
import com.pano.rtc.api.model.stats.RtcAudioRecvStats;
import com.pano.rtc.api.model.stats.RtcAudioSendStats;
import com.pano.rtc.api.model.stats.RtcSystemStats;
import com.pano.rtc.api.model.stats.RtcVideoBweStats;
import com.pano.rtc.api.model.stats.RtcVideoRecvStats;
import com.pano.rtc.api.model.stats.RtcVideoSendStats;

import java.util.ArrayList;
import java.util.List;

public class PanoMediaStatsObserver implements RtcMediaStatsObserver {

    private List<RtcMediaStatsObserver> mListeners = new ArrayList<>();

    public void addListener(RtcMediaStatsObserver observer) {
        mListeners.add(observer);
    }

    public void removeListener(RtcMediaStatsObserver observer) {
        mListeners.remove(observer);
    }

    @Override
    public void onVideoSendStats(RtcVideoSendStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onVideoSendStats(stats);
            }
        });
    }

    @Override
    public void onVideoRecvStats(RtcVideoRecvStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onVideoRecvStats(stats);
            }
        });
    }

    @Override
    public void onAudioSendStats(RtcAudioSendStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onAudioSendStats(stats);
            }
        });
    }

    @Override
    public void onAudioRecvStats(RtcAudioRecvStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onAudioRecvStats(stats);
            }
        });
    }

    @Override
    public void onScreenSendStats(RtcVideoSendStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onScreenSendStats(stats);
            }
        });
    }

    @Override
    public void onScreenRecvStats(RtcVideoRecvStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onScreenRecvStats(stats);
            }
        });
    }

    @Override
    public void onVideoBweStats(RtcVideoBweStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onVideoBweStats(stats);
            }
        });
    }

    @Override
    public void onSystemStats(RtcSystemStats stats) {
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        runOnUiThread(()-> {
            for (RtcMediaStatsObserver observer : mListeners) {
                observer.onSystemStats(stats);
            }
        });
    }
}
