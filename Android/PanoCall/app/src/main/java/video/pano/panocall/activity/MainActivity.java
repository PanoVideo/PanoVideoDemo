package video.pano.panocall.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.pano.rtc.api.RtcEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;

import static video.pano.panocall.info.Config.APPID;
import static video.pano.panocall.info.Config.APP_TOKEN;
import static video.pano.panocall.info.Config.PANO_SERVER;
import static video.pano.panocall.info.Config.USER_ID;
import static video.pano.panocall.info.Constant.KEY_ROOM_ID;
import static video.pano.panocall.info.Constant.KEY_USER_NAME;
import static video.pano.panocall.info.Constant.PERMISSION_RTC_REQUEST_CODE;

public class MainActivity extends AppCompatActivity {

    private EditText mEditRoomId;
    private EditText mEditUserName;
    private ProgressBar mPBarIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTitleView();

        mEditRoomId = findViewById(R.id.edit_room_id);
        mPBarIndicator = findViewById(R.id.pbar_room_indicator);
        mEditUserName = findViewById(R.id.edit_user_name);

        setupViews();
        ensureLeaveRtcRoom();
    }

    private void initTitleView() {
        TextView titleView = findViewById(R.id.tv_title);
        ImageView rightIcon = findViewById(R.id.iv_right_icon);
        ImageView leftIcon = findViewById(R.id.iv_left_icon);

        titleView.setText(R.string.title_join_call);

        rightIcon.setVisibility(View.VISIBLE);
        rightIcon.setImageResource(R.drawable.svg_icon_setting);
        rightIcon.setOnClickListener(view ->{
            SettingsActivity.launch(MainActivity.this,false);
        });

        leftIcon.setVisibility(View.VISIBLE);
        leftIcon.setImageResource(R.drawable.svg_icon_advanced_settings);
        leftIcon.setOnClickListener(view ->{
            View customView = LayoutInflater.from(this).inflate(R.layout.dialog_room_advanced_setting, null);
            EditText appId = customView.findViewById(R.id.app_id);
            appId.setText(APPID);
            EditText appServer = customView.findViewById(R.id.app_server);
            appServer.setText(PANO_SERVER);
            EditText token = customView.findViewById(R.id.token);
            token.setText(APP_TOKEN);
            EditText userId = customView.findViewById(R.id.user_id);
            userId.setText(USER_ID);

            Dialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.room_advanced_settings_title)
                    .setView(customView)
                    .setPositiveButton(R.string.title_button_ok, (dialog1, which) -> {
                        APPID = appId.getText().toString();
                        PANO_SERVER = appServer.getText().toString();
                        APP_TOKEN = token.getText().toString();
                        USER_ID = userId.getText().toString();
                        ((PanoApplication) getApplication()).refreshPanoEngine();
                    })
                    .setNegativeButton(R.string.title_button_cancel, null)
                    .create();
            dialog.show();
        });
    }

    private void setupViews(){
        findViewById(R.id.tv_join_room).setOnClickListener(view -> {
            doJoinRoom();
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = prefs.getString(KEY_USER_NAME, "");
        String roomId = prefs.getString(KEY_ROOM_ID, "");
        if (!TextUtils.isEmpty(userName)) {
            mEditUserName.setText(userName);
        }
        if(!TextUtils.isEmpty(roomId)){
            mEditRoomId.setText(roomId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            checkPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPBarIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_RTC_REQUEST_CODE:
                if (RtcEngine.checkPermission(this).size() == 0) {
                    startPanoCall();
                } else {
                    Toast.makeText(MainActivity.this, "Some permissions are denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doJoinRoom() {
        String roomId = mEditRoomId.getText().toString();
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(MainActivity.this, "Room ID is empty", Toast.LENGTH_LONG).show();
            return;
        }
        checkPermissions();
    }

    private void startPanoCall() {
        String roomId = mEditRoomId.getText().toString();
        String userName = mEditUserName.getText().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_ROOM_ID, roomId);
        editor.apply();

        long localUserId ;
        if (!TextUtils.isEmpty(USER_ID)) {
            localUserId = Long.parseLong(USER_ID);
        } else {
            localUserId = 10000 + new Random().nextInt(5000);
        }

        if (TextUtils.isEmpty(APP_TOKEN)) {
            Toast.makeText(MainActivity.this, "Token is empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(APPID)) {
            Toast.makeText(MainActivity.this, "AppId is empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(PANO_SERVER)) {
            Toast.makeText(MainActivity.this, "AppServer is empty", Toast.LENGTH_LONG).show();
            return;
        }

        CallActivity.launch(this, APP_TOKEN, roomId, localUserId, userName);
    }

    public static void launch(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }

    // 确保离开房间。在某些case下房间未离开但是UI回到了主界面
    void ensureLeaveRtcRoom() {
        PanoApplication app = (PanoApplication)getApplication();
        Log.w(PanoApplication.TAG, "The room is not left when back to main page");
        RtcEngine rtcEngine = app.getPanoEngine();
        if(rtcEngine != null){
            rtcEngine.stopVideo();
            rtcEngine.stopPreview();
            rtcEngine.stopAudio();
            rtcEngine.leaveChannel();
            app.mIsLocalVideoStarted = false;
        }
    }

    private void checkPermissions() {
        final List<String> missed = RtcEngine.checkPermission(this);
        if (missed.size() != 0) {

            List<String> showRationale = new ArrayList<>();
            for (String permission : missed) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showRationale.add(permission);
                }
            }

            if (showRationale.size() > 0) {
                String msg = getResources().getString(R.string.msg_permission_call);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    missed.toArray(new String[0]),
                                    PERMISSION_RTC_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, missed.toArray(new String[0]), PERMISSION_RTC_REQUEST_CODE);
            }

            return;
        }

        startPanoCall();
    }

    public static boolean checkPermission(Context context, String permission) {
        return context.checkPermission(permission,
                android.os.Process.myPid(),
                android.os.Process.myUid()) ==
                PackageManager.PERMISSION_GRANTED;
    }

}
