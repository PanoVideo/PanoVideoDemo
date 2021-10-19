package video.pano.panocall.info;

import android.util.LongSparseArray;

import video.pano.panocall.model.UserInfo;
import video.pano.panocall.utils.DeviceRatingTest;

public class UserManager {

    private long mHostUserId ;
    private UserInfo mLocalUser;
    private LongSparseArray<UserInfo> mRemoteUsers = new LongSparseArray<>();

    public LongSparseArray<UserInfo> mVideoUsers = new LongSparseArray<>();
    public LongSparseArray<UserInfo> mScreenUsers = new LongSparseArray<>();
    public LongSparseArray<UserInfo> mWhiteboardUsers = new LongSparseArray<>();

    private static UserManager ins;
    private UserManager(){}

    public static UserManager getIns() {

        if (ins == null) {
            synchronized (UserManager.class) {
                ins = new UserManager();
            }
        }
        return ins;
    }

    public void setLocalUser(UserInfo user) {
        mLocalUser = user;
    }
    public UserInfo getLocalUser() {
        return mLocalUser;
    }
    public boolean isMySelf(long userId){
        return mLocalUser != null && mLocalUser.userId == userId ;
    }

    public void addRemoteUser(UserInfo user) {
        mRemoteUsers.put(user.userId, user);
    }
    public UserInfo getRemoteUser(long userId) {
        return mRemoteUsers.get(userId);
    }
    public void removeRemoteUser(long userId) {
        mRemoteUsers.remove(userId);
    }
    public boolean isRemoteEmpty() {
        return mRemoteUsers.size() <= 0;
    }
    public LongSparseArray<UserInfo> getRemoteUsers() {
        return mRemoteUsers;
    }
    public int getRemoteSize(){
        return mRemoteUsers.size() ;
    }

    public void addVideoUser(UserInfo user){
        mVideoUsers.put(user.userId,user);
    }
    public void removeVideoUser(long userId) {
        mVideoUsers.remove(userId);
    }

    public int getVideoSize(){
        return mVideoUsers.size() ;
    }

    public LongSparseArray<UserInfo> getVideoUsers() {
        return mVideoUsers;
    }

    public boolean isVideoEmpty(){
        return mVideoUsers.size() <= 0;
    }


    public void addScreenUser(UserInfo user){
        mScreenUsers.put(user.userId,user);
    }
    public void removeScreenUser(long userId) {
        mScreenUsers.remove(userId);
    }

    public int getScreenSize(){
        return mScreenUsers.size() ;
    }

    public boolean isScreenEmpty(){
        return mScreenUsers.size() <= 0;
    }

    public LongSparseArray<UserInfo> getScreenUsers() {
        return mScreenUsers;
    }

    public void addWhiteboardUser(UserInfo user){
        mWhiteboardUsers.put(user.userId,user);
    }
    public void removeWhiteboardUser(long userId) {
        mWhiteboardUsers.remove(userId);
    }
    public LongSparseArray<UserInfo> getWhiteboardUsers() {
        return mWhiteboardUsers;
    }
    public UserInfo getWhiteboardUser(long userId){
        return mWhiteboardUsers.get(userId);
    }

    public long getHostUserId() {
        return mHostUserId;
    }

    public void setHostUserId(long mHostUserId) {
        this.mHostUserId = mHostUserId;
    }

    public void clear() {
        mLocalUser = null;
        mRemoteUsers.clear();
        mVideoUsers.clear();
        mScreenUsers.clear();
        mWhiteboardUsers.clear();
        mHostUserId = 0L ;
    }
}
