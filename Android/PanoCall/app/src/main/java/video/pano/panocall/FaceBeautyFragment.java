package video.pano.panocall;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.IVideoRender;
import com.pano.rtc.api.RtcView;

import java.util.ArrayList;
import java.util.List;

import video.pano.RendererCommon;

import static android.Manifest.permission.CAMERA;


public class FaceBeautyFragment extends Fragment {

    private static final String[] PERMISSIONS = {
            CAMERA,
    };

    private boolean mNeedSwitchCamera = false;
    private RtcView mRtcView;

    public FaceBeautyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_face_beauty, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRtcView = view.findViewById(R.id.face_beauty_view);
        mRtcView.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FILL);
        mRtcView.init(new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {}
            @Override
            public void onFrameResolutionChanged(int i, int i1, int i2) {}
        });
        mRtcView.setZOrderMediaOverlay(true);

        PanoApplication app = (PanoApplication) getActivity().getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enabled = prefs.getBoolean(RoomActivity.KEY_ENABLE_FACE_BEAUTY, false);
        float intensity = prefs.getFloat(RoomActivity.KEY_FACE_BEAUTY_INTENSITY, 0);

        SeekBar sbIntensity = view.findViewById(R.id.seekBar_face_beauty_intensity);
        Switch swFaceBeautify = view.findViewById(R.id.switch_face_beauty_enable);
        swFaceBeautify.setChecked(enabled);
        sbIntensity.setEnabled(enabled);
        sbIntensity.setProgress((int)(intensity*sbIntensity.getMax()));
        app.getPanoEngine().setFaceBeautify(enabled);
        if (enabled) {
            app.getPanoEngine().setFaceBeautifyIntensity(intensity);
        }
        swFaceBeautify.setOnClickListener(v -> {
            boolean enabled1 = swFaceBeautify.isChecked();
            app.getPanoEngine().setFaceBeautify(enabled1);
            sbIntensity.setEnabled(enabled1);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(RoomActivity.KEY_ENABLE_FACE_BEAUTY, enabled1);
            editor.apply();
        });
        sbIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float intensity = seekBar.getProgress()/(float)sbIntensity.getMax();
                app.getPanoEngine().setFaceBeautifyIntensity(intensity);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(RoomActivity.KEY_FACE_BEAUTY_INTENSITY, intensity);
                editor.apply();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkCameraPermission(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        PanoApplication app = (PanoApplication) getActivity().getApplication();
        if (!app.mIsLocalVideoStarted) {
            app.getPanoEngine().stopPreview();
        } else if (mNeedSwitchCamera) {
            app.getPanoEngine().switchCamera();
        }
        app.getPanoEngine().setLocalVideoRender(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RoomActivity.PERMISSION_REQUEST_CODE:
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

    private void openCamera()
    {
        PanoApplication app = (PanoApplication) getActivity().getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int resolution = Integer.parseInt(prefs.getString(RoomActivity.KEY_VIDEO_SENDING_RESOLUTION, "1"));
        Constants.VideoProfileType profile = Constants.VideoProfileType.HD720P;;
        if (resolution == 1) {
            profile = Constants.VideoProfileType.Standard;
        }

        mNeedSwitchCamera = false;
        app.getPanoEngine().setLocalVideoRender(mRtcView);
        if (!app.mIsLocalVideoStarted) {
            mRtcView.setMirror(true);
            app.getPanoEngine().startPreview(profile, true);
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
        for(String permission : PERMISSIONS) {
            if(!MainActivity.checkPermission(context, permission)) {
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
                                    RoomActivity.PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), ungranted.toArray(new String[0]), RoomActivity.PERMISSION_REQUEST_CODE);
            }

            return;
        }

        openCamera();
    }
}
