package video.pano.panocall;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDex;

import com.pano.rtc.api.Constants;

import video.pano.panocall.info.Config;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.Utils;


public class PanoApplication extends Application {
    public static final String TAG = "PVC";

    MutableLiveData<Constants.VideoProfileType> mLocalVideoProfile = new MutableLiveData<>();
    MutableLiveData<Constants.VideoFrameRateType> mVideoFrameRateType = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        Utils.initConfig();
        PanoRtcEngine.getIns().createEngine();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public LiveData<Constants.VideoProfileType> getLocalVideoProfile() {
        return mLocalVideoProfile;
    }

    public LiveData<Constants.VideoFrameRateType> getVideoFrameRateType() {
        return mVideoFrameRateType;
    }

    public void updateVideoProfile(int profile) {
        if (!Config.sIsLocalVideoStarted) {
            return;
        }
        mLocalVideoProfile.postValue(DeviceRatingTest.getIns()
                .getProfileType(profile));
    }
    public void updateVideoFrameRateType(int frameRateType) {
        if (!Config.sIsLocalVideoStarted) {
            return;
        }
        mVideoFrameRateType.postValue(DeviceRatingTest.getIns()
                .getVideoFrameRateType(frameRateType));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PanoRtcEngine.getIns().clear();
    }

}
