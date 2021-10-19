package video.pano.panocall.fragment;

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
import android.widget.TextView;

import video.pano.panocall.listener.OnBottomControlPanelListener;
import video.pano.panocall.viewmodel.CallViewModel;
import video.pano.panocall.R;

public class BottomControlPanelFragment extends Fragment {

    private CallViewModel mViewModel;
    private OnBottomControlPanelListener mListener;
    private TextView mBtnAudio;
    private TextView mBtnVideo;

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
            mViewModel.onBCPanelVideo(mViewModel.mIsVideoClosed);
        });
        updateVideoButtonState();

        view.findViewById(R.id.btn_call_share).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBCPanelShare();
            }
        });

        view.findViewById(R.id.btn_call_more).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBCPanelMore();
            }
        });

        view.findViewById(R.id.btn_call_user_list).setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onBCPanelUserList();
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
        if (context instanceof OnBottomControlPanelListener) {
            mListener = (OnBottomControlPanelListener) context;
        }
    }

    private void updateAudioButtonState() {
        if (mViewModel.mIsAudioMuted) {
            mBtnAudio.setText(R.string.title_call_audio_muted);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_mute, 0, 0);
        } else {
            mBtnAudio.setText(R.string.title_call_audio_normal);
            mBtnAudio.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_audio_normal, 0, 0);
        }
    }

    private void updateVideoButtonState() {
        if (mViewModel.mIsVideoClosed) {
            mBtnVideo.setText(R.string.title_call_video_closed);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_video_closed, 0, 0);
        } else {
            mBtnVideo.setText(R.string.title_call_video_normal);
            mBtnVideo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.svg_icon_video_normal, 0, 0);
        }
    }
}
