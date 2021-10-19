package video.pano.panocall.callback;

import android.util.Log;

import com.pano.rtc.api.PanoAnnotation;
import com.pano.rtc.api.PanoAnnotationManager;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.rtc.PanoRtcEngine;

public class PanoAnnotationCallback implements PanoAnnotationManager.Callback {

    private static final String TAG = "PanoAnnotationCallback";
    private List<PanoAnnotationManager.Callback> mListeners = new ArrayList<>();

    public void addListener(PanoAnnotationManager.Callback listener) {
        mListeners.add(listener);
    }

    public void removeListener(PanoAnnotationManager.Callback listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStart");
        PanoAnnotation annotation = PanoRtcEngine.getIns().getPanoEngine().getAnnotationMgr().getVideoAnnotation(userId, streamId);
        AnnotationHelper.getIns().setAnnotation(annotation);

        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        for (PanoAnnotationManager.Callback callback : mListeners) {
            if (callback != null) {
                callback.onVideoAnnotationStart(userId, streamId);
            }
        }
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
        Log.i(TAG, "onVideoAnnotationStop");
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        for (PanoAnnotationManager.Callback callback : mListeners) {
            if (callback != null) {
                callback.onVideoAnnotationStop(userId, streamId);
            }
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        Log.i(TAG, "onShareAnnotationStart");
        PanoAnnotation annotation = PanoRtcEngine.getIns().getPanoEngine().getAnnotationMgr().getShareAnnotation(userId);
        AnnotationHelper.getIns().setAnnotation(annotation);

        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        for (PanoAnnotationManager.Callback callback : mListeners) {
            if (callback != null) {
                callback.onShareAnnotationStart(userId);
            }
        }
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        Log.i(TAG, "onShareAnnotationStop");
        if(mListeners == null || mListeners.isEmpty()){
            return ;
        }
        for (PanoAnnotationManager.Callback callback : mListeners) {
            if (callback != null) {
                callback.onShareAnnotationStop(userId);
            }
        }
    }
}
