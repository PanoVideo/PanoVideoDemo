package video.pano.panocall.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PropertyData implements Serializable {
    private static final long serialVersionUID = 4924763385697753953L;

    @SerializedName("id")
    public String hostId ;

    public PropertyData(String hostId) {
        this.hostId = hostId;
    }
}
