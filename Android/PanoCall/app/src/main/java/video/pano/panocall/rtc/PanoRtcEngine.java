package video.pano.panocall.rtc;

import static video.pano.panocall.info.Config.APPID;
import static video.pano.panocall.info.Config.PANO_SERVER;
import static video.pano.panocall.info.Constant.KEY_SCREEN_CAPTURE_FPS;
import static video.pano.panocall.info.Constant.KEY_VIDEO_FRAME_RATE;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.RtcEngineCallback;
import com.pano.rtc.api.RtcEngineConfig;
import com.pano.rtc.api.RtcMediaStatsObserver;
import com.pano.rtc.api.RtcMessageService;
import com.pano.rtc.api.RtcWhiteboard;

import video.pano.panocall.callback.PanoEngineCallback;
import video.pano.panocall.callback.PanoMediaStatsObserver;
import video.pano.panocall.callback.PanoMessageCallback;
import video.pano.panocall.callback.PanoWhiteboardCallback;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public class PanoRtcEngine {

    private RtcEngine mRtcEngine;
    private RtcWhiteboard mRtcWhiteboard ;
    private RtcMessageService mRtcMessageService;

    private final PanoEngineCallback mRtcCallback = new PanoEngineCallback();
    private final PanoWhiteboardCallback mWhiteboardCallback = new PanoWhiteboardCallback();
    private final PanoMessageCallback mMessageCallback = new PanoMessageCallback();
    private final PanoMediaStatsObserver mMediaStatsObserver = new PanoMediaStatsObserver();

    private final Constants.AudioAecType mAudioAecType = Constants.AudioAecType.Default;
    private final boolean mHwAcceleration = false;

    private static class Holder {
        private static final PanoRtcEngine INSTANCE = new PanoRtcEngine();
    }

    public static PanoRtcEngine getIns() {
        return PanoRtcEngine.Holder.INSTANCE;
    }

    private PanoRtcEngine() {}

    public RtcEngine createEngine() {
        if (mRtcEngine == null ) {
            synchronized (PanoRtcEngine.class) {
                if (mRtcEngine == null ) {
                    mRtcEngine = initEngine();
                }
            }
        }
        return mRtcEngine;
    }

    private RtcEngine initEngine() {
        // 设置PANO媒体引擎的配置参数
        String screenOptMode = SPUtils.getString(KEY_SCREEN_CAPTURE_FPS, "0");
        Constants.VideoFrameRateType frameRateType = DeviceRatingTest.getIns()
                .getVideoFrameRateType(SPUtils.getInt(KEY_VIDEO_FRAME_RATE, 1));
        try {
            mRtcEngine = RtcEngine.create(getConfig());
            mRtcEngine.setOption(Constants.PanoOptionType.ScreenOptimization,
                    !"0".equals(screenOptMode));
            mRtcEngine.setOption(Constants.PanoOptionType.DisableAV1Encoding,true);
            mRtcEngine.setVideoFrameRate(frameRateType);
            mRtcWhiteboard = mRtcEngine.getWhiteboard();
            mRtcMessageService = mRtcEngine.getMessageService();

            mRtcWhiteboard.setCallback(mWhiteboardCallback);
            mRtcWhiteboard.enableCursorPosSync(true);
            mRtcWhiteboard.enableShowRemoteCursor(true);
            mRtcEngine.setMediaStatsObserver(mMediaStatsObserver);
            mRtcMessageService.setCallback(mMessageCallback);
            return mRtcEngine ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }

    private RtcEngineConfig getConfig(){
        RtcEngineConfig engineConfig = new RtcEngineConfig();
        engineConfig.appId = APPID;
        engineConfig.server = PANO_SERVER;
        engineConfig.context = Utils.getApp();
        engineConfig.callback = mRtcCallback;
        engineConfig.audioAecType = mAudioAecType;
        engineConfig.videoCodecHwAcceleration = mHwAcceleration;
        return engineConfig ;
    }

    public RtcEngine getPanoEngine() { return mRtcEngine; }
    public RtcWhiteboard getPanoWhiteboard(){
        return mRtcWhiteboard ;
    }
    public RtcMessageService getPanoMessageService(){
        return mRtcMessageService ;
    }

    public void registerEventHandler(RtcEngineCallback handler) { mRtcCallback.addListener(handler); }
    public void removeEventHandler(RtcEngineCallback handler) { mRtcCallback.removeListener(handler); }

    public void registerWhiteboardHandler(RtcWhiteboard.Callback handler) { mWhiteboardCallback.addListener(handler); }
    public void removeWhiteboardHandler(RtcWhiteboard.Callback handler) { mWhiteboardCallback.removeListener(handler); }

    public void registerMessageCallback(RtcMessageService.Callback handler){ mMessageCallback.addListener(handler); }
    public void removeMessageCallback(RtcMessageService.Callback handler){ mMessageCallback.removeListener(handler); }

    public void registerRtcMediaStatsObserver(RtcMediaStatsObserver observer){ mMediaStatsObserver.addListener(observer); }
    public void removeRtcMediaStatsObserver(RtcMediaStatsObserver observer){ mMediaStatsObserver.removeListener(observer); }

    public void refresh() {
        clear();
        createEngine();
    }

    public void clear(){
        if(mRtcEngine != null){
            mRtcEngine.destroy();
            mRtcEngine = null ;
        }
    }
}
