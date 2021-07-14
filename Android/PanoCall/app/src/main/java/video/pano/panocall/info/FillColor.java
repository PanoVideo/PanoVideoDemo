package video.pano.panocall.info;

import java.util.HashMap;
import java.util.Map;

public enum FillColor {

    Black(0xFF000000),
    Red(0xFFE02C0B),
    Orange(0xFFD46B08),
    Yellow(0xFFE7BD0E),
    Green(0xFF54D612),
    Turquoise(0xFF90DE0F),
    Azure(0xFF0C8EE5),
    Blue(0xFF1631D3),
    Purple(0xFF5613D8);

    private final int value;

    FillColor(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    private final static Map<Integer, FillColor> map = new HashMap<>();

    static {
        for (FillColor r : FillColor.values()) {
            map.put(r.value, r);
        }
    }

    public static FillColor valueOf(int result) {
        FillColor r = map.get(result);
        if (r == null) {
            return FillColor.Black;
        }
        return r;
    }

}
