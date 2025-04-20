package qmf.poc.service.log;

public class LogHelper {
    public static String ellipse(String veryLongString) {
        if (veryLongString.length() > 203) {
            return veryLongString.substring(0, 200) + "...";
        }
        return veryLongString;
    }

    public static String ellipse(Object veryLongString) {
        return ellipse(veryLongString.toString());
    }
}
