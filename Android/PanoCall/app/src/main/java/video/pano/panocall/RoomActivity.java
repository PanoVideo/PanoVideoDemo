package video.pano.panocall;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.pano.rtc.api.RtcEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomActivity extends AppCompatActivity {
    private static final String KEY_IS_JOIN = "is_join";
    public static final String KEY_PANO_TOKEN = "key_pano_token";
    public static final String KEY_ROOM_ID = "key_room_id";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_IS_HOST = "key_is_host";
    public static final String KEY_AUTO_MUTE_AUDIO = "key_auto_mute_audio";
    public static final String KEY_AUTO_START_CAMERA = "key_auto_start_camera";
    public static final String KEY_VIDEO_SENDING_RESOLUTION = "key_video_sending_resolution";
    public static final String KEY_AUTO_START_SPEAKER = "key_auto_start_speaker";
    public static final String KEY_ENABLE_DEBUG_MODE = "key_enable_debug_mode";
    public static final String KEY_ENABLE_FACE_BEAUTY = "key_enable_face_beauty";
    public static final String KEY_FACE_BEAUTY_INTENSITY = "key_face_beauty_intensity";
    public static final String KEY_LEAVE_CONFIRM = "key_leave_confirm";
    public static final String KEY_APP_UUID = "key_app_uuid";
    public static final int PERMISSION_REQUEST_CODE = 10;

    private EditText mEditRoomId;
    private EditText mEditUserName;
    private ProgressBar mPBarIndicator;
    private boolean mIsHost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        mEditRoomId = findViewById(R.id.edit_room_id);
        Button btnJoin = findViewById(R.id.btn_join_channel);
        Intent intent = getIntent();
        boolean isJoin = intent.getBooleanExtra(KEY_IS_JOIN, false);
        if (isJoin) {
            setTitle(R.string.title_join_call);
            btnJoin.setText(R.string.title_join_channel);
        } else {
            long roomId = (long)10000000 + new Random().nextInt(89999999);
            mEditRoomId.setText("" + roomId);
            setTitle(R.string.title_start_call);
            btnJoin.setText(R.string.title_start_channel);
            mIsHost = true;
        }

        btnJoin.setOnClickListener(view -> {
            doJoinRoom();
        });


        mEditRoomId.setRawInputType(Configuration.KEYBOARD_QWERTY);
        mEditUserName = findViewById(R.id.edit_user_name);
        mPBarIndicator = findViewById(R.id.pbar_room_indicator);

        if (isJoin) {
            mEditRoomId.requestFocus();
        } else {
            mEditUserName.requestFocus();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = prefs.getString(KEY_USER_NAME, "");
        if (!userName.isEmpty()) {
            mEditUserName.setText(userName);
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
            case PERMISSION_REQUEST_CODE:
                if (RtcEngine.checkPermission(this).size() == 0) {
                    startPanoCall();
                } else {
                    Toast.makeText(RoomActivity.this, "Some permissions are denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void doJoinRoom() {
        String roomId = mEditRoomId.getText().toString();
        if (roomId.isEmpty()) {
            Toast.makeText(RoomActivity.this, "Room ID is empty", Toast.LENGTH_LONG).show();
            return;
        }
        checkPermissions();
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
                new AlertDialog.Builder(RoomActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    missed.toArray(new String[0]),
                                    PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, missed.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }

            return;
        }

        startPanoCall();
    }

    private void startPanoCall() {
        String roomId = mEditRoomId.getText().toString();
        String userName = mEditUserName.getText().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
        long localUserId = 10000 + new Random().nextInt(5000);
        CallActivity.launch(this, PanoApplication.APP_TOKEN, roomId, localUserId, userName, mIsHost);
    }

    public static void launch(Context context, boolean isJoin) {
        Intent intent = new Intent();
        intent.putExtra(KEY_IS_JOIN, isJoin);
        intent.setClass(context, RoomActivity.class);
        context.startActivity(intent);
    }

}
