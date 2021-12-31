package video.pano.panocall.activity;

import static video.pano.panocall.info.Config.APP_TOKEN;
import static video.pano.panocall.info.Config.USER_ID;
import static video.pano.panocall.info.Constant.FACE_BEAUTY_FRAGMENT;
import static video.pano.panocall.info.Constant.KEY_APP_UUID;
import static video.pano.panocall.info.Constant.KEY_AUDIO_UM_MUTE;
import static video.pano.panocall.info.Constant.KEY_AUTO_START_CAMERA;
import static video.pano.panocall.info.Constant.KEY_ROOM_ID;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;
import static video.pano.panocall.info.Constant.PERMISSION_RTC_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.pano.rtc.api.RtcEngine;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "MainActivity";

    private EditText mEditRoomId;
    private EditText mEditUserName;
    private ProgressBar mPBarIndicator;
    private long mLocalUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createUUID();
        initTitleView();
        initFraudView();

        mEditRoomId = findViewById(R.id.edit_room_id);
        mPBarIndicator = findViewById(R.id.pbar_room_indicator);
        mEditUserName = findViewById(R.id.edit_user_name);

        setupViews();
        ensureLeaveRtcRoom();
    }

    private void createUUID() {
        String appUuid = SPUtils.getString(KEY_APP_UUID, "");
        if (TextUtils.isEmpty(appUuid)) {
            UUID uuid = UUID.randomUUID();
            appUuid = uuid.toString().replace("-", "");
            SPUtils.put(KEY_APP_UUID, appUuid);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initTitleView() {
        TextView titleView = findViewById(R.id.tv_title);
        ImageView rightIcon = findViewById(R.id.iv_right_icon);

        titleView.setText(R.string.title_join_call);

        rightIcon.setVisibility(View.VISIBLE);
        rightIcon.setImageResource(R.drawable.svg_icon_setting);
        rightIcon.setOnClickListener(view -> {
            if (!Utils.doubleClick()) {
                SettingsActivity.launch(MainActivity.this, false);
            }
        });
    }

    private void initFraudView() {
        View fraudTipsView = findViewById(R.id.fraud_tips_view);
        findViewById(R.id.close_img).setOnClickListener(v -> {
            fraudTipsView.setVisibility(View.GONE);
        });
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void setupViews() {

        findViewById(R.id.tv_join_room).setOnClickListener(view -> {
            checkRtcPermission();
        });

        findViewById(R.id.tv_face_beauty_setting).setOnClickListener(v ->
                ContainerActivity.launch(MainActivity.this, FACE_BEAUTY_FRAGMENT,
                        getString(R.string.title_face_beauty), "")
        );

        SwitchCompat audioUnMuteSwitch = findViewById(R.id.switch_audio_un_mute);
        audioUnMuteSwitch.setChecked(SPUtils.getBoolean(KEY_AUDIO_UM_MUTE, true));
        audioUnMuteSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                SPUtils.put(KEY_AUDIO_UM_MUTE, isChecked)
        );

        SwitchCompat autoStartCameraSwitch = findViewById(R.id.switch_auto_start_camera);
        autoStartCameraSwitch.setChecked(SPUtils.getBoolean(KEY_AUTO_START_CAMERA, true));
        autoStartCameraSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                SPUtils.put(KEY_AUTO_START_CAMERA, isChecked)

        );

        String userName = SPUtils.getString(KEY_USER_NAME, "");
        String roomId = SPUtils.getString(KEY_ROOM_ID, "");
        if (!TextUtils.isEmpty(userName)) {
            mEditUserName.setText(userName);
        }
        if (!TextUtils.isEmpty(roomId)) {
            mEditRoomId.setText(roomId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            checkRtcPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPBarIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void doJoinRoom() {
        String roomId = mEditRoomId.getText().toString();
        String userName = mEditUserName.getText().toString();
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(userName)) {
            Toast.makeText(this, R.string.msg_join_alert, Toast.LENGTH_LONG).show();
            return;
        }

        SPUtils.put(KEY_USER_NAME, userName);
        SPUtils.put(KEY_ROOM_ID, roomId);

        if (!TextUtils.isEmpty(USER_ID)) {
            mLocalUserId = Long.parseLong(USER_ID);
        } else {
            mLocalUserId = 10000 + new Random().nextInt(5000);
        }

        MeetingActivity.launch(this, APP_TOKEN, roomId, mLocalUserId, userName);
    }

    // 确保离开房间。在某些case下房间未离开但是UI回到了主界面
    void ensureLeaveRtcRoom() {
        Log.w(PanoApplication.TAG, "The room is not left when back to main page");
        RtcEngine rtcEngine = PanoRtcEngine.getIns().getPanoEngine();
        if (rtcEngine != null) {
            rtcEngine.leaveChannel();
        }
        Config.sIsLocalVideoStarted = false;
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    private void checkRtcPermission() {
        if (EasyPermissions.hasPermissions(Utils.getApp(), Config.RTC_PERMISSIONS)) {
            doJoinRoom();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.pano_title_ask_again),
                    PERMISSION_RTC_REQUEST_CODE, Config.RTC_PERMISSIONS);
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(requestCode != PERMISSION_RTC_REQUEST_CODE) return ;

        if(perms.size() == Config.RTC_PERMISSIONS.length){
            doJoinRoom();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).setTitle(R.string.pano_title_ask_again).setRationale(R.string.pano_rationale_ask_again)
                    .build().show();
        } else {
            Toast.makeText(this, R.string.permission_required_title, Toast.LENGTH_SHORT).show();
        }
    }
}
