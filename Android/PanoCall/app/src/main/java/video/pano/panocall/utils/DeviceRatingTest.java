package video.pano.panocall.utils;

import android.widget.Toast;

import com.pano.rtc.api.Constants;

import video.pano.panocall.R;

public class DeviceRatingTest {

    private static DeviceRatingTest ins;
    private DeviceRatingTest(){}

    public static DeviceRatingTest getIns() {

        if (ins == null) {
            synchronized (DeviceRatingTest.class) {
                ins = new DeviceRatingTest();
            }
        }
        return ins;
    }

    public int updateProfileByDeviceRating(Constants.DeviceRating deviceRating) {
        if (deviceRating == Constants.DeviceRating.VeryBad) {
            return 0;
        } else if (deviceRating == Constants.DeviceRating.Bad) {
            return 1;
        } else if (deviceRating == Constants.DeviceRating.Poor) {
            return 2;
        } else if (deviceRating == Constants.DeviceRating.Good) {
            return 3;
        } else if (deviceRating == Constants.DeviceRating.Excellent) {
            return 3;
        }
        return 2 ;
    }

    public void showRatingToast(int maxProfile){
        switch (maxProfile){
            case 0:
                Toast.makeText(Utils.getApp(),Utils.getApp().getString(R.string.msg_device_rating,"90p"),Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(Utils.getApp(),Utils.getApp().getString(R.string.msg_device_rating,"180p"),Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(Utils.getApp(),Utils.getApp().getString(R.string.msg_device_rating,"360p"),Toast.LENGTH_SHORT).show();
                break;
            case 3:
            case 4:
                Toast.makeText(Utils.getApp(),Utils.getApp().getString(R.string.msg_device_rating,"720p"),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    public Constants.VideoProfileType getProfileType(int resolution){
        switch (resolution){
            case 0:
                return Constants.VideoProfileType.Lowest ;
            case 1:
                return Constants.VideoProfileType.Low;
            case 3:
            case 4:
                return Constants.VideoProfileType.HD720P;
            case 2:
            default:
                return Constants.VideoProfileType.Standard;
        }
    }
}
