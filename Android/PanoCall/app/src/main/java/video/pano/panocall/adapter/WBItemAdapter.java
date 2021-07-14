package video.pano.panocall.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.listener.OnWbItemClicklistener;

public class WBItemAdapter extends RecyclerView.Adapter<WBItemViewHolder> {

    private Context mContext;
    private List<String> mWBItems = new ArrayList<>();
    private OnWbItemClicklistener mOnItemClickListener;
    private String mCurrentWbId ;

    public void setOnItemClickListener(OnWbItemClicklistener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public WBItemAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> wbItems,String checkWbId){
        mWBItems.clear();
        mWBItems.addAll(wbItems);
        mCurrentWbId = checkWbId ;
        notifyDataSetChanged();
    }

    public void setCheckWbId(String checkWbId){
        mCurrentWbId = checkWbId ;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WBItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(mContext,R.layout.whiteboard_item_layout,null);
        return new WBItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WBItemViewHolder holder, int position) {
        String wbDocId = mWBItems.get(position);

        if(!TextUtils.isEmpty(wbDocId)){
            String docName = mContext.getString(R.string.title_wb_default_wb);
            if(!wbDocId.contains("default")){
                int pageNum = position - 1;
                docName = mContext.getString(R.string.title_wb_default_doc,pageNum <=0 ? 1 : position);
            }
            holder.wbItem.setText(docName);
        }
        holder.wbItem.setSelected(wbDocId.equals(mCurrentWbId));

        holder.itemView.setOnClickListener(v->{
            if(mOnItemClickListener != null){
                mOnItemClickListener.onItemClick(wbDocId);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mWBItems == null ? 0 : mWBItems.size();
    }

}

class WBItemViewHolder extends RecyclerView.ViewHolder {

    public final TextView wbItem;

    public WBItemViewHolder(@NonNull View itemView) {
        super(itemView);
        wbItem = itemView.findViewById(R.id.tv_page_item_name);
    }
}
