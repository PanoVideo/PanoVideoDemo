package video.pano.panocall.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.model.stats.RtcAudioRecvStats;
import com.pano.rtc.api.model.stats.RtcAudioSendStats;

import video.pano.panocall.R;

public class StatisticsAudioFragment extends StatisticsFragment {

    private TextView mSendBitrateText;
    private TextView mReceiveBitrateText;
    private TextView mSendLostRatioText;
    private TextView mReceiveLostRatioText;
    private TextView mSendRoundTripText;

    public static Fragment newInstance(){
        StatisticsAudioFragment fragment = new StatisticsAudioFragment();
        return fragment ;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_audio,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        mSendBitrateText = view.findViewById(R.id.tv_bitrate_send);
        mReceiveBitrateText = view.findViewById(R.id.tv_bitrate_receive);
        mSendLostRatioText = view.findViewById(R.id.tv_lost_ratio_send);
        mReceiveLostRatioText = view.findViewById(R.id.tv_lost_ratio_receive);
        mSendRoundTripText = view.findViewById(R.id.tv_round_trip_time_send);
    }

    @Override
    public void onAudioRecvStats(RtcAudioRecvStats stats) {
        long bitrate = stats.bitrate / 1000;
        mReceiveBitrateText.setText(bitrate+"kb/s");
        mReceiveLostRatioText.setText((int)stats.lossRatio+"%");
    }

    @Override
    public void onAudioSendStats(RtcAudioSendStats stats) {
        long bitrate = stats.bitrate / 1000;
        mSendBitrateText.setText(bitrate+"kb/s");
        mSendLostRatioText.setText((int)stats.lossRatio+"%");
        mSendRoundTripText.setText(stats.rtt+"ms");
    }
}
