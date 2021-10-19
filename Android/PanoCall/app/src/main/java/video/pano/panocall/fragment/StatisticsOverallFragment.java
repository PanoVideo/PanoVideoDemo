package video.pano.panocall.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.model.stats.RtcSystemStats;
import com.pano.rtc.api.model.stats.RtcVideoSendStats;

import video.pano.panocall.R;
import video.pano.panocall.utils.NetUtils;
import video.pano.panocall.utils.Utils;

public class StatisticsOverallFragment extends StatisticsFragment {

    private TextView mCpuTitleText;
    private TextView mCpuPercentText;
    private ProgressBar mCpuProgress;
    private TextView mRamTitleText;
    private TextView mRamPercentText;
    private ProgressBar mRamProgress;
    private TextView mBandwidthText;
    private TextView mBandwidthPercentText;

    public static Fragment newInstance() {
        StatisticsOverallFragment fragment = new StatisticsOverallFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_overall, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCpuViews(view);
        initRamViews(view);
        initBandwidthViews(view);
        setData();
    }

    private void setData() {
//        int cpuCore = Utils.getCPUCores();
        int cpuCore = Runtime.getRuntime().availableProcessors();
        if (cpuCore > 0) {
            mCpuTitleText.setText(getString(R.string.title_statistics_cpu_core, cpuCore));
        } else {
            mCpuTitleText.setText("—");
        }
        if (NetUtils.isNetConnected(Utils.getApp())) {
            mBandwidthText.setText(NetUtils.getNetworkState(Utils.getApp()));
        }else{
            mBandwidthText.setText("—");
        }
    }

    private void initCpuViews(View view){
        mCpuTitleText = view.findViewById(R.id.tv_cpu_title);
        mCpuPercentText = view.findViewById(R.id.tv_cpu_percent);
        mCpuProgress = view.findViewById(R.id.progress_cpu);
        View cpuTitleView = view.findViewById(R.id.fl_cpu_title);
        View cpuContentView = view.findViewById(R.id.cl_cpu_content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cpuContentView.setVisibility(View.GONE);
            cpuTitleView.setVisibility(View.GONE);
        }else{
            cpuContentView.setVisibility(View.VISIBLE);
            cpuTitleView.setVisibility(View.VISIBLE);
        }
    }

    private void initRamViews(View view){
        mRamTitleText = view.findViewById(R.id.tv_ram_title);
        mRamPercentText = view.findViewById(R.id.tv_ram_percent);
        mRamProgress = view.findViewById(R.id.progress_ram);
    }

    private void initBandwidthViews(View view){
        mBandwidthText = view.findViewById(R.id.tv_bandwidth_value);
        mBandwidthPercentText = view.findViewById(R.id.tv_bandwidth_percent);
    }

    @Override
    public void onSystemStats(RtcSystemStats stats) {
        int memorySize = (int)(stats.totalPhysMemory / 1024 / 1024 );
        int workingSize = (int)(stats.workingSetSize / 1024 );

        if(mRamTitleText != null){
            mRamTitleText.setText(getString(R.string.title_statistics_memory_size,memorySize));
        }
        if(mRamPercentText != null){
            mRamPercentText.setText(workingSize+" MB");
        }
        if(mRamProgress != null){
            float current = stats.workingSetSize / 1024 ;
            float total = stats.totalPhysMemory / 1024 ;
            int usage = (int)(current / total * 100);
            mRamProgress.setProgress(usage);
        }
        if(mCpuPercentText != null){
            mCpuPercentText.setText(stats.totalCpuUsage+"%");
        }
        if(mCpuProgress != null){
            mCpuProgress.setProgress(stats.totalCpuUsage);
        }
    }

    @Override
    public void onVideoSendStats(RtcVideoSendStats stats) {
        if(mBandwidthPercentText != null){
            long bitrate = stats.bitrate / 1000 ;
            mBandwidthPercentText.setText(getString(R.string.title_statistics_bandwidth_usage,bitrate));
        }
    }
}
