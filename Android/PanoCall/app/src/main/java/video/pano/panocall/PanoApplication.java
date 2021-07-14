package video.pano.panocall;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.PanoAnnotationManager;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.RtcEngineCallback;
import com.pano.rtc.api.RtcEngineConfig;
import com.pano.rtc.api.RtcWhiteboard;

import java.util.Locale;
import java.util.UUID;

import video.pano.panocall.callback.PanoAnnotationCallback;
import video.pano.panocall.callback.PanoEngineCallback;
import video.pano.panocall.callback.PanoWhiteboardCallback;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.Utils;

import static video.pano.panocall.info.Config.APPID;
import static video.pano.panocall.info.Config.PANO_SERVER;
import static video.pano.panocall.info.Constant.KEY_APP_UUID;
import static video.pano.panocall.info.Constant.KEY_SCREEN_CAPTURE_FPS;


public class PanoApplication extends Application {
    public static final String TAG = "PVC";

    private RtcEngine mRtcEngine;
    private RtcWhiteboard mRtcWhiteboard ;
    private PanoEngineCallback mRtcCallback = new PanoEngineCallback();
    private PanoWhiteboardCallback mWhiteboardCallback = new PanoWhiteboardCallback();
    private PanoAnnotationCallback mAnnotationCallback = new PanoAnnotationCallback(this);

    protected Constants.AudioAecType mAudioAecType = Constants.AudioAecType.Default;
    protected boolean mHwAcceleration = false;

    public boolean mIsLocalVideoStarted = false; // 此变量用于通知美颜页本地视频是否开启
    public boolean mIsFrontCamera = true; // 此变量用于通知美颜页当前是否是前置摄像头
    public String mFeedbackRoomId;
    public long mFeedbackUserId;
    public String mFeedbackUserName;
    private String mAppUuid;

    MutableLiveData<Constants.VideoProfileType> mLocalVideoProfile = new MutableLiveData<>();
    private String mScreenOptMode;


    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mScreenOptMode = prefs.getString(KEY_SCREEN_CAPTURE_FPS, "0");
        mAppUuid = prefs.getString(KEY_APP_UUID, "");
        if (!TextUtils.isEmpty(mAppUuid)) {
            UUID uuid = UUID.randomUUID();
            mAppUuid = uuid.toString().replace("-", "");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_APP_UUID, mAppUuid);
            editor.apply();
        }
        createPanoEngine();
    }

    private void createPanoEngine(){
        // 设置PANO媒体引擎的配置参数
        RtcEngineConfig engineConfig = new RtcEngineConfig();
        engineConfig.appId = APPID;
        engineConfig.server = PANO_SERVER;
        engineConfig.context = getApplicationContext();
        engineConfig.callback = mRtcCallback;
        engineConfig.audioAecType = mAudioAecType;
        engineConfig.videoCodecHwAcceleration = mHwAcceleration;
        try {
            mRtcEngine = RtcEngine.create(engineConfig);
            mRtcWhiteboard = mRtcEngine.getWhiteboard();
            mRtcWhiteboard.setCallback(mWhiteboardCallback);
            mRtcEngine.getAnnotationMgr().setCallback(mAnnotationCallback);
            mRtcEngine.setOption(Constants.PanoOptionType.ScreenOptimization,
                    !"0".equals(mScreenOptMode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RtcEngine getPanoEngine() { return mRtcEngine; }
    public RtcWhiteboard getPanoWhiteboard(){
        return mRtcWhiteboard ;
    }

    public String getAppUuid() { return mAppUuid; }
    public void updateLanguage(int lang) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        switch (lang) {
            case 1: // English
                configuration.setLocale(Locale.ENGLISH);
                break;
            case 2: // Simple chinese
                configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
                break;
            default:
                configuration.setLocale(Locale.getDefault());
                break;
        }
        resources.updateConfiguration(configuration, displayMetrics);
    }
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

    public void registerEventHandler(RtcEngineCallback handler) { mRtcCallback.addListener(handler); }
    public void removeEventHandler(RtcEngineCallback handler) { mRtcCallback.removeListener(handler); }

    public void registerWhiteboardHandler(RtcWhiteboard.Callback handler) { mWhiteboardCallback.addListener(handler); }
    public void removeWhiteboardHandler(RtcWhiteboard.Callback handler) { mWhiteboardCallback.removeListener(handler); }

    public void registerAnnotationCallback(PanoAnnotationManager.Callback handler){ mAnnotationCallback.addListener(handler); }
    public void removeAnnotationCallback(PanoAnnotationManager.Callback handler){ mAnnotationCallback.removeListener(handler); }

    public void refreshPanoEngine() {
        RtcEngine.destroy();
        createPanoEngine();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }
}
