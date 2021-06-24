import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;

public class Edge {

    private List<DataPoint> outBuffer = new ArrayList<DataPoint>();
    private List<Sensor> sensors = new ArrayList<Sensor>();
    String ip = "35.198.79.71";
    private int port = 80;
    private Socket socket;
    private DataInputStream inputstream;
    private DataOutputStream outputstream;
    private int ID;
    private String name;
    private int[] location = new int[2];
    private JSONParser jsonparser = new JSONParser();
    private int IOtimeout = 500; //timeout in ms

    boolean connected;

    public Edge() {
        sensors.add(new BrightnessSensor());
        sensors.add(new HumiditySensor());
        sensors.add(new MoistureSensor());
        sensors.add(new TemperatureSensor());

        this.ID = (int) Math.floor(Math.random() * 65535);
        this.name = ("Node " + this.ID);
        this.location[0] = (int) Math.floor(Math.random() * 10);
        this.location[1] = (int) Math.floor(Math.random() * 10);
    }

    private boolean receiveData() throws IOException {
        try {
            if (!connected) {
                connect();
            }
            String a = inputstream.readUTF();

            // turn received message into String
            byte[] utf8Bytes = a.getBytes("UTF8");
            String str = new String(utf8Bytes, "UTF8");
            JSONObject jsonObject = (JSONObject) jsonparser.parse(str);
            
            // if received object is from a previous session then acknowledge
            if (!(boolean) jsonObject.get("prev")) {
                outputstream.writeUTF("OK");
            }
            System.out.println(jsonObject);
            

            return true;
    	} catch (ParseException err){
    		System.out.println("JSON Parsing error");
    		return false;
        } catch (IOException e) {
        	System.out.println("Connection lost, attempting reconnect in next cycle");
            connected = false;
            return false;
        }
    }

    private void collectData() {
        for (Sensor s : sensors) {
            String time = Long.toString(System.currentTimeMillis());
            outBuffer.add(new DataPoint(s.toString(), s.getData(), time)); // read sensor values and add them to the outBuffer
        }
    }

    private boolean sendData() throws IOException {
        try {
            ListIterator<DataPoint> iterator = outBuffer.listIterator();
            if (!connected) {
                connect();
            }
            while (iterator.hasNext()) {
                DataPoint dp = iterator.next();
                JSONObject data = dp.getJSON();

                outputstream.writeUTF(data.toString());

                if (inputstream.available() > 0) {
                    receiveData();
                }

                iterator.remove();
            }
        } catch (IOException e) {
            connected = false;
            System.out.println("Connection lost, attempting reconnect in next cycle");
            return false;
        }
        return true;
    }

    private void connect() throws IOException {
        InetAddress address = InetAddress.getByName(ip);
        socket = new Socket();
        socket.setSoTimeout(IOtimeout);
        socket.connect(new InetSocketAddress(address, port), IOtimeout);
        inputstream = new DataInputStream(socket.getInputStream());
        outputstream = new DataOutputStream(socket.getOutputStream());
        System.out.println("connection established");
        JSONObject registration = new JSONObject();
        registration.put("ID", this.ID);
        registration.put("name", this.name);
        registration.put("pos", this.location);
        System.out.println(registration.toString());
        outputstream.writeUTF(registration.toString());
        connected = true;
    }

    public static void main(String[] args) throws IOException {
        Edge edge = new Edge();
        int interval = 1000;

        long lastSent = System.currentTimeMillis();
        while (true) {
            // establish connection
            edge.collectData();
            edge.sendData();

            long millis = System.currentTimeMillis(); // send regularly independent of data collection &
            // sending time
            long timeSinceLast = millis - lastSent;
            lastSent = millis;
            try {
                Thread.sleep(Utils.clamp(interval - timeSinceLast, 0, interval));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }
}