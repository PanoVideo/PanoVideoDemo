package video.pano.panocall.activity;

import static video.pano.panocall.info.Config.MAX_AUDIO_DUMP_SIZE;
import static video.pano.panocall.info.Constant.FACE_BEAUTY_FRAGMENT;
import static video.pano.panocall.info.Constant.FEED_BACK_FRAGMENT;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_SPEAKER;
import static video.pano.panocall.info.Constant.KEY_DEVICE_RATING;
import static video.pano.panocall.info.Constant.KEY_ENABLE_DEBUG_MODE;
import static video.pano.panocall.info.Constant.KEY_LEAVE_CONFIRM;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;
import static video.pano.panocall.info.Constant.KEY_VIDEO_RESOLUTION_ID;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.SOUND_FEED_BACK_FRAGMENT;
import static video.pano.panocall.info.Constant.WEB_PAGE_FRAGMENT;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import video.pano.panocall.BuildConfig;
import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public class SettingsActivity extends BaseSettingActivity{

    private static boolean sIsDeviceRating;

    private PanoApplication mPanoApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initData();
        initTitleView();
        initSettingViews();
    }

    private void initData() {
        Intent intent = getIntent();
        sIsDeviceRating = intent.getBooleanExtra(KEY_DEVICE_RATING, false);
        mPanoApp = (PanoApplication) Utils.getApp();
    }

    private void initTitleView() {
        TextView titleView = findViewById(R.id.tv_title);
        ImageView leftIcon = findViewById(R.id.iv_left_icon);

        titleView.setText(R.string.title_activity_settings);

        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_back);
        leftIcon.setOnClickListener(v ->
                finish()
        );
    }

    private void initSettingViews() {
        initUserName();
        initAutoStartSpeaker();
        initLeaveConfirm();
        initDebugMode();
        initStatistics();
        initFaceBeauty();
        initFeedback();
        initSoundFeedback();
        initVideoResolution();
        initVersion();
        initAboutUs();
    }

    private void initUserName(){
        TextView userNameTv = findViewById(R.id.tv_user_name_content);
        String userName = SPUtils.getString(KEY_USER_NAME, "");
        userNameTv.setText(userName);
    }

    @SuppressLint ("UseSwitchCompatOrMaterialCode")
    private void initAutoStartSpeaker(){
        SwitchCompat autoStartSpeakerSwitch = findViewById(R.id.switch_auto_start_speaker);
        autoStartSpeakerSwitch.setChecked(SPUtils.getBoolean(KEY_AUTO_START_SPEAKER,false));
        autoStartSpeakerSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                SPUtils.put(KEY_AUTO_START_SPEAKER,isChecked)
        );
    }

    @SuppressLint ("UseSwitchCompatOrMaterialCode")
    private void initLeaveConfirm(){
        SwitchCompat leaveConfirmSwitch = findViewById(R.id.switch_leave_confirm);
        leaveConfirmSwitch.setChecked(SPUtils.getBoolean(KEY_LEAVE_CONFIRM,false));
        leaveConfirmSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                SPUtils.put(KEY_LEAVE_CONFIRM,isChecked)
        );
    }

    @SuppressLint ("UseSwitchCompatOrMaterialCode")
    private void initDebugMode(){
        SwitchCompat debugModeSwitch = findViewById(R.id.switch_enable_debug_mode);
        debugModeSwitch.setChecked(SPUtils.getBoolean(KEY_ENABLE_DEBUG_MODE,false));
        debugModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (isChecked) {
                String msg = getResources().getString(R.string.msg_open_debug_mode);
                new AlertDialog.Builder(this)
                        .setMessage(msg)
                        .setPositiveButton(R.string.title_button_ok, (dialog, which) -> {
                            PanoRtcEngine.getIns().getPanoEngine().startAudioDump(MAX_AUDIO_DUMP_SIZE);
                            SPUtils.put(KEY_ENABLE_DEBUG_MODE,true);
                        })
                        .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> {
                            debugModeSwitch.setChecked(false);
                        })
                        .create()
                        .show();
            }else{
                PanoRtcEngine.getIns().getPanoEngine().stopAudioDump();
            }
        });
    }

    private void initStatistics() {
        findViewById(R.id.cl_statistics_container).setOnClickListener(v ->
                StatisticsActivity.launch(this)
        );
    }

    private void initFaceBeauty(){
        findViewById(R.id.cl_face_beauty_container).setOnClickListener(v ->
                ContainerActivity.launch(SettingsActivity.this,FACE_BEAUTY_FRAGMENT,
                        getString(R.string.title_face_beauty),"")
        );
    }

    private void initFeedback(){
        findViewById(R.id.cl_send_feedback_container).setOnClickListener( v ->
                ContainerActivity.launch(SettingsActivity.this,FEED_BACK_FRAGMENT,
                        getString(R.string.title_send_feedback),"")
        );
    }

    private void initSoundFeedback(){
        findViewById(R.id.cl_send_sound_feedback_container).setOnClickListener( v->
                ContainerActivity.launch(SettingsActivity.this,SOUND_FEED_BACK_FRAGMENT,
                        getString(R.string.title_send_sound_feedback),"")
        );
    }

    private void initAboutUs(){
        findViewById(R.id.cl_about_us_container).setOnClickListener( v->
                ContainerActivity.launch(SettingsActivity.this,WEB_PAGE_FRAGMENT,
                        getString(R.string.title_about_us),"https://www.pano.video/about.html")
        );
    }

    private void initVideoResolution(){
        RadioGroup videoResolutionGroup = findViewById(R.id.rg_video_resolution);
        int resolutionId = SPUtils.getInt(KEY_VIDEO_RESOLUTION_ID, R.id.rb_video_360p);
        videoResolutionGroup.check(resolutionId);

        videoResolutionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int resolution = 1;
            if(checkedId == R.id.rb_video_180p){
                resolution = 1 ;
            }else if(checkedId == R.id.rb_video_360p){
                resolution = 2 ;
            }else if(checkedId == R.id.rb_video_720p){
                resolution = 3 ;
            }

            mPanoApp.updateVideoProfile(resolution);
            int maxProfile = DeviceRatingTest.getIns()
                    .updateProfileByDeviceRating(PanoRtcEngine.getIns().getPanoEngine().queryDeviceRating());
            if(sIsDeviceRating && resolution > maxProfile){
                DeviceRatingTest.getIns().showRatingToast(maxProfile);
            }
            SPUtils.put(KEY_VIDEO_RESOLUTION_ID,checkedId);
            SPUtils.put(KEY_VIDEO_SENDING_RESOLUTION,resolution);
        });
    }

    private void initVersion(){
        TextView versionTv = findViewById(R.id.tv_version_content);
        String app_ver = BuildConfig.VERSION_NAME;
        String sdk_ver = PanoRtcEngine.getIns().getPanoEngine().getSdkVersion();
        String panoVersion = app_ver + " (" + sdk_ver + ")";
        versionTv.setText(panoVersion);
    }

    public static void launch(Context context, boolean deviceRating) {
        Intent intent = new Intent();
        intent.putExtra(KEY_DEVICE_RATING, deviceRating);
        intent.setClass(context, SettingsActivity.class);
        context.startActivity(intent);
    }

}
