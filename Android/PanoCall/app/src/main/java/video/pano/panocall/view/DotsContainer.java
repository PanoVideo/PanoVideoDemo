package video.pano.panocall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import video.pano.panocall.R;
import video.pano.panocall.info.Config;

public class DotsContainer extends RadioGroup {

    private Context mContext;

    public DotsContainer(Context context) {
        this(context,null);
    }

    public DotsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context ;
    }


    public void showDots(int count){
        removeAllViews();
        for(int i = 0 ; i < count ; i ++){
            RadioButton dotItem = (RadioButton) LayoutInflater.from(mContext).inflate(R.layout.layout_dot_item,this,false);
            dotItem.setId(Config.sNextGeneratedId.incrementAndGet());
            addView(dotItem);
        }
    }

    public void checkItem(int pos){
        if(pos >= getChildCount()){
            return ;
        }
        check(getChildAt(pos).getId());
    }

}
