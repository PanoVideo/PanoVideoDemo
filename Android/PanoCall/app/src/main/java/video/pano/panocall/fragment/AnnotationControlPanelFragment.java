package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.FROM_GRAPHICS;
import static video.pano.panocall.info.Constant.FROM_OTHER;
import static video.pano.panocall.info.Constant.FROM_PENCIL;
import static video.pano.panocall.info.Constant.FROM_TEXT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.PanoAnnotation;

import video.pano.panocall.R;
import video.pano.panocall.info.FillColor;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.viewmodel.MeetingViewModel;

public class AnnotationControlPanelFragment extends Fragment {

    private TextView mTvPencilInt;
    private TextView mTvTextInt;
    private SeekBar mPencilSeek;
    private SeekBar mTextSeek;
    private RadioGroup mPencilColorGroup;
    private RadioGroup mTextColorGroup;
    private RadioGroup mGraphicsColorGroup;
    private RadioGroup mGraphicsToolsGroup;
//    private RadioGroup mBottomToolGroup;
    private RadioButton mPencilRadio;

    private View mBottomPopupView;
    private View mPencilPopupView;
    private View mGraphicsPopupView;
    private View mTextPopupView;

    private Constants.WBToolType mToolType = Constants.WBToolType.None;
    private Constants.WBToolType mGraphicsToolsType = Constants.WBToolType.None ;

    private FillColor mTextFillColor = FillColor.Black;
    private FillColor mPencilFillColor = FillColor.Black;
    private FillColor mGraphicsFillColor = FillColor.Black;

    private int mLineWidth = 10;
    private int mFontSize = 36;

    private int mTextColorId = R.id.rb_text_color_black;
    private int mPencilColorId = R.id.rb_pencil_color_black;
    private int mGraphicsColorId = R.id.rb_graphics_color_black;
    private int mGraphicsToolsId = R.id.rb_graphics_line;

    private MeetingViewModel mViewModel;
    private PanoAnnotation mAnnotation;

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
        initViewModel();
        initBottomGraphicsPopup(view);
        initBottomPencilPopup(view);
        initBottomTextPopup(view);

        initTopToolsBar(view);
        //bottom tools bar
        initBottomToolsBar(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAnnotation = AnnotationHelper.getIns().getAnnotation();
        init();
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(getActivity()).get(MeetingViewModel.class);
    }


    private void init() {
        mTvPencilInt.setText(String.valueOf(mLineWidth));
        mPencilSeek.setProgress(mLineWidth);
        mPencilColorGroup.check(mPencilColorId);

        if(mAnnotation != null){
            mAnnotation.setLineWidth(mLineWidth);
            mAnnotation.setColor(mPencilFillColor.getValue());
            mAnnotation.setToolType(Constants.WBToolType.Path);
        }
    }

    private void initTopToolsBar(View rootView){
        rootView.findViewById(R.id.img_back).setOnClickListener( v ->{
            mViewModel.onClickAnnotationStop();
//            reset();
        });
    }
    
    private void initBottomToolsBar(View rootView) {
//        mBottomToolGroup = rootView.findViewById(R.id.rg_bottom_toolbar);

        mPencilRadio = rootView.findViewById(R.id.rb_toolbar_pencil);
        mPencilRadio.setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_PENCIL);
            showOrHidePencilPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_graphics).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_GRAPHICS);
            showOrHideGraphicsPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_text).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_TEXT);
            showOrHideTextPopupView();
        });

        rootView.findViewById(R.id.rb_toolbar_delete).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_OTHER);
            selectToolType(Constants.WBToolType.Delete);
        });

        rootView.findViewById(R.id.rb_toolbar_select).setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_OTHER);
            selectToolType(Constants.WBToolType.Select);
        });

        mBottomPopupView = rootView.findViewById(R.id.cl_bottom_toolbar_popup_view);
        mBottomPopupView.setOnClickListener(v -> {
            showOrHideOtherPopupView(FROM_OTHER);
            mViewModel.onAnnotationToolsClick();
        });
    }

    private void initBottomPencilPopup(View rootView) {
        mPencilPopupView = rootView.findViewById(R.id.cl_anno_pencil_popup_view);
        mTvPencilInt = rootView.findViewById(R.id.tv_pencil_stroke_intensity);
        mPencilColorGroup = rootView.findViewById(R.id.radio_pencil_color_group);
        mPencilSeek = rootView.findViewById(R.id.seekBar_pencil_stroke_intensity);

        mPencilColorGroup.setOnCheckedChangeListener((group, checkedId) -> {

            selectToolType(mToolType);

            if (checkedId == R.id.rb_pencil_color_black) {
                setColor(FillColor.Black, R.id.rb_pencil_color_black, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_red) {
                setColor(FillColor.Red, R.id.rb_pencil_color_red, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_orange) {
                setColor(FillColor.Orange, R.id.rb_pencil_color_orange, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_pencil_color_yellow, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_green) {
                setColor(FillColor.Green, R.id.rb_pencil_color_green, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_pencil_color_turquoise, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_azure) {
                setColor(FillColor.Azure, R.id.rb_pencil_color_azure, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_blue) {
                setColor(FillColor.Blue, R.id.rb_pencil_color_blue, FROM_PENCIL);
            } else if (checkedId == R.id.rb_pencil_color_purple) {
                setColor(FillColor.Purple, R.id.rb_pencil_color_purple, FROM_PENCIL);
            }
        });

        mPencilSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLineWidth = progress + 1;
                mTvPencilInt.setText(String.valueOf(mLineWidth));
                if(mAnnotation != null) mAnnotation.setLineWidth(mLineWidth);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initBottomTextPopup(View rootView) {
        mTextPopupView = rootView.findViewById(R.id.cl_anno_text_popup_view);
        mTvTextInt = rootView.findViewById(R.id.tv_text_stroke_intensity);
        mTextColorGroup = rootView.findViewById(R.id.radio_text_color_group);
        mTextSeek = rootView.findViewById(R.id.seekBar_text_stroke_intensity);

        mTextColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectToolType(mToolType);

            if (checkedId == R.id.rb_text_color_black) {
                setColor(FillColor.Black, R.id.rb_text_color_black, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_red) {
                setColor(FillColor.Red, R.id.rb_text_color_red, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_orange) {
                setColor(FillColor.Orange, R.id.rb_text_color_orange, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_text_color_yellow, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_green) {
                setColor(FillColor.Green, R.id.rb_text_color_green, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_text_color_turquoise, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_azure) {
                setColor(FillColor.Azure, R.id.rb_text_color_azure, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_blue) {
                setColor(FillColor.Blue, R.id.rb_text_color_blue, FROM_TEXT);
            } else if (checkedId == R.id.rb_text_color_purple) {
                setColor(FillColor.Purple, R.id.rb_text_color_purple, FROM_TEXT);
            }
        });

        mTextSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFontSize = progress + 10;
                mTvTextInt.setText(String.valueOf(mFontSize));
                if(mAnnotation != null) mAnnotation.setFontSize(mFontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initBottomGraphicsPopup(View rootView) {
        mGraphicsPopupView = rootView.findViewById(R.id.cl_anno_graphics_popup_view);

        mGraphicsToolsGroup = rootView.findViewById(R.id.radio_graphics_tools_group);
        mGraphicsToolsGroup.setOnCheckedChangeListener((group,checkedId) -> {

            mGraphicsToolsId = checkedId ;

            if(checkedId == R.id.rb_graphics_line){
                selectToolType(Constants.WBToolType.Line);
            }else if(checkedId == R.id.rb_hollow_circle){
                selectToolType(Constants.WBToolType.Ellipse);
            }else if(checkedId == R.id.rb_hollow_square){
                selectToolType(Constants.WBToolType.Rect);
            }else{
                selectToolType(Constants.WBToolType.Path);
            }
            mGraphicsToolsType = mToolType ;
            setColor(mGraphicsFillColor,mGraphicsColorId,FROM_GRAPHICS);
        });

        mGraphicsColorGroup = rootView.findViewById(R.id.radio_graphics_color_group);
        mGraphicsColorGroup.setOnCheckedChangeListener((group, checkedId) -> {

            selectToolType(mToolType);

            if (checkedId == R.id.rb_graphics_color_black) {
                setColor(FillColor.Black, R.id.rb_graphics_color_black, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_red) {
                setColor(FillColor.Red, R.id.rb_graphics_color_red, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_orange) {
                setColor(FillColor.Orange, R.id.rb_graphics_color_orange, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_yellow) {
                setColor(FillColor.Yellow, R.id.rb_graphics_color_yellow, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_green) {
                setColor(FillColor.Green, R.id.rb_graphics_color_green, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_turquoise) {
                setColor(FillColor.Turquoise, R.id.rb_graphics_color_turquoise, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_azure) {
                setColor(FillColor.Azure, R.id.rb_graphics_color_azure, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_blue) {
                setColor(FillColor.Blue, R.id.rb_graphics_color_blue, FROM_GRAPHICS);
            } else if (checkedId == R.id.rb_graphics_color_purple) {
                setColor(FillColor.Purple, R.id.rb_graphics_color_purple, FROM_GRAPHICS);
            }
        });
    }

    private void showOrHidePencilPopupView() {
        int v = mPencilPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mPencilPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvPencilInt.setText(String.valueOf(mLineWidth));
            mPencilSeek.setProgress(mLineWidth);
            mPencilColorGroup.check(mPencilColorId);

            mToolType = Constants.WBToolType.Path ;
            if(mAnnotation != null){
                mAnnotation.setLineWidth(mLineWidth);
                mAnnotation.setColor(mPencilFillColor.getValue());
                mAnnotation.setToolType(mToolType);
            }
        }
    }

    private void showOrHideTextPopupView() {
        int v = mTextPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mTextPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvTextInt.setText(String.valueOf(mFontSize));
            mTextSeek.setProgress(mFontSize);
            mTextColorGroup.check(mTextColorId);

            mToolType = Constants.WBToolType.Text ;
            if(mAnnotation != null){
                mAnnotation.setFontSize(mFontSize);
                mAnnotation.setColor(mTextFillColor.getValue());
                mAnnotation.setToolType(mToolType);
            }
        }
    }

    private void showOrHideGraphicsPopupView() {
        int v = mGraphicsPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mGraphicsPopupView.setVisibility(v);
        if (v == View.VISIBLE) {
            mToolType = mGraphicsToolsType ;
            if(mAnnotation != null){
                mAnnotation.setLineWidth(10);
                mAnnotation.setColor(mGraphicsFillColor.getValue());
                mAnnotation.setToolType(mToolType);
            }
            mGraphicsColorGroup.check(mGraphicsColorId);
            mGraphicsToolsGroup.check(mGraphicsToolsId);
        }
    }

    private void selectToolType(Constants.WBToolType toolType) {
        mToolType = toolType;
        if(mAnnotation != null) mAnnotation.setToolType(toolType);
    }

    private void setColor(FillColor fillColor, int id, int from){
        if(mAnnotation != null) mAnnotation.setColor(fillColor.getValue());
        switch (from) {
            case FROM_GRAPHICS:
                mGraphicsFillColor = fillColor;
                mGraphicsColorId = id;
                break;
            case FROM_PENCIL:
                mPencilFillColor = fillColor;
                mPencilColorId = id;
                break;
            case FROM_TEXT:
                mTextFillColor = fillColor;
                mTextColorId = id;
                break;
        }
    }

    private void showOrHideOtherPopupView(int from) {
        switch (from) {
            case FROM_GRAPHICS:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                break;
            case FROM_PENCIL:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_TEXT:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_OTHER:
            default:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
        }
    }

}