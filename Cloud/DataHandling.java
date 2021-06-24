import java.util.*;

public class DataHandling {

    private Map map;
    private String command = "";
    private Map answer = new LinkedHashMap<>();
    public static final double OPT_TEMP = 22;
    public static final double OPT_MOIST = 3;
    public static final double OPT_HUMID = 40;
    public static final double OPT_BRGT = 0.3;

    public DataHandling(Map map) {
        this.map = map;
    }

    public Map handleData() {
        String type = map.get("type").toString();
        switch (type) {
            case "TEMPERATURE":
                answer = compareData(OPT_TEMP);
                break;
            case "MOISTURE":
                answer = compareData(OPT_MOIST);
                break;
            case "HUMIDITY":
                answer = compareData(OPT_HUMID);
                break;
            case "BRIGHTNESS":
                answer = compareData(OPT_BRGT);
                break;
            default:
                break;
        }
        return answer;
    }

    private Map compareData(double opt) {
        String measuredString = map.get("value").toString();
        double measured = Double.parseDouble(measuredString);
        double gap = opt - measured;
        if (gap > 0) {
            command = "raise";
        } else if (gap < 0) {
            command = "lower";
        } else {
            command = "ok";
        }
        answer.put("command", command);
        answer.put("gap", gap);
        return answer;
    }

}
