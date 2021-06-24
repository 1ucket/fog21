import java.util.*;

public class HumiditySensor extends Sensor {
	double min = 0;
	double max = 100;
	Random r = new Random();
	double lastValue = 40;
	
	public String getData()
	{
		lastValue = lastValue + r.nextGaussian()*0.01;
		double doubleValue = Utils.clamp(lastValue, 0, 100);
                String value = String.valueOf(doubleValue);
                return value;
	}
	public dataSource getType()
	{
		return dataSource.HUMIDITY;
	}
        
        public String toString(){
            return "HUMIDITY";
        }
}