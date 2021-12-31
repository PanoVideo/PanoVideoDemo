package video.pano.panocall.fragment;

import static video.pano.panocall.info.Constant.FROM_GRAPHICS;
import static video.pano.panocall.info.Constant.FROM_OTHER;
import static video.pano.panocall.info.Constant.FROM_PENCIL;
import static video.pano.panocall.info.Constant.FROM_TEXT;
import static video.pano.panocall.info.Constant.FROM_TOP;
import static video.pano.panocall.info.Constant.KEY_SHARE_ANNOTATION_START;
import static video.pano.panocall.info.Constant.KEY_USER_ID;
import static video.pano.panocall.info.Constant.KEY_VIDEO_ANNOTATION_START;
import static video.pano.panocall.rtc.MeetingViewFactory.TYPE_SMALL_VIEW;
import static video.pano.panocall.utils.ScreenSensorUtils.DURATION;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavDestination;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.PanoCoursePageView;
import com.pano.rtc.api.RtcWhiteboard;
import com.pano.rtc.api.WBVisionConfig;

import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.adapter.WBItemAdapter;
import video.pano.panocall.info.Config;
import video.pano.panocall.info.FillColor;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnWhiteboardEventListener;
import video.pano.panocall.model.UserInfo;
import video.pano.panocall.rtc.MeetingViewFactory;
import video.pano.panocall.rtc.PanoRtcEngine;
import video.pano.panocall.utils.AnnotationHelper;
import video.pano.panocall.utils.ScreenSensorUtils;
import video.pano.panocall.utils.Utils;


public class WhiteboardFragment extends MeetingFragment implements OnWhiteboardEventListener {

    private static final int RATION_9 = 900;
    private static final int RATION_16 = 1600;

    private RtcWhiteboard mRtcWhiteboard;
    private PanoCoursePageView mCoursePageView;

    private TextView mTvPageName;
    private TextView mTvPageSpeaker;
    private TextView mTextPageNum;
    private TextView mTextPageScale;
    private TextView mTvPencilInt;
    private TextView mTvTextInt;
    private SeekBar mPencilSeek;
    private SeekBar mTextSeek;
    private RadioGroup mPencilColorGroup;
    private RadioGroup mTextColorGroup;
    private RadioGroup mGraphicsColorGroup;
    private RadioGroup mGraphicsToolsGroup;
    private RadioButton mPencilRadio;

    private WBItemAdapter mWbItemAdapter;

    private View mBottomPopupView;
    private View mTopPopupView;
    private View mPencilPopupView;
    private View mGraphicsPopupView;
    private View mTextPopupView;
    private View mAdminView;

    private final Resources mResources = Utils.getApp().getResources();

    private Constants.WBToolType mToolType = Constants.WBToolType.None;
    private Constants.WBFillType mFillType = Constants.WBFillType.None;

    private Constants.WBToolType mGraphicsToolsType = Constants.WBToolType.None;
    private Constants.WBFillType mGraphicsFillType = Constants.WBFillType.None;

    private FillColor mTextFillColor = FillColor.Black;
    private FillColor mPencilFillColor = FillColor.Black;
    private FillColor mGraphicsFillColor = FillColor.Black;

    private int mLineWidth = 10;
    private int mFontSize = 36;

    private int mTextColorId = R.id.rb_text_color_black;
    private int mPencilColorId = R.id.rb_pencil_color_black;
    private int mGraphicsColorId = R.id.rb_graphics_color_black;
    private int mGraphicsToolsId = R.id.rb_graphics_line;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mResources != null && mResources.getConfiguration() != null
                && mResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return inflater.inflate(R.layout.fragment_whiteboard_land, container, false);
        }
        return inflater.inflate(R.layout.fragment_whiteboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        initUserInfo();
        initViews(view);
        init();
        setupUserVideoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mLocalMeetingView != null) getLifecycle().removeObserver(mLocalMeetingView);
        mViewModel.setOnWhiteboardEventListener(null);
    }

    @Override
    protected void initViewModel(){
        super.initViewModel();
        mRtcWhiteboard = mViewModel.getPanoWhiteboard();
        mViewModel.setOnWhiteboardEventListener(this);
    }

    private void initViews(View view) {
        initMeetingView(view);

        initTopToolbarPopup(view);
        initBottomGraphicsPopup(view);
        initBottomPencilPopup(view);
        initBottomTextPopup(view);

        // top tools bar
        initTopToolsBar(view);

        //bottom tools bar
        initBottomToolsBar(view);

        initCoursePageView(view);
    }

    private void init() {

        mTvPencilInt.setText(String.valueOf(mLineWidth));
        mPencilSeek.setProgress(mLineWidth);
        mPencilColorGroup.check(mPencilColorId);

        mRtcWhiteboard.setLineWidth(mLineWidth);

        selectToolType(Constants.WBToolType.Path);
        setColor(mPencilFillColor, mPencilColorId, FROM_PENCIL);

        mPencilRadio.setChecked(true);
    }

    private void initUserInfo() {
        if (UserManager.getIns().getHostId() == UserManager.getIns().getLocalUserId()) {
            mRtcWhiteboard.setRoleType(Constants.WBRoleType.Admin);
        }
    }

    private void initCoursePageView(View view) {
        mCoursePageView = view.findViewById(R.id.pano_course_page_view);

        WBVisionConfig config = new WBVisionConfig();
        config.height = RATION_9;
        config.width = RATION_16;
        config.limited = true;
        mRtcWhiteboard.initVision(config);
        mRtcWhiteboard.close();
        mRtcWhiteboard.open(mCoursePageView);

        if (mResources != null
                && mResources.getConfiguration() != null) {
            resize(mResources.getConfiguration().orientation);
        }
    }

    private void resize(int orientation) {
        float scale = 0L;
        int pageHeight = 0;
        int pageWidth = 0;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pageHeight = Config.sScreenWidth;
            pageWidth = Config.sScreenWidth * RATION_16 / RATION_9;
            scale = (float) pageHeight / RATION_16;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            pageWidth = Config.sScreenWidth;
            pageHeight = Config.sScreenWidth * RATION_9 / RATION_16;
            scale = (float) pageHeight / RATION_16;
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCoursePageView.getLayoutParams();
        params.height = pageHeight;
        params.width = pageWidth;
        mCoursePageView.setLayoutParams(params);

        mRtcWhiteboard.setCurrentScaleFactor(scale);
    }

    private void initTopToolsBar(View rootView) {
        mTextPageNum = rootView.findViewById(R.id.tv_page_num);
        mTextPageScale = rootView.findViewById(R.id.tv_page_percent);

        setPageNum(mRtcWhiteboard.getCurrentPageNumber(), mRtcWhiteboard.getTotalNumberOfPages());
        setPageScale(mRtcWhiteboard.getCurrentScaleFactor());

        rootView.findViewById(R.id.tv_close_wb).setOnClickListener(view1 -> {
            leaveWhiteboard();
        });
        rootView.findViewById(R.id.cl_page_tools).setOnClickListener(view1 -> {
            showOrHideOtherPopupView(FROM_TOP);
            showOrHideTopPopupView();
        });
        rootView.findViewById(R.id.img_page_undo).setOnClickListener(view -> {
            showOrHideOtherPopupView(FROM_OTHER);
            mRtcWhiteboard.undo();
        });
        rootView.findViewById(R.id.img_page_redo).setOnClickListener(view -> {
            showOrHideOtherPopupView(FROM_OTHER);
            mRtcWhiteboard.redo();
        });
    }

    private void initTopToolbarPopup(View rootView) {
        mTopPopupView = rootView.findViewById(R.id.cl_top_toolbar_popup_view);
        mTopPopupView.setOnClickListener(v -> showOrHideOtherPopupView(FROM_OTHER));

        mAdminView = rootView.findViewById(R.id.ll_wb_admin_view);

        mTvPageName = rootView.findViewById(R.id.tv_page_name);
        mTvPageSpeaker = rootView.findViewById(R.id.tv_page_speaker);

        RecyclerView rvWbList = rootView.findViewById(R.id.rv_wb_list);
        rvWbList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWbItemAdapter = new WBItemAdapter(getContext());
        rvWbList.setAdapter(mWbItemAdapter);

        mWbItemAdapter.setOnItemClickListener(wbDocId -> checkWbDoc(wbDocId, true));

        rootView.findViewById(R.id.tv_page_prev).setOnClickListener(view1 -> mRtcWhiteboard.prevPage());
        rootView.findViewById(R.id.tv_page_next).setOnClickListener(view1 -> mRtcWhiteboard.nextPage());
        rootView.findViewById(R.id.tv_page_add).setOnClickListener(view1 -> mRtcWhiteboard.addPage(true));
        rootView.findViewById(R.id.tv_page_remove).setOnClickListener(view1 -> {
            int cpn = mRtcWhiteboard.getCurrentPageNumber();
            mRtcWhiteboard.removePage(cpn);
        });
    }

    private void initBottomToolsBar(View rootView) {
        rootView.findViewById(R.id.img_orientation).setOnClickListener(v -> {
            if (mResources == null) return;

            Configuration configuration = mResources.getConfiguration();
            if (configuration == null) return;

            FragmentActivity activity = getActivity();
            if (activity == null || activity.isFinishing()) return;

            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - ScreenSensorUtils.getIns().getLastTimeMillis() < DURATION) {
                return;
            }

            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                ScreenSensorUtils.getIns().autoOrientation(false,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ScreenSensorUtils.getIns().setLastTimeMillis(currentTimeMillis);
            } else {
//                ScreenSensorUtils.getIns().autoOrientation(false,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ScreenSensorUtils.getIns().setLastTimeMillis(currentTimeMillis);
            }
        });

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
        mBottomPopupView.setOnClickListener(v ->
                showOrHideOtherPopupView(FROM_OTHER)
        );
    }

    private void initBottomPencilPopup(View rootView) {
        mPencilPopupView = rootView.findViewById(R.id.cl_wb_pencil_popup_view);
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
                mRtcWhiteboard.setLineWidth(mLineWidth);
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
        mTextPopupView = rootView.findViewById(R.id.cl_wb_text_popup_view);
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
                mRtcWhiteboard.setFontSize(mFontSize);
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
        mGraphicsPopupView = rootView.findViewById(R.id.cl_wb_graphics_popup_view);

        mGraphicsToolsGroup = rootView.findViewById(R.id.radio_graphics_tools_group);
        mGraphicsToolsGroup.setOnCheckedChangeListener((group, checkedId) -> {

            mGraphicsToolsId = checkedId;

            if (checkedId == R.id.rb_graphics_line) {
                selectToolType(Constants.WBToolType.Line);
            } else if (checkedId == R.id.rb_hollow_circle) {
                selectToolType(Constants.WBToolType.Ellipse);
            } else if (checkedId == R.id.rb_hollow_square) {
                selectToolType(Constants.WBToolType.Rect);
            } else if (checkedId == R.id.rb_solid_circle) {
                selectToolType(Constants.WBToolType.Ellipse, Constants.WBFillType.Color);
            } else if (checkedId == R.id.rb_solid_square) {
                selectToolType(Constants.WBToolType.Rect, Constants.WBFillType.Color);
            }
            mGraphicsFillType = mFillType;
            mGraphicsToolsType = mToolType;
            setColor(mGraphicsFillColor, mGraphicsColorId, FROM_GRAPHICS);
        });

        mGraphicsColorGroup = rootView.findViewById(R.id.radio_graphics_color_group);
        mGraphicsColorGroup.setOnCheckedChangeListener((group, checkedId) -> {

            selectToolType(mToolType, mFillType);

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

    private void showOrHideTopPopupView() {
        int v = mTopPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mTopPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            checkAdminView();
            refreshWbList();
        }
    }

    private void showOrHidePencilPopupView() {
        int v = mPencilPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mPencilPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvPencilInt.setText(String.valueOf(mLineWidth));
            mPencilSeek.setProgress(mLineWidth);
            mPencilColorGroup.check(mPencilColorId);

            mRtcWhiteboard.setLineWidth(mLineWidth);

            selectToolType(Constants.WBToolType.Path);
            setColor(mPencilFillColor, mPencilColorId, FROM_PENCIL);
        }
    }

    private void showOrHideTextPopupView() {
        int v = mTextPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mTextPopupView.setVisibility(v);

        if (v == View.VISIBLE) {
            mTvTextInt.setText(String.valueOf(mFontSize));
            mTextSeek.setProgress(mFontSize);
            mTextColorGroup.check(mTextColorId);

            mRtcWhiteboard.setFontSize(mFontSize);

            selectToolType(Constants.WBToolType.Text);
            setColor(mTextFillColor, mTextColorId, FROM_TEXT);
        }
    }

    private void showOrHideGraphicsPopupView() {
        int v = mGraphicsPopupView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
        mGraphicsPopupView.setVisibility(v);
        if (v == View.VISIBLE) {
            mToolType = mGraphicsToolsType;
            mFillType = mGraphicsFillType;

            mGraphicsColorGroup.check(mGraphicsColorId);
            mGraphicsToolsGroup.check(mGraphicsToolsId);

            mRtcWhiteboard.setLineWidth(10);

            selectToolType(mToolType, mFillType);
            setColor(mGraphicsFillColor, mGraphicsColorId, FROM_GRAPHICS);
        }
    }

    private void selectToolType(Constants.WBToolType toolType) {
        selectToolType(toolType, Constants.WBFillType.None);
    }

    private void selectToolType(Constants.WBToolType toolType, Constants.WBFillType fillType) {
        mFillType = fillType;
        mToolType = toolType;
        mRtcWhiteboard.setToolType(toolType);
        mRtcWhiteboard.setFillType(fillType);
    }

    private void setColor(FillColor fillColor, int id, int from) {
        if (mFillType == Constants.WBFillType.Color) {
            mRtcWhiteboard.setFillColor(fillColor.getValue());
        } else {
            mRtcWhiteboard.setForegroundColor(fillColor.getValue());
        }
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
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                break;
            case FROM_PENCIL:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_TEXT:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.VISIBLE);
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_TOP:
                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
            case FROM_OTHER:
            default:
                if (mTopPopupView != null) mTopPopupView.setVisibility(View.GONE);

                if (mBottomPopupView != null) mBottomPopupView.setVisibility(View.GONE);
                if (mPencilPopupView != null) mPencilPopupView.setVisibility(View.GONE);
                if (mTextPopupView != null) mTextPopupView.setVisibility(View.GONE);
                if (mGraphicsPopupView != null) mGraphicsPopupView.setVisibility(View.GONE);
                break;
        }
    }

    private void leaveWhiteboard() {
        Configuration configuration = mResources.getConfiguration();
        if (configuration != null) {
            FragmentActivity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
        showOrHideOtherPopupView(FROM_OTHER);
        mRtcWhiteboard.close();
        mViewModel.mWhiteboardContentUpdate = false;
        mViewModel.mWhiteboardStart = false;

        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.WhiteboardFragment) {
            mNavController.navigate(R.id.action_WhiteboardFragment_to_SpeakerFragment);
        }
    }

    private SpannableStringBuilder packageText(String start, String end) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        SpannableString key = new SpannableString(start);
        key.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")), 0, key.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        SpannableString value = new SpannableString(end);
        value.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), 0, value.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        ssb.append(key).append("  ").append(value);

        return ssb;
    }

    private void refreshWbList() {
        String currentWBId = mRtcWhiteboard.getCurrentWhiteboardId();
        List<String> wbFiles = mRtcWhiteboard.enumerateFiles();

        if (!TextUtils.isEmpty(currentWBId)) {
            String docName = getString(R.string.title_wb_default_wb);
            if (!currentWBId.contains("default")) {
                int size = wbFiles.size();
                for (int i = 0; i < size; i++) {
                    if (currentWBId.equals(wbFiles.get(i))) {
                        int pageNum = i - 1;
                        docName = getString(R.string.title_wb_default_doc, pageNum <= 0 ? 1 : i);
                    }
                }
            }
            SpannableStringBuilder pageNameBuilder = packageText(getString(R.string.title_wb_current_name), docName);
            mTvPageName.setText(pageNameBuilder);
        }

        mWbItemAdapter.setData(wbFiles, currentWBId);
    }

    private void checkWbDoc(String checkWbId, boolean needSwitch) {
        if (TextUtils.isEmpty(checkWbId)) return;
        if (checkWbId.equals(mRtcWhiteboard.getCurrentFileId())) {
            return;
        }

        String docName = getString(R.string.title_wb_default_wb);
        List<String> wbFiles = mRtcWhiteboard.enumerateFiles();

        if (!checkWbId.contains("default")) {
            int size = wbFiles.size();
            for (int i = 0; i < size; i++) {
                if (checkWbId.equals(wbFiles.get(i))) {
                    int pageNum = i - 1;
                    docName = getString(R.string.title_wb_default_doc, pageNum <= 0 ? 1 : i);
                }
            }
        }
        SpannableStringBuilder pageNameBuilder = packageText(getString(R.string.title_wb_current_name), docName);
        mTvPageName.setText(pageNameBuilder);

        mWbItemAdapter.setCheckWbId(checkWbId);
        if (needSwitch) mRtcWhiteboard.switchDoc(checkWbId);
    }

    // -------------------------- PANO Whiteboard Handler --------------------------

    @Override
    public void onCreateDoc(Constants.QResult result, String fileId) {
        refreshWbList();
    }

    @Override
    public void onSwitchDoc(Constants.QResult result, String fileId) {
        checkWbDoc(fileId, false);
    }

    @Override
    public void onDeleteDoc(Constants.QResult result, String fileId) {
        refreshWbList();
    }

    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        setPageNum(curPage, totalPages);
    }

    @Override
    public void onViewScaleChanged(float scale) {
        setPageScale(scale);
    }

    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        if (newRole == Constants.WBRoleType.Admin) {
            mAdminView.setVisibility(View.VISIBLE);
        } else {
            mAdminView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWhiteboardStop() {
        leaveWhiteboard();
    }

    // -------------------------- CallViewModel.CallEventHandler --------------------------

    private void setPageNum(int curPage, int totalPages) {
        mTextPageNum.setText(curPage + "/" + totalPages);
    }

    private void setPageScale(float scale) {
        int scale100 = (int) (scale * 100);
        mTextPageScale.setText(scale100 + "%");
    }

    /***************************Indication Start*******************************************/
    @Override
    public void onUserJoinIndication(UserInfo user) {
        if(mLocalMeetingView.isEmptyUserInfo()){
            switchViewInfoData(mLocalMeetingView,user);
        }
    }

    @Override
    public void onUserLeaveIndication(UserInfo user) {
        if (mLocalMeetingView.getUserId() == user.userId) {
            setupUserVideoView();
        }
    }

    @Override
    public void onUserVideoStart(UserInfo user) {
        if (mLocalMeetingView.isEmptyUserInfo()
                || (mLocalMeetingView.checkUserInfo(user) && !user.isScreenStarted())) {
            refreshViewInfo(mLocalMeetingView,user);
            subscribeVideo(mLocalMeetingView);
        }
    }

    @Override
    public void onUserVideoStop(UserInfo user) {
        // 取消订阅此用户视频
        if(mLocalMeetingView.checkUserInfo(user) && !user.isScreenStarted()){
            unSubscribeVideo(mLocalMeetingView);
        }
    }

    @Override
    public void onHostUserIdChanged(long hostId) {
        checkAdminView();

    }
    /***************************Indication End*******************************************/
    /***************************Start/Stop LocalVideo*******************************************/

    @Override
    public void stopLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.checkUserInfo(localUser.userId)) {
            unSubscribeVideo(mLocalMeetingView);
        }
    }

    @Override
    public void startLocalVideo() {
        UserInfo localUser = UserManager.getIns().getLocalUser();
        if (mLocalMeetingView.checkUserInfo(localUser.userId)) {
            mLocalMeetingView.switchRtcVisible(true);
            subscribeVideo(mLocalMeetingView);
        }
    }

    /***************************Start/Stop LocalVideo*******************************************/

    @Override
    public void onNetworkQuality(long userId, Constants.QualityRating quality) {
        if(mLocalMeetingView.checkUserInfo(userId)){
            if (quality == Constants.QualityRating.Excellent || quality == Constants.QualityRating.Good) {
                mLocalMeetingView.updateSignalImg(getSignalGoodResourceId(false));
            } else if (quality == Constants.QualityRating.Poor) {
                mLocalMeetingView.updateSignalImg(getSignalPoorResourceId(false));
            } else {
                mLocalMeetingView.updateSignalImg(getSignalLowResourceId(false));
            }
        }
    }

    /******************************Annotation Start*********************************************/
    @Override
    public void onVideoAnnotationStart(long userId, int streamIds) {
        mViewModel.mWhiteboardContentUpdate = false;
        mViewModel.mWhiteboardStart = false;

        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.WhiteboardFragment) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID,userId);
            bundle.putBoolean(KEY_VIDEO_ANNOTATION_START,true);
            mNavController.navigate(R.id.action_WhiteboardFragment_to_SpeakerFragment,bundle);
        }
    }

    @Override
    public void onShareAnnotationStart(long userId) {
        mViewModel.mWhiteboardContentUpdate = false;
        mViewModel.mWhiteboardStart = false;

        NavDestination currentDestination = mNavController.getCurrentDestination();
        if(currentDestination == null){
            return ;
        }
        if (currentDestination.getId() == R.id.WhiteboardFragment) {
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_USER_ID,userId);
            bundle.putBoolean(KEY_SHARE_ANNOTATION_START,true);
            mNavController.navigate(R.id.action_WhiteboardFragment_to_SpeakerFragment,bundle);
        }
    }

    /******************************Annotation End*********************************************/

    private void checkAdminView(){
        String speakerName = "";
        long hostId = UserManager.getIns().getHostId();
        if(UserManager.getIns().getLocalUserId() == hostId){
            UserInfo localUser = UserManager.getIns().getLocalUser();
            speakerName = localUser.getUserName();
            mAdminView.setVisibility(View.VISIBLE);
        }else{
            UserInfo adminUser = UserManager.getIns()
                    .getRemoteUser(UserManager.getIns().getHostId());
            if(adminUser != null){
                speakerName = adminUser.getUserName();
            }
            mAdminView.setVisibility(View.GONE);
        }

        SpannableStringBuilder presenterBuilder = packageText(getString(R.string.title_wb_current_presenter), speakerName);
        mTvPageSpeaker.setText(presenterBuilder);
    }

    @Override
    public void updateUserAudioState(UserInfo userInfo) {
        if (userInfo == null || mLocalMeetingView == null) {
            return;
        }
        if (mLocalMeetingView.checkUserInfo(userInfo)) {
            int largeAudioResource = -1;
            if (userInfo.isPSTNAudioType()) {
                largeAudioResource = userInfo.isAudioMuted() ? getAudioMutePSTNResourceId(false) : getAudioNormalPSTNResourceId(false);
            } else {
                largeAudioResource = userInfo.isAudioMuted() ? getAudioMutedResourceId(false) : getAudioNormalResourceId(false);
            }
            mLocalMeetingView.updateAudioImg(largeAudioResource);
        }
    }

    private void initMeetingView(View view) {
        ViewGroup smallViewContainer = view.findViewById(R.id.dc_small_view);
        mLocalMeetingView = MeetingViewFactory.createMeetingViewInfo(getContext(), TYPE_SMALL_VIEW);
        smallViewContainer.removeAllViews();
        smallViewContainer.addView(mLocalMeetingView.getInfoView());
        mLocalMeetingView.setParentView(smallViewContainer);
        getLifecycle().addObserver(mLocalMeetingView);
    }

    private void setupUserVideoView() {
        boolean flag = false;
        UserInfo userInfo = null;

        if (!UserManager.getIns().isVideoEmpty()) {
            userInfo = UserManager.getIns().getVideoUsers().valueAt(0);
            if (userInfo != null && userInfo.isVideoStarted()) {
                flag = true;
            }
        }

        if (!flag) {
            if (!UserManager.getIns().isRemoteEmpty()) {
                userInfo = UserManager.getIns().getRemoteUsers().valueAt(0);
                if (userInfo != null) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            userInfo = UserManager.getIns().getLocalUser();
        }
        if(userInfo != null){
            refreshViewInfo(mLocalMeetingView,userInfo);
        }else{
            mLocalMeetingView.setInfoViewVisible(false);
        }
        subscribeVideo(mLocalMeetingView);
        updateUserAudioState(userInfo);
    }

}
