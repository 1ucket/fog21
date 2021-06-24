import java.util.*;

public class TemperatureSensor extends Sensor {
	double center = 22;
	double lastValue = center;
	Random r = new Random();
	
	public String getData()
	{
		lastValue = lastValue + r.nextGaussian() * (0.001 + 1/(Utils.clamp(Math.abs(lastValue - center), 10, 1000))); //add a random gauss value between 0.001 and 0.1 to the last value to simulate temperature changes. The value is centered around 22°.
		String value = String.valueOf(lastValue);
                return value;
	}
	
	public dataSource getType()
	{
		return dataSource.TEMPERATURE;
	}
        
        public String toString(){
            return "TEMPERATURE";
        }
}