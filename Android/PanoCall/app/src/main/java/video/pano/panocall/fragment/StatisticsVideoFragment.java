package video.pano.panocall.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.model.stats.RtcVideoRecvStats;
import com.pano.rtc.api.model.stats.RtcVideoSendStats;

import video.pano.panocall.R;

public class StatisticsVideoFragment extends StatisticsFragment {

    private TextView mSendBitrateText;
    private TextView mReceiveBitrateText;
    private TextView mSendLostRatioText;
    private TextView mReceiveLostRatioText;
    private TextView mSendRoundTripText;
    private TextView mReceiveRoundTripText;
    private TextView mSendResolution;
    private TextView mReceiveResolution;
    private TextView mSendFrameRate;
    private TextView mReceiveFrameRate;

    public static Fragment newInstance(){
        StatisticsVideoFragment fragment = new StatisticsVideoFragment();
        return fragment ;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_video,container,false);
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
        mReceiveRoundTripText = view.findViewById(R.id.tv_round_trip_time_receive);
        mSendResolution = view.findViewById(R.id.tv_resolution_send);
        mReceiveResolution = view.findViewById(R.id.tv_resolution_receive);
        mSendFrameRate = view.findViewById(R.id.tv_frame_rate_send);
        mReceiveFrameRate = view.findViewById(R.id.tv_frame_rate_receive);
    }

    @Override
    public void onVideoRecvStats(RtcVideoRecvStats stats) {
        long bitrate = stats.bitrate / 1000;
        mReceiveBitrateText.setText(bitrate+"kb/s");
        mReceiveLostRatioText.setText((int)stats.lossRatio+"%");
        mReceiveFrameRate.setText(stats.framerate+"fps");
        String resolution = stats.width+" x "+ stats.height;
        mReceiveResolution.setText(resolution);
    }

    @Override
    public void onVideoSendStats(RtcVideoSendStats stats) {
        long bitrate = stats.bitrate / 1000;
        mSendBitrateText.setText(bitrate+"kb/s");
        mSendLostRatioText.setText((int)stats.lossRatio+"%");
        mSendRoundTripText.setText(stats.rtt+"ms");
        mSendFrameRate.setText(stats.framerate+"fps");
        String resolution = stats.width+" x "+ stats.height;
        mSendResolution.setText(resolution);
    }
}
