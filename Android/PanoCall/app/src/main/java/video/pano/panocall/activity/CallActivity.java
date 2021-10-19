package video.pano.panocall.activity;

import static video.pano.panocall.info.Constant.KEY_AUTO_MUTE_AUDIO;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_CAMERA;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_SPEAKER;
import static video.pano.panocall.info.Constant.KEY_DEVICE_RATING;
import static video.pano.panocall.info.Constant.KEY_ENABLE_DEBUG_MODE;
import static video.pano.panocall.info.Constant.KEY_ENABLE_FACE_BEAUTY;
import static video.pano.panocall.info.Constant.KEY_FACE_BEAUTY_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_GRID_POS;
import static video.pano.panocall.info.Constant.KEY_LEAVE_CONFIRM;
import static video.pano.panocall.info.Constant.KEY_PANO_TOKEN;
import static video.pano.panocall.info.Constant.KEY_ROOM_ID;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_COMMAND;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_DESCRIPTION;
import static video.pano.panocall.info.Constant.KEY_SOUND_FEEDBACK_TYPE;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.MSG_KEY;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP;
import static video.pano.panocall.info.Constant.VALUE_SOUND_FEEDBACK_TYPE_COMMAND;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.pano.rtc.api.PanoAnnotationManager;
import com.pano.rtc.api.RtcChannelConfig;
import com.pano.rtc.api.RtcEngine;
import com.pano.rtc.api.RtcEngineCallback;
import com.pano.rtc.api.RtcMediaStatsObserver;
import com.pano.rtc.api.RtcMessageService;
import com.pano.rtc.api.RtcWhiteboard;
import com.pano.rtc.api.model.stats.RtcAudioRecvStats;
import com.pano.rtc.api.model.stats.RtcAudioSendStats;
import com.pano.rtc.api.model.stats.RtcSystemStats;
import com.pano.rtc.api.model.stats.RtcVideoBweStats;
import com.pano.rtc.api.model.stats.RtcVideoRecvStats;
import com.pano.rtc.api.model.stats.RtcVideoSendStats;

import org.json.JSONObject;

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
import video.pano.panocall.fragment.UserListFragment;
import video.pano.panocall.fragment.WhiteboardFragment;
import video.pano.panocall.info.Config;
import video.pano.panocall.listener.AnnotationListener;
import video.pano.panocall.listener.OnBottomControlPanelListener;
import video.pano.panocall.listener.OnControlEventListener;
import video.pano.panocall.listener.OnOrientationChangeListener;
import video.pano.panocall.listener.OnTopControlPanelListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.service.ScreenCaptureService;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.MiscUtils;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.ScreenSensorUtils;
import video.pano.panocall.utils.Utils;
import video.pano.panocall.viewmodel.CallViewModel;
import video.pano.rtc.base.util.NetworkChangeObserver;
import video.pano.rtc.base.util.NetworkChangeReceiver;
import video.pano.rtc.base.util.NetworkUtils;


public class CallActivity extends AppCompatActivity implements OnControlEventListener,
        PanoAnnotationManager.Callback,
        RtcWhiteboard.Callback,
        RtcEngineCallback,
        RtcMessageService.Callback,
        OnTopControlPanelListener,
        AnnotationListener,
        OnBottomControlPanelListener,
        NetworkChangeObserver,
        OnOrientationChangeListener,
        RtcMediaStatsObserver{
    public static final String TAG = "PVC";
    private static final long TIME_OUT_DELAY = 60 * 1000 ;
    private static final long DUMP_SIZE = 200 * 1024 * 1024;

    private int mCurrentFragmentId = R.id.FloatFragment;
    private int mPreToWhiteboardId = R.id.action_FloatFragment_to_WhiteboardFragment ;
    private CallViewModel mViewModel;
    private int mCurrentPos = 0 ;

    TopControlPanelFragment mTopControlPanel;
    BottomControlPanelFragment mBottomControlPanel;
    AnnotationControlPanelFragment mAnnotationControlPanelFragment;

    private RadioGroup mClDots;

    private Timer mPanelTimer;
    private Intent mScreenCaptureService;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    private boolean mIsControlPanelShowed = false;
    private boolean mEnableDeviceRating = true;
    private boolean mScreenCaptureOn = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        MiscUtils.setScreenOnFlag(getWindow());

        mEnableDeviceRating = SPUtils.getBoolean(KEY_DEVICE_RATING, true);

        mViewModel = new ViewModelProvider(this).get(CallViewModel.class);

        PanoApplication app = (PanoApplication)Utils.getApp();
        PanoRtcEngine.getIns().registerEventHandler(this);
        PanoRtcEngine.getIns().registerWhiteboardHandler(this);
        PanoRtcEngine.getIns().registerAnnotationCallback(this);
        PanoRtcEngine.getIns().registerMessageCallback(this);
        PanoRtcEngine.getIns().registerRtcMediaStatsObserver(this);

        mViewModel.setPanoRtcEngine(PanoRtcEngine.getIns().getPanoEngine());

        mClDots = findViewById(R.id.cl_dots);
        mClDots.setVisibility(View.GONE);

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
                int childCount = mClDots.getChildCount();
                if(arguments != null && arguments.containsKey(KEY_GRID_POS)){
                    int pos = arguments.getInt(KEY_GRID_POS);
                    if(pos < childCount){
                        mClDots.check(mClDots.getChildAt(pos).getId());
                        mCurrentPos = pos ;
                    }
                }
                mClDots.setVisibility(View.VISIBLE);
            }
        });

        loadSettings();

        app.mIsFrontCamera = mViewModel.mIsFrontCamera;

        joinRoom();

        mScreenCaptureService = new Intent(this, ScreenCaptureService.class);

        setupNetworkChangeReceiver(this);

        showControlPanel();
    }

    @Override
    public void onBackPressed() {
        boolean needConfirm = SPUtils.getBoolean(KEY_LEAVE_CONFIRM, false);
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return ;
        }
        if ("Whiteboard Fragment".contentEquals(current.getLabel())) {
            navController.navigateUp();
            navController.navigate(mPreToWhiteboardId);
        }
    }

    @Override
    public void orientationChanged(int newOrientation) {
        if(getResources() == null || getResources().getConfiguration() == null){
            return ;
        }
        if(getResources().getConfiguration().orientation == newOrientation){
            return ;
        }
        if(!ScreenSensorUtils.getIns().isAutoOrientation()){
            return ;
        }

        if(newOrientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    protected void onDestroy() {
        PanoRtcEngine.getIns().removeWhiteboardHandler(this);
        PanoRtcEngine.getIns().removeEventHandler(this);
        PanoRtcEngine.getIns().removeAnnotationCallback(this);
        PanoRtcEngine.getIns().removeMessageCallback(this);
        PanoRtcEngine.getIns().removeRtcMediaStatsObserver(this);

        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mViewModel.reset();
        resetTimer();
        unregisterNetworkReceiver();
    }


    /**************  RtcEngineCallback  **************/
    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        Log.i(TAG, "onChannelJoinConfirm, result="+result);
        if (mEnableDeviceRating) {
            DeviceRating deviceRating = mViewModel.rtcEngine().queryDeviceRating();
            Log.i(TAG, "mDeviceRating = " + deviceRating);
            updateProfileByDeviceRating(deviceRating);
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
        mViewModel.onWhiteboardStart();
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
    public void onChannelFailover(Constants.FailoverState failoverState) {
        View loadingView = findViewById(R.id.loading_layout);
        if(failoverState.equals(Constants.FailoverState.Reconnecting)){
            loadingView.setVisibility(View.VISIBLE);
        }else{
            loadingView.setVisibility(View.GONE);
        }
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
        PanoRtcEngine.getIns().getPanoWhiteboard().stopFollowVision();
    }

    @Override
    public void onVisionShareStarted(long userId) {
        UserInfo userInfo = mViewModel.getUserManager().getRemoteUser(userId);
        if(userInfo != null && !TextUtils.isEmpty(userInfo.getUserName())){
            String name = userInfo.getUserName();
            Toast.makeText(this, getString(R.string.whiteboard_started_vision_share, name), Toast.LENGTH_SHORT).show();
        }
        PanoRtcEngine.getIns().getPanoWhiteboard().startFollowVision();
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
        if (!AnnotationHelper.getIns().annotationEnable()) {
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
        mViewModel.clearRtcAudioLevels();
        mViewModel.setShowMuteAudioToast(muted);
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
        AlertDialog shareDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_call_share)
                .setItems(itemArray, (dialog, which) -> {
                    if (which == 0) {
                        mViewModel.onWhiteboardStart();
                        onClickWhiteboard();
                    } else if (which == 1) {
                       onClickScreenShare();
                    }
                })
                .setNegativeButton(R.string.title_button_cancel, (dialog, which) -> dialog.dismiss())
                .create();
        if (!shareDialog.isShowing()) {
            shareDialog.show();
        }
    }

    @Override
    public void onBCPanelUserList() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return ;
        }
        if ("Float Fragment".contentEquals(current.getLabel())) {
            navController.navigate(R.id.action_FloatFragment_to_UserListFragment);
        } else if ("Grid Fragment".contentEquals(current.getLabel())) {
            navController.navigate(R.id.action_GridFragment_to_UserListFragment);
        }
    }

    @Override
    public void onBCPanelMore() {
        AlertDialog moreDialog = new AlertDialog.Builder(this)
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
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
    }
    /**************  OnBottomControlPanelListener  **************/

    /**************  OnAnnotationControlPanelListener  **************/

    @Override
    public void onAnnotationStart() {
        AnnotationHelper.getIns().setAnnotationEnable(true);
        hideControlPanel();
        showAnnotationControlPanel();
    }

    @Override
    public void onAnnotationClose() {
        hideAnnotationControlPanel();
        showControlPanel();
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

    /**************  RtcMessageService.Callback  **************/
    @Override
    public void onUserMessage(long userId, byte[] data) {
        String jsonStr = new String(data);
        try {
            Log.d(TAG,"onUserMessage : userId = "+userId+" , message : "+jsonStr);
            JSONObject jsonObject = new JSONObject(jsonStr);
            String msgType = jsonObject.getString(KEY_SOUND_FEEDBACK_TYPE);
            String command = jsonObject.getString(KEY_SOUND_FEEDBACK_COMMAND);
            String desc = jsonObject.getString(KEY_SOUND_FEEDBACK_DESCRIPTION);

            if(VALUE_SOUND_FEEDBACK_TYPE_COMMAND.equals(msgType)
                    && VALUE_SOUND_FEEDBACK_COMMAND_STARTDUMP.equals(command)){
                mViewModel.rtcEngine().startAudioDump(DUMP_SIZE);
                mHandler.postDelayed(() -> {
                    mViewModel.rtcEngine().stopAudioDump();

                    RtcEngine.FeedbackInfo info = new RtcEngine.FeedbackInfo();
                    info.type = Constants.FeedbackType.Audio;
                    info.productName = "PanoVideoCall";
                    info.description = desc;
                    info.extraInfo = ((PanoApplication)Utils.getApp()).getAppUuid();
                    info.uploadLogs = true;
                    UserInfo localUser = mViewModel.getUserManager().getLocalUser();
                    if(localUser != null && !TextUtils.isEmpty(localUser.userName)){
                        info.contact = localUser.userName;
                    }
                    mViewModel.rtcEngine().sendFeedback(info);
                },TIME_OUT_DELAY);

            }
        } catch (Exception e) {
            return;
        }

    }

    /**************  RtcMessageService.Callback  **************/

    /**************  RtcMediaStatsObserver  **************/
    @Override
    public void onVideoSendStats(RtcVideoSendStats stats) {
    }

    @Override
    public void onVideoRecvStats(RtcVideoRecvStats stats) {
    }

    @Override
    public void onAudioSendStats(RtcAudioSendStats stats) {

    }

    @Override
    public void onAudioRecvStats(RtcAudioRecvStats stats) {

    }

    @Override
    public void onScreenSendStats(RtcVideoSendStats stats) {

    }

    @Override
    public void onScreenRecvStats(RtcVideoRecvStats stats) {
    }

    @Override
    public void onVideoBweStats(RtcVideoBweStats stats) {

    }

    @Override
    public void onSystemStats(RtcSystemStats stats) {

    }

    /**************  RtcMediaStatsObserver  **************/

    @Override
    protected void onResume() {
        super.onResume();
        updateFaceBeautyData();
    }

    private void updateFaceBeautyData(){
        boolean fbEnabled = SPUtils.getBoolean(KEY_ENABLE_FACE_BEAUTY, false);
        mViewModel.rtcEngine().setFaceBeautify(fbEnabled);
        if (fbEnabled) {
            float fbIntensity = SPUtils.getFloat(KEY_FACE_BEAUTY_INTENSITY, 0);
            mViewModel.rtcEngine().setFaceBeautifyIntensity(fbIntensity);
        }
    }

    /**************  RtcVideoTextureFilter  **************/

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
        updateFaceBeautyData();

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
        mViewModel.onClickAnnotationStop();
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
            if ((fragment instanceof FloatFragment && Objects.equals(current.getLabel(), "Float Fragment"))
                    || (fragment instanceof GridFragment && Objects.equals(current.getLabel(), "Grid Fragment"))
                    || (fragment instanceof WhiteboardFragment && Objects.equals(current.getLabel(), "Whiteboard Fragment"))
                    || (fragment instanceof UserListFragment && Objects.equals(current.getLabel(), "UserList Fragment"))) {
                return (CallFragment) fragment;
            }
        }

        return null;
    }

    private void loadSettings() {
        mViewModel.mIsAudioMuted = mViewModel.mAutoMuteAudio = SPUtils.getBoolean(KEY_AUTO_MUTE_AUDIO, false);
        mViewModel.mIsAudioSpeakerOpened = SPUtils.getBoolean(KEY_AUTO_START_SPEAKER, false);
        mViewModel.mAutoStartCamera = SPUtils.getBoolean(KEY_AUTO_START_CAMERA, true);
        mViewModel.mIsVideoClosed = !mViewModel.mAutoStartCamera;
        mViewModel.mEnableDebugMode = SPUtils.getBoolean(KEY_ENABLE_DEBUG_MODE, false);
        int resolution = SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2);
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
            mClDots.removeAllViews();
            int pageSize = 2 ;
            if(userCount > 4){
                pageSize = userCount /4 + 2 ;
            }
            for(int i = 0 ; i < pageSize ; i ++){
                RadioButton dotItem = (RadioButton) LayoutInflater.from(this).inflate(R.layout.layout_dot_item,mClDots,false);
                dotItem.setId(Config.sNextGeneratedId.incrementAndGet());
                mClDots.addView(dotItem);
            }
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination current = navController.getCurrentDestination();
            if(current != null){
                if(current.getId() == R.id.WhiteboardFragment){
                    mClDots.setVisibility(View.GONE);
                    return;
                }
            }
            if(mCurrentPos == pageSize){
                mClDots.check(mClDots.getChildAt(mCurrentPos - 1).getId());
                navController.navigateUp();
            }else if(mCurrentPos < pageSize){
                mClDots.check(mClDots.getChildAt(mCurrentPos).getId());
            }
            mClDots.setVisibility(View.VISIBLE);
        }
    }

    private void updateProfileByDeviceRating(DeviceRating deviceRating) {
        int resolution = SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2);

        int maxProfile = DeviceRatingTest.getIns().updateProfileByDeviceRating(deviceRating);

        if(resolution > maxProfile){
            resolution = maxProfile ;
            DeviceRatingTest.getIns().showRatingToast(resolution);
        }
        mViewModel.mLocalProfile = DeviceRatingTest.getIns().getProfileType(resolution);

        SPUtils.put(KEY_VIDEO_SENDING_RESOLUTION, resolution);
    }

    private void hideAnnotationControlPanel() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(mAnnotationControlPanelFragment).commitAllowingStateLoss();
        mIsControlPanelShowed = false;
    }

    private void showAnnotationControlPanel() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_control_panel, mAnnotationControlPanelFragment)
                .addToBackStack(null);
        try {
            ft.show(mAnnotationControlPanelFragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void onClickWhiteboard(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return ;
        }
        if ("Float Fragment".contentEquals(current.getLabel())) {
            mPreToWhiteboardId = R.id.action_FloatFragment_to_WhiteboardFragment ;
            navController.navigate(R.id.action_FloatFragment_to_WhiteboardFragment);
        } else if ("Grid Fragment".contentEquals(current.getLabel())) {
            mPreToWhiteboardId = R.id.action_GridFragment_to_WhiteboardFragment ;
            navController.navigate(R.id.action_GridFragment_to_WhiteboardFragment);
        }
    }

    private void onClickAnnotation() {
        if(mViewModel.mIsVideoClosed){
            Toast.makeText(this,R.string.msg_open_your_video,Toast.LENGTH_LONG).show();
            return ;
        }
        mViewModel.onClickAnnotationStart();
        AnnotationHelper.getIns().setAnnotationEnable(true);
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

    public static void launch(Context context) {
        Intent intent = new Intent();
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
