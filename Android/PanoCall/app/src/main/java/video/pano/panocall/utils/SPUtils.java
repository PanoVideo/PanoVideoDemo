package video.pano.panocall.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {

    public static final String DEFAULT_FILE_NAME = "default_pref";

    public static void put(String key, Object value) {
        put(DEFAULT_FILE_NAME, key, value);
    }

    public static void put(String fileName, String key, Object value) {
        SharedPreferences.Editor editor = Utils.getApp().getSharedPreferences(fileName, Context.MODE_PRIVATE).edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            editor.putString(key, value.toString());
        }
        editor.apply();
    }

    public static Object get(String key, Object defaultValue) {
        return get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return (boolean) get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return (int) get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        return (String) get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return (long) get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return (float) get(DEFAULT_FILE_NAME, key, defaultValue);
    }

    public static Object get(String fileName, String key, Object defaultValue) {
        SharedPreferences sp = Utils.getApp().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if (defaultValue instanceof String) {
            return sp.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sp.getLong(key, (Long) defaultValue);
        }
        return null;
    }
}
