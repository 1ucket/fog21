import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
import org.json.simple.JSONObject;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ClientHandler extends Thread {

    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    private Storage storage;
    private List<JSONObject> outBuffer = new ArrayList<JSONObject>();
    private HashMap<Socket, JSONObject> cloud = new HashMap<Socket, JSONObject>();
    private ArrayList<JSONObject> sendOff = new ArrayList<JSONObject>();
    private ArrayList<String> unavailable = new ArrayList<String>();
    private String ip;
    private int sent = 0;
    private boolean firstCheck = true;

    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, Storage storage) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.storage = storage;
        ip = this.s.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {

                if (firstCheck) { // only needs to be checked on first loop
                    sendPreviousData();
                }
                firstCheck = false;

                // receive data from client
                received = dis.readUTF();

                // turn received data into String
                byte[] utf8Bytes = received.getBytes("UTF8");
                String str = new String(utf8Bytes, "UTF8");

                if (str.equals("OK")) { // get confirmation that JSON object was delivered to edge
                    sent++;
                } else { // received data is not confirmation message but new data
                    System.out.println(received);

                    // start processing data
                    JSONObject answerJSON = new JSONObject();

                    // from: https://www.tutorialspoint.com/json_simple/json_simple_container_factory.htm
                    JSONParser parser = new JSONParser();
                    ContainerFactory containerFactory = new ContainerFactory() {
                        @Override
                        public Map createObjectContainer() {
                            return new LinkedHashMap<>();
                        }

                        @Override
                        public List creatArrayContainer() {
                            return new LinkedList<>();
                        }
                    };
                    try {
                        Map map = (Map) parser.parse(received, containerFactory);

                        // compare received data and check if edge has to take action
                        DataHandling dh = new DataHandling(map);
                        Map answer = dh.handleData();
                        JSONObject answ = createJSON(map, answer, answerJSON);

                        // send data back to edge as String
                        answ.put("prev", false);
                        String time = Long.toString(System.currentTimeMillis());
                        answ.put("timestamp", time);
                        //System.out.println(answ);

                        storage.putSendOff(answ); // save new JSON in storage

                        dos.writeUTF(answ.toString());
                        //System.out.println(storage.getSendOff().size());

                    } catch (ParseException pe) {
                        System.out.println("position: " + pe.getPosition());
                        System.out.println(pe);
                    }
                }
            } catch (IOException e) {
                handleException();
                try {
                    // closing resources
                    System.out.println("Objects ready to be sent off to client:" + storage.getSendOff().size());
                    this.dis.close();
                    this.dos.close();
                    this.s.close();
                    System.out.println("Thread closed");
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private JSONObject createJSON(Map map, Map answer, JSONObject answerJSON) {
        // fill JSON with new data and some old data to send back to edge
        answerJSON.put("ID", map.get("ID").toString());
        answerJSON.put("EDGE_ID", map.get("EDGE_ID").toString());
        answerJSON.put("type", map.get("type").toString());
        answerJSON.put("value", map.get("value").toString());
        answerJSON.put("command", answer.get("command").toString());
        answerJSON.put("gap", answer.get("gap").toString());
        return answerJSON;
    }

    private void sendPreviousData() {
        // make sure there is no data left that needs to be sent out first before receiving new one
        try {
            unavailable = storage.getUnavailable();
            // check if edge was previously connected but lost connection
            if (unavailable.contains(ip)) {
                sendOff = storage.getSendOff();
                // check that there are JSON objects in the storage from previous session
                if (!(sendOff.isEmpty())) {
                    sent = storage.getSentVariable();
                    // all objects created minus the objects that were successfully sent off = objects that previously couldn't be sent to edge
                    for (int i = 0; i < sent; i++) {
                        storage.removeFirst(); // the last objects created are the ones that weren't sent, thus remove all the ones from storage that were successfully sent
                    }
                    // send all previous objects that weren't sent to edge
                    for (JSONObject j : storage.getSendOff()) {
                        j.put("prev", true); // tell edge that this is data from a previous session
                        String time = Long.toString(System.currentTimeMillis());
                        j.put("timestamp", time);
                        dos.writeUTF(j.toString());
                    }
                    storage.clearSendOffAll(); // empty storage so objects from current session can be stored
                    sent = 0;
                }
            }
            storage.clearUnavailable(); // remove ip address from storage, since all data has been transmitted now
        } catch (IOException e) {
            handleException();
        }
    }

    private void handleException() {
        // how to proceed after connection to edge is lost
        storage.putSentVariable(sent); // store the number of successfully sent objects
        System.out.println("Objects arrived at client:" + sent);
        if (!(unavailable.contains(ip))) {
            storage.putUnavailable(ip); // store ip address of edge
            System.out.println("Client is unavailable");
        }
    }
}
