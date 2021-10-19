package video.pano.panocall.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.PanoAnnotationManager;

import video.pano.panocall.activity.CallActivity;
import video.pano.panocall.rtc.PanoRtcEngine;

public class BaseSettingFragment extends Fragment implements PanoAnnotationManager.Callback{

    boolean isResume = false ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanoRtcEngine.getIns().registerAnnotationCallback(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PanoRtcEngine.getIns().removeAnnotationCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isResume = true ;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResume = false ;
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        if(getActivity() != null && !getActivity().isFinishing() && isResume){
            getActivity().finish();
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        if(getActivity() != null && !getActivity().isFinishing() && isResume){
            getActivity().finish();
        }
    }
}
