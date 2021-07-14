package video.pano.panocall.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.Constants.DeviceRating;
import com.pano.rtc.api.PanoAnnotation;
import com.pano.rtc.api.PanoAnnotationManager;
import com.pano.rtc.api.RtcAudioIndication;
import com.pano.rtc.api.RtcChannelConfig;
import com.pano.rtc.api.RtcEngineCallback;
import com.pano.rtc.api.RtcWhiteboard;
import com.pano.rtc.api.model.RtcAudioLevel;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.fragment.AnnotationControlPanelFragment;
import video.pano.panocall.fragment.BottomControlPanelFragment;
import video.pano.panocall.fragment.CallFragment;
import video.pano.panocall.fragment.FloatFragment;
import video.pano.panocall.fragment.GridFragment;
import video.pano.panocall.fragment.TopControlPanelFragment;
import video.pano.panocall.fragment.WhiteboardFragment;
import video.pano.panocall.listener.AnnotationListener;
import video.pano.panocall.listener.OnBottomControlPanelListener;
import video.pano.panocall.listener.OnControlEventListener;
import video.pano.panocall.listener.OnTopControlPanelListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.service.ScreenCaptureService;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.MiscUtils;
import video.pano.panocall.utils.Utils;
import video.pano.panocall.viewmodel.CallViewModel;
import video.pano.rtc.base.util.NetworkChangeObserver;
import video.pano.rtc.base.util.NetworkChangeReceiver;
import video.pano.rtc.base.util.NetworkUtils;

import static video.pano.panocall.info.Constant.KEY_AUTO_MUTE_AUDIO;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_CAMERA;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_SPEAKER;
import static video.pano.panocall.info.Constant.KEY_DEVICE_RATING;
import static video.pano.panocall.info.Constant.KEY_ENABLE_DEBUG_MODE;
import static video.pano.panocall.info.Constant.KEY_ENABLE_FACE_BEAUTY;
import static video.pano.panocall.info.Constant.KEY_FACE_BEAUTY_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_LEAVE_CONFIRM;
import static video.pano.panocall.info.Constant.KEY_PANO_TOKEN;
import static video.pano.panocall.info.Constant.KEY_ROOM_ID;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.MSG_KEY;


public class CallActivity extends AppCompatActivity implements OnControlEventListener,
        PanoAnnotationManager.Callback,
        RtcWhiteboard.Callback,
        RtcEngineCallback,
        OnTopControlPanelListener,
        AnnotationListener,
        OnBottomControlPanelListener,
        NetworkChangeObserver{
    public static final String TAG = "PVC";

    private CallViewModel mViewModel;
    private int mCurrentFragmentId = R.id.FloatFragment;

    TopControlPanelFragment mTopControlPanel;
    BottomControlPanelFragment mBottomControlPanel;
    AnnotationControlPanelFragment mAnnotationControlPanelFragment;
    boolean mIsControlPanelShowed = false;

    private Timer mPanelTimer;
    private ConstraintLayout mClDots;
    private boolean mEnableDeviceRating = true;
    private DeviceRating mDeviceRating;
    private AlertDialog mMoreDialog;
    private AlertDialog mShareDialog;
    private Intent mScreenCaptureService;
    private boolean mScreenCaptureOn = false;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try{
            this.getSupportActionBar().hide();
        }catch (NullPointerException e){}

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        MiscUtils.setScreenOnFlag(getWindow());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mEnableDeviceRating = prefs.getBoolean(KEY_DEVICE_RATING, true);

        mViewModel = new ViewModelProvider(this).get(CallViewModel.class);

        PanoApplication app = (PanoApplication)Utils.getApp();
        app.registerEventHandler(this);
        app.registerWhiteboardHandler(this);
        app.registerAnnotationCallback(this);

        mViewModel.setPanoRtcEngine(app.getPanoEngine());

        mClDots = findViewById(R.id.cl_dots);
        ImageView imgDot1 = findViewById(R.id.img_dot_1);
        ImageView imgDot2 = findViewById(R.id.img_dot_2);
        imgDot1.setSelected(true);

        String roomId = getIntent().getStringExtra(KEY_ROOM_ID);
        mTopControlPanel = TopControlPanelFragment.newInstance(roomId);
        mBottomControlPanel = BottomControlPanelFragment.newInstance();
        mAnnotationControlPanelFragment = AnnotationControlPanelFragment.newInstance();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            mCurrentFragmentId = destination.getId();
            hideControlPanel();
            if (mCurrentFragmentId == R.id.WhiteboardFragment) {
                mClDots.setVisibility(View.GONE);
            } else {
                imgDot1.setSelected(mCurrentFragmentId == R.id.FloatFragment);
                imgDot2.setSelected(mCurrentFragmentId == R.id.GridFragment);
                checkDotIndicatorState();
            }
        });
        mClDots.setVisibility(View.GONE);

        loadSettings();

        app.mIsFrontCamera = mViewModel.mIsFrontCamera;
        joinRoom();

        mScreenCaptureService = new Intent(this, ScreenCaptureService.class);

        setupNetworkChangeReceiver(this);

        showControlPanel();
    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean needConfirm = prefs.getBoolean(KEY_LEAVE_CONFIRM, true);
        if (needConfirm) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.msg_call_exit_alert);
            builder.setPositiveButton(R.string.title_button_ok, (dialog, id) -> onConfirmedBackPressed());
            builder.setNegativeButton(R.string.title_button_cancel, (dialog, id) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        onConfirmedBackPressed();
    }

    @Override
    protected void onDestroy() {
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.removeWhiteboardHandler(this);
        app.removeEventHandler(this);
        app.removeAnnotationCallback(this);
        super.onDestroy();
        mViewModel.reset();
        resetTimer();
        unregisterNetworkReceiver();
    }


    /**************  RtcEngineCallback  **************/
    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        Log.i(TAG, "onChannelJoinConfirm, result="+result);
        if (mEnableDeviceRating) {
            mDeviceRating = mViewModel.rtcEngine().queryDeviceRating();
            Log.i(TAG, "mDeviceRating = " + mDeviceRating);
            updateProfileByDeviceRating(mDeviceRating);
        }
        mViewModel.onChannelJoinConfirm(result);
    }

    @Override
    public void onChannelLeaveIndication(Constants.QResult result) {
        Log.i(TAG, "onChannelLeaveIndication, result="+result);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_call_left_alert);
        builder.setPositiveButton(R.string.title_button_ok, (dialog, id) -> onConfirmedBackPressed());
        AlertDialog dialog = builder.create();
        dialog.show();

        mViewModel.onChannelLeaveIndication(result);
    }
    @Override
    public void onChannelCountDown(long remain) {
        Log.i(TAG, "onChannelCountDown, remian="+remain);
        mViewModel.onChannelCountDown(remain);
    }
    @Override
    public void onUserJoinIndication(long userId, String userName) {
        mViewModel.onUserJoinIndication(userId, userName);
        checkDotIndicatorState();
    }
    @Override
    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        mViewModel.onUserLeaveIndication(userId, reason);
        checkDotIndicatorState();
    }
    @Override
    public void onUserAudioStart(long userId) {
        mViewModel.onUserAudioStart(userId);
    }
    @Override
    public void onUserAudioStop(long userId) {
        mViewModel.onUserAudioStop(userId);
    }
    @Override
    public void onUserAudioSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {
        mViewModel.onUserVideoStart(userId, maxProfile);
    }
    @Override
    public void onUserVideoStop(long userId) {
        mViewModel.onUserVideoStop(userId);
    }
    @Override
    public void onUserVideoSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserAudioMute(long userId) {
        mViewModel.onUserAudioMute(userId);
    }
    @Override
    public void onUserAudioUnmute(long userId) {
        mViewModel.onUserAudioUnmute(userId);
    }
    @Override
    public void onUserVideoMute(long userId) {
        mViewModel.onUserVideoMute(userId);
    }
    @Override
    public void onUserVideoUnmute(long userId) {
        mViewModel.onUserVideoUnmute(userId);
    }
    @Override
    public void onUserScreenStart(long userId) {
        mViewModel.onUserScreenStart(userId);
    }
    @Override
    public void onUserScreenStop(long userId) {
        mViewModel.onUserScreenStop(userId);
    }
    @Override
    public void onUserScreenSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserScreenMute(long userId) {
        mViewModel.onUserScreenMute(userId);
    }
    @Override
    public void onUserScreenUnmute(long userId) {
        mViewModel.onUserScreenUnmute(userId);
    }

    @Override
    public void onWhiteboardAvailable() {
        mViewModel.onWhiteboardAvailable();
    }

    @Override
    public void onWhiteboardUnavailable() {
        mViewModel.onWhiteboardUnavailable();
    }

    @Override
    public void onWhiteboardStart() {
        if (!mViewModel.mWhiteboardState.isAvailable()) {
            Toast.makeText(this, R.string.msg_whiteboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        onClickWhiteboard();
    }

    @Override
    public void onWhiteboardStop() {
        mViewModel.onWhiteboardStop();
    }

    @Override
    public void onFirstAudioDataReceived(long userId) {}

    @Override
    public void onFirstVideoDataReceived(long userId) {}

    @Override
    public void onFirstScreenDataReceived(long userId) {}

    @Override
    public void onAudioDeviceStateChanged(String deviceId,
                                          Constants.AudioDeviceType deviceType,
                                          Constants.AudioDeviceState deviceState) {

    }

    @Override
    public void onVideoDeviceStateChanged(String deviceId,
                                          Constants.VideoDeviceType deviceType,
                                          Constants.VideoDeviceState deviceState) {

    }

    @Override
    public void onChannelFailover(Constants.FailoverState state) {

    }

    @Override
    public void onScreenStartResult(Constants.QResult result) {
        mScreenCaptureOn = result == Constants.QResult.OK;
    }

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        mViewModel.onNetworkQuality(userId, quality);
    }

    /**************  RtcEngineCallback  **************/

    /**************  RtcWhiteboard  **************/
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        mViewModel.onPageNumberChanged(curPage, totalPages);
    }

    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {
    }

    @Override
    public void onViewScaleChanged(float scale) {
        mViewModel.onViewScaleChanged(scale);
    }

    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        mViewModel.onRoleTypeChanged(newRole);
    }

    @Override
    public void onContentUpdated() {
    }

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {
        mViewModel.onCreateDoc(result, fileId);
    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {
        mViewModel.onSwitchDoc(result, fileId);
    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {
        mViewModel.onDeleteDoc(result, fileId);
    }
    @Override
    public void onMessage(long userId, byte[] msg) {
        if(msg == null || msg.length <= 0 ){
            return ;
        }
        String msgData = new String(msg);
        if(TextUtils.isEmpty(msgData)) return ;
        try{
            JSONObject jsonObject = new JSONObject(msgData);
            if(jsonObject.has(MSG_KEY)){
                long hostId = jsonObject.getLong(MSG_KEY);
                mViewModel.getUserManager().setHostUserId(hostId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mViewModel.onMessage(userId, msg);
    }

    @Override
    public void onUserJoined(long userId, String userName) {
        mViewModel.onUserJoined(userId, userName);
    }

    @Override
    public void onUserLeft(long userId) {
        mViewModel.onUserLeft(userId);
    }

    @Override
    public void onVisionShareStopped(long userId) {
        UserInfo userInfo = mViewModel.getUserManager().getRemoteUser(userId);
        if(userInfo != null && !TextUtils.isEmpty(userInfo.getUserName())){
            String name = userInfo.getUserName();
            Toast.makeText(this, getString(R.string.whiteboard_stopped_vision_share, name), Toast.LENGTH_SHORT).show();
        }
        ((PanoApplication)Utils.getApp()).getPanoWhiteboard().stopFollowVision();
    }

    @Override
    public void onVisionShareStarted(long userId) {
        UserInfo userInfo = mViewModel.getUserManager().getRemoteUser(userId);
        if(userInfo != null && !TextUtils.isEmpty(userInfo.getUserName())){
            String name = userInfo.getUserName();
            Toast.makeText(this, getString(R.string.whiteboard_started_vision_share, name), Toast.LENGTH_SHORT).show();
        }
        ((PanoApplication)Utils.getApp()).getPanoWhiteboard().startFollowVision();
    }

    /**************  RtcWhiteboard  **************/

    /**************  PanoAnnotationManager.Callback  **************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamId) {
        mViewModel.onVideoAnnotationStart(userId, streamId);
    }

    @Override
    public void onVideoAnnotationStop(long userId, int streamId) {
        mViewModel.onVideoAnnotationStop(userId, streamId);
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        mViewModel.onShareAnnotationStart(userId);
    }

    @Override
    public void onShareAnnotationStop(long userId) {
        mViewModel.onShareAnnotationStop(userId);
    }

    /**************  PanoAnnotationManager.Callback  **************/

    /**************  OnControlEventListener  **************/
    @Override
    public void onShowHideControlPanel() {
        if (!AnnotationHelper.getInstance().annotationEnable()) {
            if (mIsControlPanelShowed) {
                hideControlPanel();
            } else {
                showControlPanel();
            }
        }
    }
    /**************  OnControlEventListener  **************/


    /**************  OnTopControlPanelListener  **************/
    @Override
    public void onTCPanelAudio(boolean isSpeaker) {
        mViewModel.rtcEngine().setLoudspeakerStatus(isSpeaker);
    }
    @Override
    public void onTCPanelSwitchCamera() {
        mViewModel.rtcEngine().switchCamera();
        mViewModel.mIsFrontCamera = !mViewModel.mIsFrontCamera;
        PanoApplication app = (PanoApplication) Utils.getApp();
        app.mIsFrontCamera = mViewModel.mIsFrontCamera;
        CallFragment call = getCurrentCallFragment();
        if (call != null) {
            call.onSwitchCamera();
        }
    }
    @Override
    public void onTCPanelExit() {
        onBackPressed();
    }
    /**************  OnTopControlPanelListener  **************/


    /**************  OnBottomControlPanelListener  **************/
    @Override
    public void onBCPanelAudio(boolean muted) {
        CallFragment call = getCurrentCallFragment();
        if (call != null) {
            call.onLocalAudio(muted);
        }
    }
    @Override
    public void onBCPanelVideo(boolean closed) {
        CallFragment call = getCurrentCallFragment();
        if (call != null) {
            call.onLocalVideo(closed);
        }
    }
    @Override
    public void onBCPanelShare() {
        if (!mViewModel.mWhiteboardState.isAvailable()) {
            Toast.makeText(this, R.string.msg_whiteboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        int itemArray = mScreenCaptureOn ? R.array.share_settings_screen_share_on
                : R.array.share_settings;
        mShareDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_call_share)
                .setItems(itemArray, (dialog, which) -> {
                    if (which == 0) {
                        onClickWhiteboard();
                    } else if (which == 1) {
                       onClickScreenShare();
                    }
                })
                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
                .create();
        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }
    @Override
    public void onBCPanelMore() {
        mMoreDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.title_call_more)
            .setItems(R.array.more_settings, (dialog, which) -> {
                if (which == 0) {
                    onClickSettings();
                } else if (which == 1) {
                    onClickAnnotation();
                }
            })
            .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
            .create();
        if (!mMoreDialog.isShowing()) {
            mMoreDialog.show();
        }
    }
    /**************  OnBottomControlPanelListener  **************/

    /**************  OnAnnotationControlPanelListener  **************/

    @Override
    public void onAnnotationStart() {
        AnnotationHelper.getInstance().setAnnotationEnable(true);
        hideControlPanel();
        showAnnotationControlPanel();
    }

    @Override
    public void onAnnotationClose() {
        hideAnnotationControlPanel();
    }

    /**************  OnAnnotationControlPanelListener  **************/


    /**************  NetworkChangeObserver  **************/
    @Override
    public void onNetworkChanged(NetworkUtils.NetworkType type) {
        switch (type){
            case MOBILE:
                Toast.makeText(this,R.string.msg_cellular_data,Toast.LENGTH_SHORT).show();
                break;
            case NOT_CONNECTED:
                Toast.makeText(this,R.string.msg_no_data,Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
    /**************  NetworkChangeObserver  **************/


    // 加入房间
    private boolean joinRoom() {
        Intent intent = getIntent();
        long userId = intent.getLongExtra(KEY_USER_ID, 0);
        String roomId = intent.getStringExtra(KEY_ROOM_ID);
        String token = intent.getStringExtra(KEY_PANO_TOKEN);
        String userName = intent.getStringExtra(KEY_USER_NAME);

        // save room info
        PanoApplication app = (PanoApplication) Utils.getApp();
        app.mFeedbackRoomId = roomId;
        app.mFeedbackUserId = userId;
        app.mFeedbackUserName = userName;
        app.mIsLocalVideoStarted = false;

        UserInfo localUser = new UserInfo(userId, userName);
        mViewModel.getUserManager().setLocalUser(localUser);

        // reset audio speaker state
        mViewModel.rtcEngine().setLoudspeakerStatus(mViewModel.mIsAudioSpeakerOpened);

        // face beauty settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fbEnabled = prefs.getBoolean(KEY_ENABLE_FACE_BEAUTY, false);
        mViewModel.rtcEngine().setFaceBeautify(fbEnabled);
        if (fbEnabled) {
            float fbIntensity = prefs.getFloat(KEY_FACE_BEAUTY_INTENSITY, 0);
            mViewModel.rtcEngine().setFaceBeautifyIntensity(fbIntensity);
        }

        RtcChannelConfig config = new RtcChannelConfig();
        config.userName = userName;
        config.mode_1v1 = false;
        // 设置自动订阅所有音频
        config.subscribeAudioAll = true;
        Constants.QResult ret = mViewModel.rtcEngine().joinChannel(token, roomId, userId, config);
        if (ret != Constants.QResult.OK) {
            Toast.makeText(CallActivity.this, "joinChannel failed, result=" + ret, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // 离开房间
    void leaveRoom() {
        mViewModel.rtcEngine().stopVideo();
        mViewModel.rtcEngine().stopPreview();
        mViewModel.rtcEngine().stopAudio();
        mViewModel.rtcEngine().leaveChannel();
        PanoApplication app = (PanoApplication)Utils.getApp();
        app.mIsLocalVideoStarted = false;
    }

    private void onConfirmedBackPressed() {
        mViewModel.onAnnotationStop();
        leaveRoom();
        int count = getSupportFragmentManager().getBackStackEntryCount();
        for (int i=0; i<count; i++) {
            getSupportFragmentManager().popBackStack();
        }
        super.onBackPressed();
        Intent gotoMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        gotoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(gotoMainActivity);
    }

    void showControlPanel() {
        if (!mIsControlPanelShowed) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (!mTopControlPanel.isAdded()) {
                ft.replace(R.id.fl_top_control_panel, mTopControlPanel)
                        .addToBackStack(null);
            }
            if (!mBottomControlPanel.isAdded()) {
                ft.replace(R.id.fl_bottom_control_panel, mBottomControlPanel)
                        .addToBackStack(null);
            }
            try {
                ft.show(mTopControlPanel).show(mBottomControlPanel)
                        .commitAllowingStateLoss();
            } catch (IllegalStateException e) {
                return;
            }
            mIsControlPanelShowed = true;

            mPanelTimer = new Timer();
            mPanelTimer.schedule(new PanelTask(), 6000);
        }
    }

    void hideControlPanel() {
        if (mIsControlPanelShowed) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mTopControlPanel).remove(mBottomControlPanel).commitAllowingStateLoss();
            mIsControlPanelShowed = false;
            resetTimer();
        }
    }

    CallFragment getCurrentCallFragment() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return null;
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        FragmentManager navHostManager = Objects.requireNonNull(navHostFragment).getChildFragmentManager();
        for (Fragment fragment : navHostManager.getFragments()) {
            if ((fragment instanceof FloatFragment && Objects.equals(current.getLabel(), "Float Fragment")) ||
                    (fragment instanceof GridFragment && Objects.equals(current.getLabel(), "Grid Fragment")) ||
                    (fragment instanceof WhiteboardFragment && Objects.equals(current.getLabel(), "Whiteboard Fragment"))) {
                return (CallFragment) fragment;
            }
        }

        return null;
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mViewModel.mIsAudioMuted = mViewModel.mAutoMuteAudio = prefs.getBoolean(KEY_AUTO_MUTE_AUDIO, false);
        mViewModel.mIsAudioSpeakerOpened = prefs.getBoolean(KEY_AUTO_START_SPEAKER, true);
        mViewModel.mAutoStartCamera = prefs.getBoolean(KEY_AUTO_START_CAMERA, true);
        mViewModel.mEnableDebugMode = prefs.getBoolean(KEY_ENABLE_DEBUG_MODE, false);
        int resolution = Integer.parseInt(Objects.requireNonNull(prefs.getString(KEY_VIDEO_SENDING_RESOLUTION, "1")));
        mViewModel.mLocalProfile = DeviceRatingTest.getIns().getProfileType(resolution);
    }

    private void resetTimer() {
        if (mPanelTimer != null) {
            mPanelTimer.cancel();
            mPanelTimer.purge();
        }
    }

    private void checkDotIndicatorState() {
        int userCount = mViewModel.getUserManager().getRemoteUsers().size() + 1;
        if (userCount <= 2) {
            mClDots.setVisibility(View.GONE);
        } else {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination current = navController.getCurrentDestination();
            if (current != null) {
                if (current.getId() == R.id.WhiteboardFragment) {
                    mClDots.setVisibility(View.GONE);
                    return;
                }
            }
            mClDots.setVisibility(View.VISIBLE);
        }
    }

    private void updateProfileByDeviceRating(DeviceRating deviceRating) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int resolution = Integer.parseInt(Objects.requireNonNull(prefs.getString(KEY_VIDEO_SENDING_RESOLUTION, "1")));

        int maxProfile = DeviceRatingTest.getIns().updateProfileByDeviceRating(deviceRating);

        if(resolution > maxProfile){
            resolution = maxProfile ;
            DeviceRatingTest.getIns().showRatingToast(resolution);
        }
        mViewModel.mLocalProfile = DeviceRatingTest.getIns().getProfileType(resolution);

        prefs.edit().putString(KEY_VIDEO_SENDING_RESOLUTION, String.valueOf(resolution)).apply();
    }

    private void hideAnnotationControlPanel() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(mAnnotationControlPanelFragment)
                .commitAllowingStateLoss();
        mIsControlPanelShowed = false;
    }

    private void showAnnotationControlPanel() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_bottom_control_panel, mAnnotationControlPanelFragment)
                .addToBackStack(null);
        try {
            ft.show(mAnnotationControlPanelFragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
        }
    }

    private void onClickWhiteboard(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return ;
        }
        if ("Float Fragment".contentEquals(current.getLabel())) {
            navController.navigate(R.id.action_FloatFragment_to_WhiteboardFragment);
        } else if ("Grid Fragment".contentEquals(current.getLabel())) {
            navController.navigate(R.id.action_GridFragment_to_WhiteboardFragment);
        }
    }

    private void onClickAnnotation() {
        if(mViewModel.mIsVideoClosed){
            Toast.makeText(this,R.string.msg_open_your_video,Toast.LENGTH_LONG).show();
            return ;
        }
        mViewModel.onAnnotationStart();
        AnnotationHelper.getInstance().setAnnotationEnable(true);
        hideControlPanel();
        showAnnotationControlPanel();
    }

    private void onClickSettings() {
        SettingsActivity.launch(CallActivity.this,true);
    }

    private void onClickScreenShare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mScreenCaptureOn) {
                mViewModel.rtcEngine().stopScreen();
                stopService(mScreenCaptureService);
                mScreenCaptureOn = false;
            } else {
                startForegroundService(mScreenCaptureService);
            }
        } else {
            if (mScreenCaptureOn) {
                mViewModel.rtcEngine().stopScreen();
                mScreenCaptureOn = false;
            } else {
                mViewModel.rtcEngine().startScreen();
            }
        }
    }

    private void setupNetworkChangeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.setPriority(100);

        mNetworkChangeReceiver = new NetworkChangeReceiver(context);
        mNetworkChangeReceiver.setNetworkChangeObserver(this);
        context.registerReceiver(mNetworkChangeReceiver, intentFilter);
    }

    private void unregisterNetworkReceiver(){
        if(mNetworkChangeReceiver != null){
            unregisterReceiver(mNetworkChangeReceiver);
        }
    }

    public static void launch(Context context, String token, String roomId, long userId, String userName) {
        Intent intent = new Intent();
        intent.putExtra(KEY_PANO_TOKEN, token);
        intent.putExtra(KEY_ROOM_ID, roomId);
        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra(KEY_USER_NAME, userName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(context, CallActivity.class);
        context.startActivity(intent);
    }


    class PanelTask extends TimerTask {
        @Override
        public void run() {
            CallActivity.this.runOnUiThread(() -> {
                resetTimer();
                hideControlPanel();
            });
        }
    };

}
