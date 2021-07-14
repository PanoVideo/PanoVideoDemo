package video.pano.panocall.listener;

public interface PanoAnnotationHandler {
    void onAnnotationStart();
    void onAnnotationStop();
    void onClickPencil(boolean checked);
    void onClickArrow(boolean checked);
    void onClickEraser(boolean checked);
    void onProgressChange(int progress);
    void onCheckedColor(int index, int colorId);
}
