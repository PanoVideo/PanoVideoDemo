package video.pano.panocall.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.WindowManager;

import video.pano.panocall.info.Config;

/**
 * <pre>
 *     desc  : utils about initialization
 * </pre>
 */
public final class Utils {

    private static long lastClickTime = 0;

    @SuppressLint("StaticFieldLeak")
    private static Application sApp;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Init utils.
     * <p>Init it in the class of UtilsFileProvider.</p>
     *
     * @param app application
     */
    public static void init(final Application app) {
        if (app == null) {
            Log.e("Utils", "app is null.");
            return;
        }
        if (sApp == null) {
            sApp = app;
            return;
        }
        if (sApp.equals(app)) return;
        sApp = app;
    }

    public static void initConfig(){
        if(sApp == null) return ;
        Config.sScreenHeight = getScreenHeight();
        Config.sScreenWidth = getScreenWidth();
        Config.sStatusBarHeight = getStatusBarHeight();
    }

    /**
     * Return the Application object.
     * <p>Main process get app by UtilsFileProvider,
     * and other process get app by reflect.</p>
     *
     * @return the Application object
     */
    public static Application getApp() {
        if (sApp != null){
            return sApp;
        }else{
            throw new NullPointerException("reflect failed.");
        }
    }

    private static int getScreenWidth() {
        WindowManager windowManager = (WindowManager) sApp.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager != null){
            return getApp().getResources().getDisplayMetrics().widthPixels;
        }
        return -1 ;
    }

    private static int getScreenHeight() {
        WindowManager windowManager = (WindowManager) sApp.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager != null){
            return getApp().getResources().getDisplayMetrics().heightPixels;
        }
        return -1 ;
    }

    /**
     * 获取staus bar的高度
     *
     * @return
     */
    private static int getStatusBarHeight() {
        int result = 0;
        final Resources resources = sApp.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public synchronized static boolean doubleClick() {
        long time = System.currentTimeMillis();
        long value = time - lastClickTime;
        if (Math.abs(value) < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}
