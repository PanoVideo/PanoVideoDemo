package video.pano.panocall.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.pano.rtc.api.Constants;

import video.pano.panocall.BuildConfig;
import video.pano.panocall.fragment.FaceBeautyFragment;
import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.fragment.WebPageFragment;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.Utils;

import static video.pano.panocall.info.Config.MAX_AUDIO_DUMP_SIZE;
import static video.pano.panocall.info.Constant.KEY_DEVICE_RATING;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    private static boolean sIsDeviceRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        sIsDeviceRating = intent.getBooleanExtra(KEY_DEVICE_RATING, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.settings);
        if (fragment != null && fragment.isVisible()) {
            if (fragment instanceof FaceBeautyFragment) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        super.onSupportNavigateUp();
        onBackPressed();
        return true;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        if (fragment instanceof WebPageFragment) {
            WebPageFragment webFragment = (WebPageFragment)fragment;
            if (pref.getKey().equals("key_help")) {
                webFragment.setWebLink("https://developer.pano.video/getting-started/intro/");
            } else if (pref.getKey().equals("key_about_us")) {
                webFragment.setWebLink("https://www.pano.video/about.html");
            }
        }
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);

            PanoApplication app = (PanoApplication) Utils.getApp();

            Preference verPref = findPreference("key_pref_version");
            if (verPref != null) {
                String app_ver = BuildConfig.VERSION_NAME;
                String sdk_ver = app.getPanoEngine().getSdkVersion();
                String panoVersion = app_ver + " (" + sdk_ver + ")";
                verPref.setSummary(panoVersion);
            }

            SwitchPreference debugPref = findPreference("key_enable_debug_mode");
            if (debugPref != null) {
                debugPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if ((Boolean)newValue) {
                        String msg = getResources().getString(R.string.msg_open_debug_mode);
                        new AlertDialog.Builder(getContext())
                                .setMessage(msg)
                                .setPositiveButton(R.string.title_button_ok, (dialog, which) -> {
                                    app.getPanoEngine().startAudioDump(MAX_AUDIO_DUMP_SIZE);
                                })
                                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> {
                                    debugPref.setChecked(false);
                                })
                                .create()
                                .show();
                    }else{
                        app.getPanoEngine().stopAudioDump();
                    }
                    return true;
                });
            }

            ListPreference videoPref = findPreference("key_video_sending_resolution");
            if (videoPref != null) {
                videoPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String profileStr = (String) newValue;
                    int resolution = Integer.parseInt(profileStr);
                    app.updateVideoProfile(Integer.parseInt(profileStr));
                    int maxProfile = DeviceRatingTest.getIns().updateProfileByDeviceRating(app.getPanoEngine().queryDeviceRating());
                    if(sIsDeviceRating && resolution > maxProfile){
                        DeviceRatingTest.getIns().showRatingToast(maxProfile);
                    }
                    return true;
                });
            }

            ListPreference langPref = findPreference("key_language");
            if (langPref != null) {
                langPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String langStr = (String) newValue;
                    app.updateLanguage(Integer.parseInt(langStr));
                    return true;
                });
            }

            ListPreference screenCaptureFpsPref = findPreference("key_screen_capture_fps");
            if (screenCaptureFpsPref != null) {
                screenCaptureFpsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    app.getPanoEngine().setOption(Constants.PanoOptionType.ScreenOptimization,
                            !"0".equals(newValue));
                    return true;
                });
            }
        }
    }

    public static void launch(Context context, boolean deviceRating) {
        Intent intent = new Intent();
        intent.putExtra(KEY_DEVICE_RATING, deviceRating);
        intent.setClass(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}
