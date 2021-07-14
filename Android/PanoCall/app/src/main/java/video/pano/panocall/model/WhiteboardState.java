package video.pano.panocall.model;

import androidx.lifecycle.LiveData;

import com.pano.rtc.api.Constants;

public class WhiteboardState extends LiveData<WhiteboardState> {
    private boolean mIsAvailable = false;
    private boolean mIsContentUpdated = false;
    public Constants.WBToolType mToolType = Constants.WBToolType.Select;

    public void setAvailable(boolean available) {
        mIsAvailable = available;
        postValue(this);
    }
    public void setContentUpdated(boolean updated) {
        mIsContentUpdated = updated;
        postValue(this);
    }

    public boolean isAvailable() {
        return mIsAvailable ;
    }
    public boolean isContentUpdated() {
        return mIsContentUpdated;
    }
}
