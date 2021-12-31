package video.pano.panocall.info;

import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import video.pano.panocall.model.UserInfo;

public class UserManager {

    private long mLocalUserId ;
    private long mHostId = 0L;
    private UserInfo mLocalUser;

    private LongSparseArray<UserInfo> mRemoteUsers = new LongSparseArray<>();
    private LongSparseArray<UserInfo> mVideoUsers = new LongSparseArray<>();
    private LongSparseArray<UserInfo> mWhiteboardUsers = new LongSparseArray<>();
    private List<UserInfo> mScreenUsers = new ArrayList<>();

    private static UserManager ins;

    private UserManager() {
    }

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
        if(user != null){
            mLocalUserId = user.userId;
        }
    }

    public UserInfo getLocalUser() {
        return mLocalUser;
    }

    public boolean isMySelf(long userId) {
        return mLocalUser != null && mLocalUser.userId == userId;
    }

    public long getLocalUserId(){
        return mLocalUserId ;
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

    public int getRemoteSize() {
        return mRemoteUsers.size();
    }

    public void addVideoUser(UserInfo user) {
        mVideoUsers.put(user.userId, user);
    }

    public void removeVideoUser(long userId) {
        mVideoUsers.remove(userId);
    }

    public int getVideoSize() {
        return mVideoUsers.size();
    }

    public LongSparseArray<UserInfo> getVideoUsers() {
        return mVideoUsers;
    }

    public boolean isVideoEmpty() {
        return mVideoUsers.size() <= 0;
    }


    public void addScreenUser(UserInfo user) {
        mScreenUsers.add(user);
    }

    public void removeScreenUser(UserInfo user) {
        mScreenUsers.remove(user);
    }

    public int getScreenSize() {
        return mScreenUsers.size();
    }

    public boolean isScreenEmpty() {
        return mScreenUsers.size() <= 0;
    }

    public List<UserInfo> getScreenUsers() {
        return mScreenUsers;
    }

    public UserInfo getScreenUser(long userId) {
        for (UserInfo userInfo : mScreenUsers) {
            if (userInfo.userId == userId) {
                return userInfo;
            }
        }
        return null;
    }

    public void addWhiteboardUser(UserInfo user) {
        mWhiteboardUsers.put(user.userId, user);
    }

    public void removeWhiteboardUser(long userId) {
        mWhiteboardUsers.remove(userId);
    }

    public LongSparseArray<UserInfo> getWhiteboardUsers() {
        return mWhiteboardUsers;
    }

    public UserInfo getWhiteboardUser(long userId) {
        return mWhiteboardUsers.get(userId);
    }

    public long getHostId() {
        return mHostId;
    }

    public void setHostId(long mHostId) {
        this.mHostId = mHostId;
    }

    public void clear() {
        mLocalUser = null;
        mRemoteUsers.clear();
        mVideoUsers.clear();
        mScreenUsers.clear();
        mWhiteboardUsers.clear();
        mHostId = 0L;
    }
}
