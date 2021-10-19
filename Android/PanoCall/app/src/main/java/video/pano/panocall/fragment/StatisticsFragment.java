package video.pano.panocall.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.RtcMediaStatsObserver;
import com.pano.rtc.api.model.stats.RtcAudioRecvStats;
import com.pano.rtc.api.model.stats.RtcAudioSendStats;
import com.pano.rtc.api.model.stats.RtcSystemStats;
import com.pano.rtc.api.model.stats.RtcVideoBweStats;
import com.pano.rtc.api.model.stats.RtcVideoRecvStats;
import com.pano.rtc.api.model.stats.RtcVideoSendStats;

import video.pano.panocall.rtc.PanoRtcEngine;

public class StatisticsFragment extends Fragment implements RtcMediaStatsObserver {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PanoRtcEngine.getIns().registerRtcMediaStatsObserver(this);
    }

    @Override
    public void onVideoSendStats(RtcVideoSendStats stats) {

    }

    @Override
    public void onVideoRecvStats(RtcVideoRecvStats stats) {

    }

    @Override
    public void onAudioSendStats(RtcAudioSendStats stats) {

    }

    @Override
    public void onAudioRecvStats(RtcAudioRecvStats stats) {

    }

    @Override
    public void onScreenSendStats(RtcVideoSendStats stats) {

    }

    @Override
    public void onScreenRecvStats(RtcVideoRecvStats stats) {

    }

    @Override
    public void onVideoBweStats(RtcVideoBweStats stats) {

    }

    @Override
    public void onSystemStats(RtcSystemStats stats) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PanoRtcEngine.getIns().removeRtcMediaStatsObserver(this);
    }
}
