import org.json.simple.JSONObject;

public class DataPoint {
	public String source;
	public String value;
	public String time;
        public int ID;
        public static final int EDGE_ID = 1;
	
	public DataPoint(String source, String value, String time)
	{
		this.source = source;
		this.value = value;
		this.time = time;
                this.ID = (int) Math.floor(Math.random()*65535);
	}
	
	public JSONObject getJSON()
	{
		JSONObject data = new JSONObject();
		data.put("EDGE_ID", EDGE_ID);
                data.put("timestamp", this.time);
		data.put("type", this.source);
		data.put("value", this.value);
                data.put("ID", this.ID);
                
		return data;
	}
	
}