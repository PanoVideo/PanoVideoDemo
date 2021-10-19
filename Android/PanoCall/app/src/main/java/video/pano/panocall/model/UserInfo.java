package video.pano.panocall.model;

import com.pano.rtc.api.Constants;

public class UserInfo {

    public long userId = 0;
    public String userName = "";

    public boolean audioStarted = false;
    public boolean videoMuted = false;
    public boolean screenMuted = false;

    public boolean audioMuted = true;
    public boolean videoStarted = false;

    public boolean screenStarted = false;

    public Constants.VideoProfileType maxProfile = Constants.VideoProfileType.HD720P;

    public UserInfo(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public void setAudioMuted(boolean muted) {
        audioMuted = muted;
    }

    public void setVideoStarted(boolean started) {
        videoStarted = started;
    }

    public void setScreenStarted(boolean started) {
        screenStarted = started;
    }

    public boolean isAudioMuted() {
        return audioMuted;
    }

    public boolean isVideoStarted() {
        return videoStarted;
    }

    public boolean isScreenStarted() {
        return screenStarted;
    }

    public String getUserName() {
        return userName;
    }

    public void setScreenMuted(boolean muted) {
        screenMuted = muted;
    }

    public void setVideoMuted(boolean muted) {
        videoMuted = muted;
    }

    public void setAudioStared(boolean started) {
        audioStarted = started;
    }

    public boolean isAudioStarted() {
        return audioStarted;
    }

    public boolean isVideoMuted() {
        return videoMuted;
    }

    public boolean isScreenMuted() {
        return screenMuted;
    }


}
