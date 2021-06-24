import java.util.ArrayList;
import org.json.simple.JSONObject;

class Storage {

    private ArrayList<JSONObject> sendOff;
    private ArrayList<String> unavailable;
    private int sent;

    public Storage() {
        sendOff = new ArrayList<>();
        unavailable = new ArrayList<>();
    }

    public void putUnavailable(String s) {
        unavailable.add(s);
    }

    public void putSendOff(JSONObject j) {
        sendOff.add(j);
    }

    public ArrayList<String> getUnavailable() {
        return unavailable;
    }

    public ArrayList<JSONObject> getSendOff() {
        return sendOff;
    }

    public void clearSendOff() {
        if (!(sendOff.isEmpty())) {
            int index = sendOff.size() - 1;
            sendOff.remove(index);
        }
    }

    public void clearSendOffAll() {
        sendOff.clear();
    }

    public void removeFirst() {
        sendOff.remove(0);
    }

    public void clearUnavailable() {
        unavailable.clear();
    }

    public void putSentVariable(int s) {
        sent = s;
    }

    public int getSentVariable() {
        return sent;
    }
}
