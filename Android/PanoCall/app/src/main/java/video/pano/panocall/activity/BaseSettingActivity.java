package video.pano.panocall.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pano.rtc.api.PanoAnnotationManager;

import video.pano.panocall.rtc.PanoRtcEngine;

public class BaseSettingActivity extends AppCompatActivity implements PanoAnnotationManager.Callback {

    boolean isResume = false ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanoRtcEngine.getIns().registerAnnotationCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PanoRtcEngine.getIns().removeAnnotationCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResume = false ;
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        if(isResume) finish();
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        if(isResume) finish();
    }
}
