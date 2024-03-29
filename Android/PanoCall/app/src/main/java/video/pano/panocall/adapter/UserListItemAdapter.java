package video.pano.panocall.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.R;
import video.pano.panocall.info.UserManager;
import video.pano.panocall.listener.OnUserListItemClickListener;
import video.pano.panocall.model.UserInfo;

public class UserListItemAdapter extends RecyclerView.Adapter<UserListItemViewHolder> {

    private Context mContext ;
    private List<UserInfo> mUserInfos = new ArrayList<>();
    private OnUserListItemClickListener mItemClickListener;
    private long mHostUserId;

    public UserListItemAdapter(Context context){
        mContext = context ;
    }

    public void refreshData(){
        mUserInfos.clear();
        mUserInfos.add(UserManager.getIns().getLocalUser());
        LongSparseArray<UserInfo> remoteUsers = UserManager.getIns().getRemoteUsers();
        int size = remoteUsers.size();
        if(size > 0){
            for(int i = 0 ; i < size ; i ++){
                mUserInfos.add(remoteUsers.valueAt(i));
            }
        }
        mHostUserId = UserManager.getIns().getHostId();
        notifyDataSetChanged();
    }

    public void refreshItemData(UserInfo user){
        int pos = mUserInfos.indexOf(user);
        notifyItemChanged(pos);
    }

    public void setHostUserId(long hostUserId){
        mHostUserId = hostUserId ;
        notifyDataSetChanged();
    }

    public void setOnItemClick(OnUserListItemClickListener listener){
        mItemClickListener = listener ;
    }

    @NonNull
    @Override
    public UserListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.userlist_item_layout,parent,false);
        return new UserListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListItemViewHolder holder, int position) {
        UserInfo userInfo = mUserInfos.get(position);
        holder.videoImg.setImageResource(userInfo.isVideoStarted() ?
                R.drawable.svg_icon_user_list_video_normal : R.drawable.svg_icon_user_list_video_closed);
        if(userInfo.isPSTNAudioType()){
            holder.audioImg.setImageResource(userInfo.isAudioMuted() ? R.drawable.svg_icon_user_list_audio_pstn_mute : R.drawable.svg_icon_user_list_audio_pstn_normal);
        }else{
            holder.audioImg.setImageResource(userInfo.isAudioMuted() ? R.drawable.svg_icon_user_list_audio_mute : R.drawable.svg_icon_user_list_audio_normal);
        }
        if(mHostUserId == userInfo.userId){
            holder.hostText.setVisibility(View.VISIBLE);
        }else{
            holder.hostText.setVisibility(View.GONE);
        }
        String userName = userInfo.userName;
        if(!TextUtils.isEmpty(userName)){
            holder.userNameText.setText(userName);
            char initials = userName.charAt(0);
            holder.initialsText.setText(String.valueOf(initials));
        }
        holder.itemView.setOnClickListener(v -> {
            if(mItemClickListener != null) mItemClickListener.onItemClick(userInfo);
        });
    }

    @Override
    public int getItemCount() {
        return  mUserInfos == null ? 0 : mUserInfos.size();
    }


}

class UserListItemViewHolder extends RecyclerView.ViewHolder {

    public final TextView initialsText;
    public final TextView userNameText;
    public final TextView hostText;
    public final ImageView audioImg;
    public final ImageView videoImg;

    public UserListItemViewHolder(@NonNull View itemView) {
        super(itemView);
        initialsText = itemView.findViewById(R.id.tv_initials);
        userNameText = itemView.findViewById(R.id.tv_user_name);
        audioImg = itemView.findViewById(R.id.img_audio);
        videoImg = itemView.findViewById(R.id.img_video);
        hostText = itemView.findViewById(R.id.tv_host);

    }
}

