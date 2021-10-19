package video.pano.panocall.fragment;

import static android.Manifest.permission.CAMERA;
import static video.pano.panocall.info.Constant.KEY_ENABLE_FACE_BEAUTY;
import static video.pano.panocall.info.Constant.KEY_FACE_BEAUTY_INTENSITY;
import static video.pano.panocall.info.Constant.KEY_VIDEO_SENDING_RESOLUTION;
import static video.pano.panocall.info.Constant.PERMISSION_REQUEST_CODE;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcView;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.PanoApplication;
import video.pano.panocall.R;
import video.pano.panocall.activity.MainActivity;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.DeviceRatingTest;
import video.pano.panocall.utils.SPUtils;
import video.pano.panocall.utils.Utils;


public class FaceBeautyFragment extends BaseSettingFragment{

    private static final String TAG = "FaceBeautyFragment";

    private static final String[] PERMISSIONS = {
            CAMERA,
    };

    private boolean mNeedSwitchCamera = false;

    private RtcView mRtcView;
    private boolean mEnabled;
    private float mIntensity;
    private SeekBar mSbIntensity;
    private SwitchCompat mSwFaceBeautify;

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

        initFaceBeauty(view);
    }

    private void updateFaceBeautyData(){
        mEnabled = SPUtils.getBoolean(KEY_ENABLE_FACE_BEAUTY, false);
        mIntensity = SPUtils.getFloat(KEY_FACE_BEAUTY_INTENSITY, 0);

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
        checkCameraPermission(getActivity());
        updateFaceBeautyData();
    }

    private void initFaceBeauty(View view){

        mSbIntensity = view.findViewById(R.id.seekBar_face_beauty_intensity);
        mSwFaceBeautify = view.findViewById(R.id.switch_face_beauty_enable);

        mSwFaceBeautify.setOnClickListener(v -> {
            boolean enabled1 = mSwFaceBeautify.isChecked();
            PanoRtcEngine.getIns().getPanoEngine().setFaceBeautify(enabled1);
            mSbIntensity.setEnabled(enabled1);
            SPUtils.put(KEY_ENABLE_FACE_BEAUTY, enabled1);
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
        PanoApplication app = (PanoApplication) Utils.getApp();
        if (!app.mIsLocalVideoStarted) {
            PanoRtcEngine.getIns().getPanoEngine().stopPreview();
        } else if (mNeedSwitchCamera) {
            PanoRtcEngine.getIns().getPanoEngine().switchCamera();
        }
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (getUngrantedPermissions(getActivity()).size() == 0) {
                    openCamera();
                } else {
                    Toast.makeText(getActivity(), "Some permissions are denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openCamera() {
        PanoApplication app = (PanoApplication) Utils.getApp();
        int resolution = SPUtils.getInt(KEY_VIDEO_SENDING_RESOLUTION, 2);
        Constants.VideoProfileType profile = DeviceRatingTest.getIns().getProfileType(resolution);

        mNeedSwitchCamera = false;
        PanoRtcEngine.getIns().getPanoEngine().setLocalVideoRender(mRtcView);
        if (!app.mIsLocalVideoStarted) {
            mRtcView.setMirror(true);
            PanoRtcEngine.getIns().getPanoEngine().startPreview(profile, true);
        } else {
            if (!app.mIsFrontCamera) {
                //app.getPanoEngine().switchCamera();
                //mNeedSwitchCamera = true;
            }
            mRtcView.setMirror(app.mIsFrontCamera);
        }
    }

    private List<String> getUngrantedPermissions(Context context) {
        List<String> ungranted = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (!MainActivity.checkPermission(context, permission)) {
                ungranted.add(permission);
            }
        }
        return ungranted;
    }

    private void checkCameraPermission(Context context) {
        List<String> ungranted = getUngrantedPermissions(context);

        if (ungranted.size() != 0) {

            List<String> showRationale = new ArrayList<>();
            for (String permission : ungranted) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                    showRationale.add(permission);
                }
            }

            if (showRationale.size() > 0) {
                String msg = getResources().getString(R.string.msg_permission_update);
                new AlertDialog.Builder(context)
                        .setMessage(msg)
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(getActivity(),
                                    ungranted.toArray(new String[0]),
                                    PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), ungranted.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }

            return;
        }

        openCamera();
    }
}
