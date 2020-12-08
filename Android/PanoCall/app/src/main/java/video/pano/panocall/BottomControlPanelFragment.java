package video.pano.panocall;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.pano.rtc.api.Constants;


public class BottomControlPanelFragment extends Fragment {

    private CallViewModel mViewModel;
    private OnBottomControlPanelListener mListener;
    private Button mBtnAudio;
    private Button mBtnVideo;
    private Button mBtnWhiteboard;

    public BottomControlPanelFragment() {
        // Required empty public constructor
    }

    public static BottomControlPanelFragment newInstance() {
        BottomControlPanelFragment fragment = new BottomControlPanelFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_control_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(getActivity()).get(CallViewModel.class);

        mBtnAudio = view.findViewById(R.id.btn_call_audio);
        mBtnAudio.setOnClickListener(v -> {
            mViewModel.mIsAudioMuted = !mViewModel.mIsAudioMuted;
            updateAudioButtonState();
            if (mListener != null) {
                mListener.onBCPanelAudio(mViewModel.mIsAudioMuted);
            }
        });
        updateAudioButtonState();

        mBtnVideo = view.findViewById(R.id.btn_call_video);
        mBtnVideo.setOnClickListener(v -> {
            mViewModel.mIsVideoClosed = !mViewModel.mIsVideoClosed;
            updateVideoButtonState();
            if (mListener != null) {
                mListener.onBCPanelVideo(mViewModel.mIsVideoClosed);
            }
        });
        updateVideoButtonState();

        mBtnWhiteboard = view.findViewById(R.id.btn_call_whiteboard);
        mBtnWhiteboard.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBCPanelWhiteboard();
            }
        });
        updateWhiteboardButtonState();
        mViewModel.mWhiteboardState.observe(getViewLifecycleOwner(), wbs -> {
            updateWhiteboardButtonState();
        });

        view.findViewById(R.id.btn_call_settings).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBCPanelSettings();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BottomControlPanelFragment.OnBottomControlPanelListener) {
            mListener = (BottomControlPanelFragment.OnBottomControlPanelListener) context;
        }
    }

    private void updateAudioButtonState() {
        if (mViewModel.mIsAudioMuted) {
            mBtnAudio.setText(R.string.title_call_audio_muted);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_call_audio_muted, 0, 0);
        } else {
            mBtnAudio.setText(R.string.title_call_audio_normal);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_call_audio_normal, 0, 0);
        }
    }

    private void updateVideoButtonState() {
        if (mViewModel.mIsVideoClosed) {
            mBtnVideo.setText(R.string.title_call_video_closed);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_call_video_closed, 0, 0);
        } else {
            mBtnVideo.setText(R.string.title_call_video_normal);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_call_video_normal, 0, 0);
        }
    }

    void updateWhiteboardButtonState() {
        int wb_res_id = R.drawable.btn_call_wb_normal;
        if (!mViewModel.mWhiteboardState.isAvailable()) {
            wb_res_id = R.drawable.btn_call_wb_closed;
        } else if (mViewModel.mWhiteboardState.isContentUpdated()) {
            wb_res_id = R.drawable.btn_call_wb_update;
        }
        mBtnWhiteboard.setText(R.string.title_call_wb_closed);
        mBtnWhiteboard.setCompoundDrawablesWithIntrinsicBounds(0, wb_res_id, 0, 0);
    }


    // listener
    public interface OnBottomControlPanelListener {
        void onBCPanelAudio(boolean muted);
        void onBCPanelVideo(boolean closed);
        void onBCPanelWhiteboard();
        void onBCPanelSettings();
    }
}
