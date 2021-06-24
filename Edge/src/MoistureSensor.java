
import java.util.*;

public class MoistureSensor extends Sensor {
	double center = 3;
	double lastValue = center;
	Random r = new Random();
	
	public String getData()
	{
		lastValue = lastValue + r.nextGaussian() * (Math.abs(lastValue - center));
		double doubleValue = Utils.clamp(lastValue, 0, 10);
                String value = String.valueOf(doubleValue);
                return value;
	}
	
	public dataSource getType()
	{
		return dataSource.MOISTURE;
	}
        
        public String toString(){
            return "MOISTURE";
        }
}