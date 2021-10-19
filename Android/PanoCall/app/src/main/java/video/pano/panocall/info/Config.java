package video.pano.panocall.info;

import java.util.concurrent.atomic.AtomicInteger;

public class Config {

    public static String APPID = Input Your AppId;
    public static String USER_ID ="";
    public static String APP_TOKEN = Input Your Token;
    public static String PANO_SERVER = "api.pano.video";

    public static long MAX_AUDIO_DUMP_SIZE = 200*1024*1024; // 200 MB

    //url
    public static int sScreenWidth;
    public static int sScreenHeight;
    public static int sStatusBarHeight ;

    public static final AtomicInteger sNextGeneratedId = new AtomicInteger(2000);

}
