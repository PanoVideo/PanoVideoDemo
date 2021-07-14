package video.pano.panocall.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import video.pano.panocall.R;
import video.pano.panocall.viewmodel.CallViewModel;

import static video.pano.panocall.info.Constant.KEY_ANNOTATION_COLOR_ID;
import static video.pano.panocall.info.Constant.KEY_ANNOTATION_INTENSITY;

public class AnnotationControlPanelFragment extends Fragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    public static final int DEFAULT_COLOR = 0xFFD4380D ;
    public static final int DEFAULT_WIDTH = 5 ;
    public static final int DEFAULT_COLOR_ID = R.id.rb_color_red ;

    private RadioButton mDefaultCheckedBtn;
    private ConstraintLayout mPopupView;
    private TextView mStrokeTv;
    private SeekBar mStrokeSeek;
    private RadioGroup mRadioColorGroup;
    private CallViewModel mViewModel;

    public static AnnotationControlPanelFragment newInstance() {
        AnnotationControlPanelFragment fragment = new AnnotationControlPanelFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_annotation_control_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(getActivity()).get(CallViewModel.class);

        mPopupView = view.findViewById(R.id.cl_annotation_popup_view);
        mStrokeSeek = view.findViewById(R.id.seekBar_annotation_stroke_intensity);
        mStrokeSeek.setOnSeekBarChangeListener(this);
        mStrokeTv = view.findViewById(R.id.tv_stroke_intensity);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new ButtonCheckedChangeListener());

        mRadioColorGroup = view.findViewById(R.id.radio_color_group);
        mRadioColorGroup.setOnCheckedChangeListener(new ColorCheckedChangeListener());

        mDefaultCheckedBtn = view.findViewById(R.id.pencil);
        mDefaultCheckedBtn.setOnClickListener(this);

        view.findViewById(R.id.back).setOnClickListener(this);
        view.findViewById(R.id.arrow).setOnClickListener(this);
        view.findViewById(R.id.ashcan).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int annotationId = prefs.getInt(KEY_ANNOTATION_COLOR_ID, DEFAULT_COLOR_ID);
        int annotationProgress = prefs.getInt(KEY_ANNOTATION_INTENSITY,DEFAULT_WIDTH);

        mStrokeTv.setText(String.valueOf(annotationProgress));
        mStrokeSeek.setProgress(annotationProgress);

        mRadioColorGroup.check(annotationId);

    }

    @Override
    public void onResume() {
        super.onResume();
        mDefaultCheckedBtn.setChecked(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            if (mViewModel != null) {
                mViewModel.onAnnotationStop();
            }
        }else if(v.getId() == R.id.pencil){
            mPopupView.setVisibility(mPopupView.getVisibility() == View.VISIBLE ?
                    View.GONE : View.VISIBLE);
        }else{
            mPopupView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStrokeTv.setText(String.valueOf(progress));
        if (mViewModel != null) {
            mViewModel.onProgressChange(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class ButtonCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = group.findViewById(checkedId);
            if(checkedId == R.id.pencil){
                if (mViewModel != null) {
                    mViewModel.onClickPencil(radioButton.isChecked());
                }
            }else if(checkedId == R.id.arrow){
                if (mViewModel != null) {
                    mViewModel.onClickArrow(radioButton.isChecked());
                }
            }else if(checkedId == R.id.ashcan){
                if (mViewModel != null) {
                    mViewModel.onClickEraser(radioButton.isChecked());
                }
            }
        }
    }

    class ColorCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_color_black){
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(0,checkedId);
                }
            }else if (checkedId == R.id.rb_color_red) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(1,checkedId);
                }
            }else if (checkedId == R.id.rb_color_orange) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(2,checkedId);
                }
            }else if (checkedId == R.id.rb_color_yellow) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(3,checkedId);
                }
            }else if (checkedId == R.id.rb_color_green) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(4,checkedId);
                }
            }else if (checkedId == R.id.rb_color_turquoise) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(5,checkedId);
                }
            }else if (checkedId == R.id.rb_color_azure) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(6,checkedId);
                }
            }else if (checkedId == R.id.rb_color_blue) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(7,checkedId);
                }
            }else if (checkedId == R.id.rb_color_purple) {
                if (mViewModel != null) {
                    mViewModel.onCheckedColor(8,checkedId);
                }
            }
        }
    }

}
