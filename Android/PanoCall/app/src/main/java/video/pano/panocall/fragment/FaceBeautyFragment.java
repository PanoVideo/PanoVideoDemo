package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.KEY_ENABLE_FACE_BEAUTY;
import static video.pano.panocall.info.Constant.KEY_FACE_BEAUTY_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.PERMISSION_REQUEST_CODE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcView;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import video.pano.panocall.R;
import video.pano.panocall.info.Config;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;

public class FaceBeautyFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "FaceBeautyFragment";

    private boolean mNeedSwitchCamera = false;

    private RtcView mRtcView;
    private boolean mEnabled;
    private float mIntensity;
    private SeekBar mSbIntensity;
    private SwitchCompat mSwFaceBeautify;
    private boolean mIsPause;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_beauty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRtcView = view.findViewById(R.id.face_beauty_view);
        mRtcView.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FILL);
        mRtcView.setZOrderMediaOverlay(true);
        getData();
        initFaceBeauty(view);
        showCamera();
    }

    private void getData() {
        mEnabled = SPUtils.getBoolean(KEY_ENABLE_FACE_BEAUTY, false);
        mIntensity = SPUtils.getFloat(KEY_FACE_BEAUTY_INTENSITY, 0);
    }

    private void updateFaceBeautyData() {
        mSwFaceBeautify.setChecked(mEnabled);
        mSbIntensity.setEnabled(mEnabled);
        mSbIntensity.setProgress((int) (mIntensity * mSbIntensity.getMax()));
        PanoRtcEngine.getIns().getPanoEngine().setFaceBeautify(mEnabled);

        if (mEnabled) {
            PanoRtcEngine.getIns().getPanoEngine().setFaceBeautifyIntensity(mIntensity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mIsPause){
            openCamera();
            mIsPause = false ;
        }
        getData();
        updateFaceBeautyData();
    }

    private void initFaceBeauty(View view) {
        mSbIntensity = view.findViewById(R.id.seekBar_face_beauty_intensity);
        mSwFaceBeautify = view.findViewById(R.id.switch_face_beauty_enable);

        mSwFaceBeautify.setOnClickListener(v -> {
            mEnabled = mSwFaceBeautify.isChecked();
            PanoRtcEngine.getIns().getPanoEngine().setFaceBeautify(mEnabled);
            mSbIntensity.setEnabled(mEnabled);

            SPUtils.put(KEY_ENABLE_FACE_BEAUTY, mEnabled);
        });
        mSbIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float intensity = seekBar.getProgress() / (float) mSbIntensity.getMax();
                PanoRtcEngine.getIns().getPanoEngine().setFaceBeautifyIntensity(intensity);
                SPUtils.put(KEY_FACE_BEAUTY_INTENSITY, intensity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (!Config.sIsLocalVideoStarted) {
            PanoRtcEngine.getIns().getPanoEngine().stopPreview();
        } else if (mNeedSwitchCamera) {
            PanoRtcEngine.getIns().getPanoEngine().switchCamera();
        }
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(null);
        mIsPause = true;
    }

    public void openCamera() {
        int resolution = SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2);
        Constants.VideoProfileType profile = DeviceRatingTest.getIns().getProfileType(resolution);

        mNeedSwitchCamera = false;
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(mRtcView);
        if (!Config.sIsLocalVideoStarted) {
            mRtcView.setMirror(true);
            PanoRtcEngine.getIns().getPanoEngine().startPreview(profile, true);
        } else {
            mRtcView.setMirror(Config.sIsFrontCamera);
        }
    }

    public void showCamera() {
        if (EasyPermissions.hasPermissions(Utils.getApp(), Config.FACE_BEAUTY_PERMISSION)) {
            openCamera();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_permission_required_title),
                    PERMISSION_REQUEST_CODE, Config.FACE_BEAUTY_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果转发给EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).setTitle(R.string.pano_title_ask_again).setRationale(R.string.pano_rationale_ask_again)
                    .build().show();
        }
    }
}
