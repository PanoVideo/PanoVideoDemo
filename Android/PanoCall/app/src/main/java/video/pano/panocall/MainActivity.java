package video.pano.panocall;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pano.rtc.api.RtcEngine;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {
            INTERNET,
            ACCESS_NETWORK_STATE,
            ACCESS_WIFI_STATE,
            WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btn_start_call).setOnClickListener(view -> {
            enterRoom(false);
        });
        findViewById(R.id.btn_join_call).setOnClickListener(view -> {
            enterRoom(true);
        });

        ensureLeaveRtcRoom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RoomActivity.PERMISSION_REQUEST_CODE:
                if (getUngrantedPermissions(this).size() != 0) {
                    Toast.makeText(MainActivity.this, "Some permissions are denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void enterRoom(boolean isJoin) {
        RoomActivity.launch(this, isJoin);
    }

    // 确保离开房间。在某些case下房间未离开但是UI回到了主界面
    void ensureLeaveRtcRoom() {
        PanoApplication app = (PanoApplication)getApplication();
        if (app.mIsRoomJoined) {
            Log.w(PanoApplication.TAG, "The room is not left when back to main page");
            RtcEngine rtcEngine = app.getPanoEngine();
            rtcEngine.stopVideo();
            rtcEngine.stopPreview();
            rtcEngine.stopAudio();
            rtcEngine.leaveChannel();
            app.mIsLocalVideoStarted = false;
            app.mIsRoomJoined = false;
        }
    }

    private List<String> getUngrantedPermissions(Context context) {
        List<String> ungranted = new ArrayList<>();
        for(String permission : PERMISSIONS) {
            if(!checkPermission(context, permission)) {
                ungranted.add(permission);
            }
        }
        return ungranted;
    }

    static boolean checkPermission(Context context, String permission) {
        return context.checkPermission(permission,
                android.os.Process.myPid(),
                android.os.Process.myUid()) ==
                PackageManager.PERMISSION_GRANTED;
    }
}
