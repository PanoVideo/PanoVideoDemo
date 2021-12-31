package video.pano.panocall.rtc;

import android.content.Context;

public class MeetingViewFactory {

    public static final int TYPE_LARGE_VIEW = 1;
    public static final int TYPE_MIDDLE_VIEW = 2;
    public static final int TYPE_SMALL_VIEW = 3;

    public static final int MODE_LOCAL_VIDEO = 10;
    public static final int MODE_REMOTE_VIDEO = 11;
    public static final int MODE_REMOTE_SCREEN = 12;

    public static AbsInfoViewComponent createInfoView(Context context, int type) {
        switch (type) {
            case TYPE_LARGE_VIEW:
                return new LargeInfoView(context);
            case TYPE_MIDDLE_VIEW:
                return new MiddleInfoView(context);
            case TYPE_SMALL_VIEW:
            default:
                return new SmallInfoView(context);
        }
    }

    public static AbsOperateComponent createOperate(int mode) {
        switch (mode) {
            case MODE_REMOTE_VIDEO:
                return new RemoteVideoOperate();
            case MODE_REMOTE_SCREEN:
                return new RemoteScreenOperate();
            case MODE_LOCAL_VIDEO:
            default:
                return new LocalVideoOperate();
        }
    }

    public static MeetingViewInfo createMeetingViewInfo(Context context, int type){
        AbsInfoViewComponent infoComponent = createInfoView(context, type);
        MeetingViewInfo meetingViewInfo = new MeetingViewInfo();
        meetingViewInfo.addInfoComponent(infoComponent);
        return meetingViewInfo;
    }

}
