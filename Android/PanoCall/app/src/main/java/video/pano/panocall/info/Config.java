package video.pano.panocall.info;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.util.concurrent.atomic.AtomicInteger;

public class Config {

    public static String APPID = Input Your AppId;
    public static String USER_ID ="";
    public static String APP_TOKEN = Input Your Token;
    public static String PANO_SERVER = "api.pano.video";

    public static long MAX_AUDIO_DUMP_SIZE = 200*1024*1024; // 200 MB

    public static int sScreenWidth;
    public static int sScreenHeight;
    public static int sStatusBarHeight ;

    public static boolean sIsLocalVideoStarted = false; // 此变量用于通知美颜页本地视频是否开启
    public static boolean sIsFrontCamera = true; // 此变量用于通知美颜页当前是否是前置摄像头

    public static final AtomicInteger sNextGeneratedId = new AtomicInteger(2000);

    public static final int DELAY_TIME = 1000;

    public static final String[] RTC_PERMISSIONS = {
            RECORD_AUDIO,
            CAMERA,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE,
    };

    public static final String[] FACE_BEAUTY_PERMISSION = {
            CAMERA
    };


}
