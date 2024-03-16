package data.scripts;

import com.fs.starfarer.api.Global;

public class US_txt {
    private static final String category = "unknownSkies";

    public static String txt(String id) {
        return Global.getSettings().getString(category, id);
    }
}