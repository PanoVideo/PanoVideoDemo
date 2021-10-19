package video.pano.panocall;

import static video.pano.panocall.info.Constant.KEY_APP_UUID;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDex;

import com.pano.rtc.api.Constants;

import java.util.Locale;
import java.util.UUID;

import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;


public class PanoApplication extends Application {
    public static final String TAG = "PVC";

    public boolean mIsLocalVideoStarted = false; // 此变量用于通知美颜页本地视频是否开启
    public boolean mIsFrontCamera = true; // 此变量用于通知美颜页当前是否是前置摄像头
    public String mFeedbackRoomId;
    public String mFeedbackUserName;
    public long mFeedbackUserId;

    private String mAppUuid;

    MutableLiveData<Constants.VideoProfileType> mLocalVideoProfile = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        Utils.initConfig();

        mAppUuid = SPUtils.getString(KEY_APP_UUID, "");
        if (TextUtils.isEmpty(mAppUuid)) {
            UUID uuid = UUID.randomUUID();
            mAppUuid = uuid.toString().replace("-", "");
            SPUtils.put(KEY_APP_UUID, mAppUuid);
        }

        PanoRtcEngine.getIns().createEngine();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public String getAppUuid() { return mAppUuid; }
    public void updateVideoProfile(int profile) {
        if (!mIsLocalVideoStarted) {
            return;
        }
        mLocalVideoProfile.postValue(DeviceRatingTest.getIns()
                .getProfileType(profile));
    }
    public LiveData<Constants.VideoProfileType> getLocalVideoProfile() {
        return mLocalVideoProfile;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PanoRtcEngine.getIns().clear();
    }

}
