package video.pano.panocall.utils;

import com.pano.rtc.api.PanoAnnotation;

public class AnnotationHelper {

    private PanoAnnotation mAnnotation;
    private boolean mAnnotationEnable;
    private boolean mIsAnnotationHost;

    private static class Holder {
        private static final AnnotationHelper INSTANCE = new AnnotationHelper();
    }

    public static AnnotationHelper getInstance() {
        return Holder.INSTANCE;
    }

    private AnnotationHelper() {

    }

    public void setAnnotation(PanoAnnotation annotation) {
        mAnnotation = annotation;
    }

    public PanoAnnotation getAnnotation() {
        return mAnnotation;
    }

    public void setAnnotationEnable(boolean enable) {
        mAnnotationEnable = enable;
    }

    public boolean annotationEnable() {
        return mAnnotationEnable;
    }

    public void setAnnotationHost(boolean host) {
        mIsAnnotationHost = host;
    }

    public boolean annotationHost() {
        return mIsAnnotationHost;
    }
}
