package video.pano.panocall;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.RtcEngineConfig;
import com.pano.rtc.api.RtcWhiteboard;

import java.util.Locale;
import java.util.UUID;


public class PanoApplication extends Application {
    public static final String TAG = "PanoApplication";
    public static String APPID = Input Your AppId;
    public static String PANO_SERVER = "api.pano.video";
    public static long kMaxAudioDumpSize = 200*1024*1024; // 200 MB
    public static String APP_TOKEN = Input Your Token;
    public static String USER_ID;

    private RtcEngine mRtcEngine;
    private PanoEngineCallback mRtcCallback = new PanoEngineCallback();
    private PanoWhiteboardCallback mWhiteboardCallback = new PanoWhiteboardCallback();

    protected Constants.AudioAecType mAudioAecType = Constants.AudioAecType.Default;
    protected boolean mHwAcceleration = false;

    boolean mIsRoomJoined = false;
    boolean mIsLocalVideoStarted = false; // 此变量用于通知美颜页本地视频是否开启
    boolean mIsFrontCamera = true; // 此变量用于通知美颜页当前是否是前置摄像头
    String mFeedbackRoomId;
    long mFeedbackUserId;
    String mFeedbackUserName;
    private String mAppUuid;

    MutableLiveData<Constants.VideoProfileType> mLocalVideoProfile = new MutableLiveData<>();


    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAppUuid = prefs.getString(RoomActivity.KEY_APP_UUID, "");
        if (mAppUuid.isEmpty()) {
            UUID uuid = UUID.randomUUID();
            mAppUuid = uuid.toString().replace("-", "");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(RoomActivity.KEY_APP_UUID, mAppUuid);
            editor.apply();
        }
        /*
        int lang = Integer.parseInt(prefs.getString(KEY_LANGUAGE, "0"));
        if (lang != 0) {
            updateLanguage(lang);
        }*/
        createPanoEngine();
    }

    private void createPanoEngine() {
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
            RtcWhiteboard whiteboard = mRtcEngine.getWhiteboard();
            whiteboard.setCallback(mWhiteboardCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RtcEngine getPanoEngine() { return mRtcEngine; }

    public void refreshPanoEngine() {
        RtcEngine.destroy();
        createPanoEngine();
    }

    public PanoEngineCallback getPanoCallback() { return mRtcCallback; }
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
        Constants.VideoProfileType prof = Constants.VideoProfileType.Standard;
        if (profile == 2) {
            prof = Constants.VideoProfileType.HD720P;
        }
        mLocalVideoProfile.postValue(prof);
    }
    public LiveData<Constants.VideoProfileType> getLocalVideoProfile() {
        return mLocalVideoProfile;
    }

    public void registerEventHandler(PanoEventHandler handler) { mRtcCallback.addHandler(handler); }
    public void removeEventHandler(PanoEventHandler handler) { mRtcCallback.removeHandler(handler); }
    public void registerWhiteboardHandler(PanoWhiteboardHandler handler) { mWhiteboardCallback.addHandler(handler); }
    public void removeWhiteboardHandler(PanoWhiteboardHandler handler) { mWhiteboardCallback.removeHandler(handler); }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RtcEngine.destroy();
    }
}
