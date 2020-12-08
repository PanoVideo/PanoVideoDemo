package video.pano.panocall;

import android.util.LongSparseArray;

import androidx.lifecycle.LiveData;

import com.pano.rtc.api.Constants;

public class UserManager {
    static class ObservableState extends LiveData<ObservableState> {
        boolean state = false;

        void setState(boolean st) {
            state = st;
            postValue(this);
        }

        boolean getState() { return state; }
    }

    static class UserInfo {
        long userId = 0;
        String userName = "";
        ObservableState audioStarted = new ObservableState();
        ObservableState videoStarted = new ObservableState();
        ObservableState screenStarted = new ObservableState();
        ObservableState audioMuted = new ObservableState();
        ObservableState videoMuted = new ObservableState();
        ObservableState screenMuted = new ObservableState();
        Constants.VideoProfileType maxProfile = Constants.VideoProfileType.HD720P;

        UserInfo(long userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }

        void setUserInfo(long userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }

        void updateAudioState(boolean started) {
            audioStarted.setState(started);
        }
        void updateAudioMuteState(boolean muted) {
            audioMuted.setState(muted);
        }
        void updateVideoState(boolean started) {
            videoStarted.setState(started);
        }
        void updateVideoMuteState(boolean muted) {
            videoMuted.setState(muted);
        }
        void updateScreenState(boolean started) {
            screenStarted.setState(started);
        }
        void updateScreenMuteState(boolean muted) {
            screenMuted.setState(muted);
        }

        boolean isAudioStarted() {
            return audioStarted.getState();
        }
        boolean isAudioMuted() {
            return audioMuted.getState();
        }
        boolean isVideoStarted() {
            return videoStarted.getState();
        }
        boolean isVideoMuted() {
            return videoMuted.getState();
        }
        boolean isScreenStarted() {
            return screenStarted.getState();
        }
        boolean isScreenMuted() {
            return screenMuted.getState();
        }
    }
    private UserInfo mLocalUser;
    private LongSparseArray<UserInfo> mRemoteUsers = new LongSparseArray<>();

    void setLocalUser(UserInfo user) {
        mLocalUser = user;
    }
    UserInfo getLocalUser() {
        return mLocalUser;
    }

    void addRemoteUser(UserInfo user) {
        mRemoteUsers.put(user.userId, user);
    }
    UserInfo getRemoteUser(long userId) {
        return mRemoteUsers.get(userId);
    }
    void removeRemoteUser(long userId) {
        mRemoteUsers.remove(userId);
    }
    boolean isEmpty() {
        return mRemoteUsers.size() <= 0;
    }
    LongSparseArray<UserInfo> getRemoteUsers() {
        return mRemoteUsers;
    }
    void clear() {
        mLocalUser = null;
        mRemoteUsers.clear();
    }
}
