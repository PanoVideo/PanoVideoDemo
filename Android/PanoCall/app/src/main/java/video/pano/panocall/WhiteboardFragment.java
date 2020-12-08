package video.pano.panocall;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.fragment.NavHostFragment;

import com.pano.rtc.api.Constants;
import com.pano.rtc.api.RtcWbView;
import com.pano.rtc.api.RtcWhiteboard;


public class WhiteboardFragment extends CallFragment implements PanoWhiteboardHandler {

    private ImageButton mImageButtonSelected;
    private int mImageResourceNormal;
    private RtcWhiteboard mRtcWhiteboard;
    private TextView mTextPageNum;
    private TextView mTextPageScale;
    private ConstraintLayout mClGraph;
    private ImageButton mImageGraph;
    private ConstraintLayout mClColor;
    private ImageButton mImageColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_whiteboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRtcWhiteboard = mViewModel.rtcEngine().getWhiteboard();
        PanoApplication app = (PanoApplication) getActivity().getApplication();
        app.registerWhiteboardHandler(this);
        mViewModel.mWhiteboardState.setContentUpdated(false);

        // toolbox bar
        view.findViewById(R.id.img_wb_toolbox_back).setOnClickListener(view1 -> {
            NavHostFragment.findNavController(WhiteboardFragment.this).navigateUp();
        });
        ImageButton imgSelect = view.findViewById(R.id.img_wb_toolbox_select);
        imgSelect.setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Select);
        });
        ImageButton imgPen = view.findViewById(R.id.img_wb_toolbox_pen);
        imgPen.setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Path);
        });
        view.findViewById(R.id.img_wb_toolbox_text).setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Text);
        });
        mImageGraph = view.findViewById(R.id.img_wb_toolbox_graph);
        mImageGraph.setOnClickListener(view1 -> {
            //selectToolType(Constants.WBToolType.Rect);
            int v = mClGraph.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            mClGraph.setVisibility(v);
            mClColor.setVisibility(View.GONE);
        });
        ImageButton imgText = view.findViewById(R.id.img_wb_toolbox_text);
        imgText.setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Text);
        });
        mImageColor = view.findViewById(R.id.img_wb_toolbox_color);
        mImageColor.setOnClickListener(view1 -> {
            int v = mClColor.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            mClColor.setVisibility(v);
            mClGraph.setVisibility(View.GONE);
        });
        ImageButton imgEraser = view.findViewById(R.id.img_wb_toolbox_eraser);
        imgEraser.setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Eraser);
        });
        view.findViewById(R.id.img_wb_toolbox_undo).setOnClickListener(view1 -> mRtcWhiteboard.undo());
        view.findViewById(R.id.img_wb_toolbox_redo).setOnClickListener(view1 -> mRtcWhiteboard.redo());

        // graph toolbox
        mClGraph = view.findViewById(R.id.cl_wb_toolbox_graph);
        view.findViewById(R.id.img_wb_toolbox_rect).setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Rect);
        });
        view.findViewById(R.id.img_wb_toolbox_ellipse).setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Ellipse);
        });
        view.findViewById(R.id.img_wb_toolbox_line).setOnClickListener(view1 -> {
            selectToolType(Constants.WBToolType.Line);
        });

        // color toolbox
        mClColor = view.findViewById(R.id.cl_wb_toolbox_color);
        view.findViewById(R.id.img_wb_toolbox_color_red).setOnClickListener(view1 -> {
            onColorChanged(255, 0, 0, R.drawable.btn_wb_tb_color_red);
        });
        view.findViewById(R.id.img_wb_toolbox_color_green).setOnClickListener(view1 -> {
            onColorChanged(0, 255, 0, R.drawable.btn_wb_tb_color_green);
        });
        view.findViewById(R.id.img_wb_toolbox_color_blue).setOnClickListener(view1 -> {
            onColorChanged(0, 0, 255, R.drawable.btn_wb_tb_color_blue);
        });
        view.findViewById(R.id.img_wb_toolbox_color_orange).setOnClickListener(view1 -> {
            onColorChanged(255, 165,0, R.drawable.btn_wb_tb_color_orange);
        });
        view.findViewById(R.id.img_wb_toolbox_color_other).setOnClickListener(view1 -> {
            onColorChanged(128, 38,84, R.drawable.btn_wb_tb_color_other);
        });
        view.findViewById(R.id.img_wb_toolbox_color_gray).setOnClickListener(view1 -> {
            onColorChanged(192, 192,192, R.drawable.btn_wb_tb_color_gray);
        });
        view.findViewById(R.id.img_wb_toolbox_color_black).setOnClickListener(view1 -> {
            onColorChanged(0, 0,0, R.drawable.btn_wb_tb_color_black);
        });


        selectToolType(mViewModel.mWhiteboardState.mToolType);

        // page bar
        mTextPageNum = view.findViewById(R.id.tv_wb_page_number);
        int curPageNo = mRtcWhiteboard.getCurrentPageNumber();
        int totalPages = mRtcWhiteboard.getTotalNumberOfPages();
        mTextPageNum.setText("" + curPageNo + "/" + totalPages);
        mTextPageScale = view.findViewById(R.id.tv_wb_page_scale);
        float scaleFactor = mRtcWhiteboard.getCurrentScaleFactor();
        int scale100 = (int)(scaleFactor * 100);
        mTextPageScale.setText("" + scale100 + "%");
        view.findViewById(R.id.img_wb_page_prev).setOnClickListener(view1 -> mRtcWhiteboard.prevPage());
        view.findViewById(R.id.img_wb_page_next).setOnClickListener(view1 -> mRtcWhiteboard.nextPage());
        view.findViewById(R.id.img_wb_page_add).setOnClickListener(view1 -> mRtcWhiteboard.addPage(true));
        view.findViewById(R.id.img_wb_page_remove).setOnClickListener(view1 -> {
            int cpn = mRtcWhiteboard.getCurrentPageNumber();
            mRtcWhiteboard.removePage(cpn);
        });

        mRtcWhiteboard.setLineWidth(10);
        mRtcWhiteboard.setFontSize(36);
        onColorChanged(128, 38,84, R.drawable.btn_wb_tb_color_other);
        if (mViewModel.mIsHost) {
            mRtcWhiteboard.setRoleType(Constants.WBRoleType.Admin);
        }

        RtcWbView wbView = view.findViewById(R.id.rtc_whiteboard_view);
        wbView.setZOrderMediaOverlay(true);
        mRtcWhiteboard.open(wbView);
    }

    @Override
    public void onDestroyView() {
        PanoApplication app = (PanoApplication) getActivity().getApplication();
        app.removeWhiteboardHandler(this);
        mViewModel.mWhiteboardState.setContentUpdated(false);
        super.onDestroyView();
    }

    private void selectToolType(Constants.WBToolType toolType) {
        if (mImageButtonSelected != null) {
            if (toolType == mViewModel.mWhiteboardState.mToolType) {
                return;
            }
            mImageButtonSelected.setImageResource(mImageResourceNormal);
        }
        ImageButton img_btn = null;
        int res_id_normal = R.mipmap.btn_wb_tb_select_normal;
        int res_id_selected = R.mipmap.btn_wb_tb_select_selected;
        boolean is_graph = false;
        switch (toolType) {
            case Select:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_select);
                res_id_normal = R.mipmap.btn_wb_tb_select_normal;
                res_id_selected = R.mipmap.btn_wb_tb_select_selected;
                break;
            case Path:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_pen);
                res_id_normal = R.mipmap.btn_wb_tb_pen_normal;
                res_id_selected = R.mipmap.btn_wb_tb_pen_selected;
                break;
            case Line:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_line);
                res_id_normal = R.mipmap.btn_wb_tb_line_normal;
                res_id_selected = R.mipmap.btn_wb_tb_line_selected;
                is_graph = true;
                break;
            case Rect:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_rect);
                res_id_normal = R.mipmap.btn_wb_tb_rect_normal;
                res_id_selected = R.mipmap.btn_wb_tb_rect_selected;
                is_graph = true;
                break;
            case Ellipse:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_ellipse);
                res_id_normal = R.mipmap.btn_wb_tb_ellipse_normal;
                res_id_selected = R.mipmap.btn_wb_tb_ellipse_selected;
                is_graph = true;
                break;
            case Text:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_text);
                res_id_normal = R.mipmap.btn_wb_tb_text_normal;
                res_id_selected = R.mipmap.btn_wb_tb_text_selected;
                break;
            case Eraser:
                img_btn = getView().findViewById(R.id.img_wb_toolbox_eraser);
                res_id_normal = R.mipmap.btn_wb_tb_eraser_normal;
                res_id_selected = R.mipmap.btn_wb_tb_eraser_selected;
                break;
            default:
                break;
        }
        if (img_btn != null) {
            img_btn.setImageResource(res_id_selected);
            mViewModel.mWhiteboardState.mToolType = toolType;
            mImageButtonSelected = img_btn;
            mImageResourceNormal = res_id_normal;
        }
        mClGraph.setVisibility(View.GONE);
        mClColor.setVisibility(View.GONE);
        if (is_graph) {
            mImageGraph.setImageResource(R.mipmap.btn_wb_tb_graph_selected);
        } else {
            mImageGraph.setImageResource(R.mipmap.btn_wb_tb_graph_normal);
        }
        mRtcWhiteboard.setToolType(toolType);
    }

    private void onColorChanged(int r, int g, int b, int color_id) {
        mRtcWhiteboard.setForegroundColor(r/255.0f, g/255.0f, b/255.0f, 1);
        mClColor.setVisibility(View.GONE);
        mImageColor.setImageResource(color_id);
    }



    // -------------------------- PANO Whiteboard Handler --------------------------
    @Override
    public void onPageNumberChanged(int curPage, int totalPages) {
        mTextPageNum.setText("" + curPage + "/" + totalPages);
    }
    @Override
    public void onImageStateChanged(String url, Constants.WBImageState state) {

    }
    @Override
    public void onViewScaleChanged(float scale) {
        int scale100 = (int)(scale * 100);
        mTextPageScale.setText("" + scale100 + "%");
    }
    @Override
    public void onRoleTypeChanged(Constants.WBRoleType newRole) {
        mViewModel.mWhiteboardState.setRoleType(newRole);
    }
    @Override
    public void onContentUpdated() {

    }
    @Override
    public void onMessage(long userId, byte[] bytes) {

    }

    // -------------------------- CallViewModel.CallEventHandler --------------------------
    @Override
    public void onWhiteboardUnavailable() {
        Toast.makeText(getActivity(), R.string.msg_whiteboard_unavailable, Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(WhiteboardFragment.this).navigateUp();
    }


    // video view
    @Override
    void setupUserViewArray() {
        // 初始化宫格视图数组，目前支持四宫格
        initUserViewArray(4);

        View view = getView();

        // 配置视图1参数, 此视图一般用于显示本地用户视频
        mUserViewArray[0].initView(0,
                view.findViewById(R.id.small_view_1),
                view.findViewById(R.id.tv_small_view_1_user),
                view.findViewById(R.id.img_small_view_1_audio),
                null,
                mViewModel.mScalingType);

        // 配置视图2参数
        mUserViewArray[1].initView(1,
                view.findViewById(R.id.small_view_2),
                view.findViewById(R.id.tv_small_view_2_user),
                view.findViewById(R.id.img_small_view_2_audio),
                null,
                mViewModel.mScalingType);

        // 配置视图3参数
        mUserViewArray[2].initView(2,
                view.findViewById(R.id.small_view_3),
                view.findViewById(R.id.tv_small_view_3_user),
                view.findViewById(R.id.img_small_view_3_audio),
                null,
                mViewModel.mScalingType);

        // 配置视图4参数
        mUserViewArray[3].initView(3,
                view.findViewById(R.id.small_view_4),
                view.findViewById(R.id.tv_small_view_4_user),
                view.findViewById(R.id.img_small_view_4_audio),
                null,
                mViewModel.mScalingType);

        initUserVideoView(0, false);
    }

    @Override
    Constants.VideoProfileType getProfileForVideoView(int index, Constants.VideoProfileType maxProfile) {
        return Constants.VideoProfileType.Lowest;
    }

}
