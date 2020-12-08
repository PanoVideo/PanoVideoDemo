package video.pano.panocall;

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

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.settings_activity);
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

            PanoApplication app = (PanoApplication)getActivity().getApplication();

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
                    if (app.mIsRoomJoined) {
                        if ((Boolean)newValue) {
                            app.getPanoEngine().startAudioDump(PanoApplication.kMaxAudioDumpSize);
                        } else {
                            app.getPanoEngine().stopAudioDump();
                        }
                    }
                    return true;
                });
            }

            ListPreference videoPref = findPreference("key_video_sending_resolution");
            if (videoPref != null) {
                videoPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String profileStr = (String) newValue;
                    app.updateVideoProfile(Integer.parseInt(profileStr));
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
        }
    }
}
