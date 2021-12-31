package video.pano.panocall.model;

import java.util.Objects;

public class UserInfo {

    public long userId = 0;
    public String userName = "";

    public boolean audioMuted = true;
    public boolean videoStarted = false;
    public boolean screenStarted = false;
    public boolean pstnAudioType = false;
    public boolean mirror;

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

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
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

    public boolean isPSTNAudioType() {
        return pstnAudioType;
    }

    public void setPSTNAudioType(boolean pstnAudioType) {
        this.pstnAudioType = pstnAudioType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return userId == userInfo.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
