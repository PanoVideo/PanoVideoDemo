package video.pano.panocall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcChannelConfig;

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

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import video.pano.panocall.utils.MiscUtils;


public class CallActivity extends AppCompatActivity implements PanoEventHandler,
        PanoWhiteboardHandler,
        CallFragment.OnControlEventListener,
        TopControlPanelFragment.OnTopControlPanelListener,
        BottomControlPanelFragment.OnBottomControlPanelListener {
    public static final String TAG = "PVC";
    private static final long kCountDownThreshold = 5*60*1000; // 5min

    private CallViewModel mViewModel;
    private int mCurrentFragmentId = R.id.FloatFragment;

    TopControlPanelFragment mTopControlPanel;
    BottomControlPanelFragment mBottomControlPanel;
    boolean mIsControlPanelShowed = false;

    private Timer mPanelTimer;
    private CountDownTimer mCountDownTimer;
    private long mCountDownRemain = 0;
    private long mCountDownTick = 0;
    private TextView mCountDownView;
    private ConstraintLayout mClDots;

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
        //MiscUtils.hideWindowStatusBar(getWindow());

        mViewModel = new ViewModelProvider(this).get(CallViewModel.class);

        PanoApplication app = (PanoApplication)getApplication();
        app.registerEventHandler(this);
        app.registerWhiteboardHandler(this);
        mViewModel.setPanoRtcEngine(app.getPanoEngine());
        mViewModel.setPanoEngineCallback(app.getPanoCallback());

        mClDots = findViewById(R.id.cl_dots);
        ImageView imgDot1 = findViewById(R.id.img_dot_1);
        ImageView imgDot2 = findViewById(R.id.img_dot_2);
        imgDot1.setSelected(true);

        mCountDownView = findViewById(R.id.tv_call_count_down);
        mCountDownView.setVisibility(View.GONE);

        String roomId = getIntent().getStringExtra(RoomActivity.KEY_ROOM_ID);
        mTopControlPanel = TopControlPanelFragment.newInstance(roomId);
        mBottomControlPanel = BottomControlPanelFragment.newInstance();

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
    }

    @Override
    public void onBackPressed() {
        if (mViewModel.mIsRoomJoined) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean needConfirm = prefs.getBoolean(RoomActivity.KEY_LEAVE_CONFIRM, true);
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
        }
        onConfirmedBackPressed();
    }

    @Override
    protected void onDestroy() {
        PanoApplication app = (PanoApplication)getApplication();
        app.removeWhiteboardHandler(this);
        app.removeEventHandler(this);
        app.mIsRoomJoined = false;
        super.onDestroy();
        mViewModel.reset();
        resetTimer();
    }


    // 加入房间
    private boolean joinRoom() {
        Intent intent = getIntent();
        long userId = intent.getLongExtra(RoomActivity.KEY_USER_ID, 0);
        String roomId = intent.getStringExtra(RoomActivity.KEY_ROOM_ID);
        String token = intent.getStringExtra(RoomActivity.KEY_PANO_TOKEN);
        String userName = intent.getStringExtra(RoomActivity.KEY_USER_NAME);
        boolean isHost = intent.getBooleanExtra(RoomActivity.KEY_IS_HOST, false);
        mViewModel.mIsHost = isHost;
        mViewModel.mWhiteboardState.setRoleType(isHost ? Constants.WBRoleType.Admin : Constants.WBRoleType.Attendee);

        // save room info
        PanoApplication app = (PanoApplication) getApplication();
        app.mFeedbackRoomId = roomId;
        app.mFeedbackUserId = userId;
        app.mFeedbackUserName = userName;
        app.mIsRoomJoined = false;
        app.mIsLocalVideoStarted = false;

        UserManager.UserInfo localUser = new UserManager.UserInfo(userId, userName);
        mViewModel.getUserManager().setLocalUser(localUser);

        // reset audio speaker state
        mViewModel.rtcEngine().setLoudspeakerStatus(mViewModel.mIsAudioSpeakerOpened);

        // face beauty settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fbEnabled = prefs.getBoolean(RoomActivity.KEY_ENABLE_FACE_BEAUTY, false);
        mViewModel.rtcEngine().setFaceBeautify(fbEnabled);
        if (fbEnabled) {
            float fbIntensity = prefs.getFloat(RoomActivity.KEY_FACE_BEAUTY_INTENSITY, 0);
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
        PanoApplication app = (PanoApplication)getApplication();
        app.mIsLocalVideoStarted = false;
        app.mIsRoomJoined = false;
    }

    private void onConfirmedBackPressed() {
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
                ft.setCustomAnimations(R.anim.panel_appear, R.anim.panel_disappear)
                        .show(mTopControlPanel)
                        .show(mBottomControlPanel)
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
            ft.setCustomAnimations(R.anim.panel_appear, R.anim.panel_disappear)
                    .remove(mTopControlPanel)
                    .remove(mBottomControlPanel)
                    .commitAllowingStateLoss();
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
            if ((fragment instanceof FloatFragment && current.getLabel().equals("Float Fragment")) ||
                    (fragment instanceof GridFragment && current.getLabel().equals("Grid Fragment")) ||
                    (fragment instanceof WhiteboardFragment && current.getLabel().equals("Whiteboard Fragment"))) {
                return (CallFragment) fragment;
            }
        }

        return null;
    }

    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mViewModel.mAutoMuteAudio = prefs.getBoolean(RoomActivity.KEY_AUTO_MUTE_AUDIO, false);
        mViewModel.mIsAudioSpeakerOpened = prefs.getBoolean(RoomActivity.KEY_AUTO_START_SPEAKER, true);
        mViewModel.mAutoStartCamera = prefs.getBoolean(RoomActivity.KEY_AUTO_START_CAMERA, true);
        mViewModel.mEnableDebugMode = prefs.getBoolean(RoomActivity.KEY_ENABLE_DEBUG_MODE, false);
        int resolution = Integer.parseInt(prefs.getString(RoomActivity.KEY_VIDEO_SENDING_RESOLUTION, "1"));
        if (resolution == 1) {
            mViewModel.mLocalProfile = Constants.VideoProfileType.Standard;
        } else {
            mViewModel.mLocalProfile = Constants.VideoProfileType.HD720P;
        }
    }

    private void resetTimer() {
        if (mPanelTimer != null) {
            mPanelTimer.cancel();
            mPanelTimer.purge();
        }
    }

    private String tickToCountDownText(long tick) {
        long seconds = tick / 1000;
        long sec = seconds % 60;
        long min = seconds / 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void startCountDown(long remain) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownView.setVisibility(View.VISIBLE);
        mCountDownView.setText(tickToCountDownText(remain));
        mCountDownTimer = new CountDownTimer(remain, 1000) {

            public void onTick(long millisUntilFinished) {
                long currTick = System.currentTimeMillis();
                long r = mCountDownRemain + mCountDownTick - currTick;
                mCountDownView.setText(tickToCountDownText(r));
            }

            public void onFinish() {
                mCountDownView.setText(R.string.msg_call_ended);
            }
        }.start();
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


    // -------------------------- PANO Event Handler --------------------------
    @Override
    public void onChannelJoinConfirm(Constants.QResult result) {
        Log.i(TAG, "onChannelJoinConfirm, result="+result);
        mViewModel.mIsRoomJoined = result == Constants.QResult.OK;
        PanoApplication app = (PanoApplication)getApplication();
        app.mIsRoomJoined = mViewModel.mIsRoomJoined;
        showControlPanel();
        //Toast.makeText(CallActivity.this, "onChannelJoinConfirm result=" + result, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onChannelLeaveIndication(Constants.QResult result) {
        Log.i(TAG, "onChannelLeaveIndication, result="+result);
        mViewModel.mIsRoomJoined = false;
        PanoApplication app = (PanoApplication)getApplication();
        app.mIsRoomJoined = mViewModel.mIsRoomJoined;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.msg_call_left_alert);
        builder.setPositiveButton(R.string.title_button_ok, (dialog, id) -> onConfirmedBackPressed());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onChannelCountDown(long remain) {
        Log.i(TAG, "onChannelCountDown, remian="+remain);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownRemain = remain * 1000;
        mCountDownTick = System.currentTimeMillis();
        if (mCountDownRemain <= kCountDownThreshold) {
            startCountDown(mCountDownRemain);
        } else {
            mCountDownView.setVisibility(View.GONE);
            long r = mCountDownRemain - kCountDownThreshold;
            mCountDownTimer = new CountDownTimer(r, r) {
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    long currTick = System.currentTimeMillis();
                    startCountDown(mCountDownRemain + mCountDownTick - currTick);
                }
            }.start();
        }
    }
    @Override
    public void onUserJoinIndication(long userId, String userName) {
        checkDotIndicatorState();
    }
    @Override
    public void onUserLeaveIndication(long userId, Constants.UserLeaveReason reason) {
        checkDotIndicatorState();
    }
    @Override
    public void onUserAudioStart(long userId) {}
    @Override
    public void onUserAudioStop(long userId) {}
    @Override
    public void onUserAudioSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserVideoStart(long userId, Constants.VideoProfileType maxProfile) {}
    @Override
    public void onUserVideoStop(long userId) {}
    @Override
    public void onUserVideoSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserAudioMute(long userId) {}
    @Override
    public void onUserAudioUnmute(long userId) {}
    @Override
    public void onUserVideoMute(long userId) {}
    @Override
    public void onUserVideoUnmute(long userId) {}
    @Override
    public void onUserScreenStart(long userId) {}
    @Override
    public void onUserScreenStop(long userId) {}
    @Override
    public void onUserScreenSubscribe(long userId, Constants.MediaSubscribeResult result) {}
    @Override
    public void onUserScreenMute(long userId) {}
    @Override
    public void onUserScreenUnmute(long userId) {}

    @Override
    public void onWhiteboardAvailable() {

    }

    @Override
    public void onWhiteboardUnavailable() {

    }

    @Override
    public void onWhiteboardStart() {

    }

    @Override
    public void onWhiteboardStop() {

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


    // -------------------------- PANO Whiteboard Handler --------------------------
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {}
    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {}
    @Override
    public void onViewScaleChanged(float scale) {}
    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        mViewModel.mWhiteboardState.setRoleType(newRole);
    }
    @Override
    public void onContentUpdated() {
        mViewModel.mWhiteboardState.setContentUpdated(true);
    }
    @Override
    public void onMessage(long userId, byte[] bytes) {

    }



    // -------------------------- CallFragment.OnControlEventListener --------------------------
    @Override
    public void onShowHideControlPanel() {
        if (mIsControlPanelShowed) {
            hideControlPanel();
        } else {
            showControlPanel();
        }
    }


    // -------------------------- TCPanel.OnControlEventListener --------------------------
    @Override
    public void onTCPanelAudio(boolean isSpeaker) {
        mViewModel.rtcEngine().setLoudspeakerStatus(isSpeaker);
    }
    @Override
    public void onTCPanelSwitchCamera() {
        mViewModel.rtcEngine().switchCamera();
        mViewModel.mIsFrontCamera = !mViewModel.mIsFrontCamera;
        PanoApplication app = (PanoApplication)getApplication();
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

    // -------------------------- BCPanel.OnControlEventListener --------------------------
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
    public void onBCPanelWhiteboard() {
        if (!mViewModel.mWhiteboardState.isAvailable()) {
            Toast.makeText(this, R.string.msg_whiteboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavDestination current = navController.getCurrentDestination();
        if (current == null) {
            return ;
        }
        if (current.getLabel().equals("Float Fragment")) {
            navController.navigate(R.id.action_FloatFragment_to_WhiteboardFragment);
        } else if (current.getLabel().equals("Grid Fragment")) {
            navController.navigate(R.id.action_GridFragment_to_WhiteboardFragment);
        }
    }
    @Override
    public void onBCPanelSettings() {
        Intent intent = new Intent(CallActivity.this, SettingsActivity.class);
        startActivity(intent);
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


    public static void launch(Context context, String token, String roomId, long userId, String userName, boolean isHost) {
        Intent intent = new Intent();
        intent.putExtra(RoomActivity.KEY_PANO_TOKEN, token);
        intent.putExtra(RoomActivity.KEY_ROOM_ID, roomId);
        intent.putExtra(RoomActivity.KEY_USER_ID, userId);
        intent.putExtra(RoomActivity.KEY_USER_NAME, userName);
        intent.putExtra(RoomActivity.KEY_IS_HOST, isHost);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(context, CallActivity.class);
        context.startActivity(intent);
    }

}
