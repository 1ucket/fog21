import java.time.LocalTime;
import java.util.Random;

public class BrightnessSensor extends Sensor {

    Random r = new java.util.Random();

    public String getData() {
        LocalTime time = LocalTime.now();
        double timedist = time.getMinute() + time.getHour() * 60; //linear time curve from 0 to 24h
        if (timedist > 720) {
            timedist = Math.abs(timedist - 1440); //invert curve after noon to achieve pyramid-like distribution
        }
        timedist = timedist / 720 + r.nextGaussian() * 0.01; //normalize and add random noise

        String value = String.valueOf(timedist);
        return value;
    }

    public dataSource getType() {
        return dataSource.BRIGHTNESS;
    }

    public String toString() {
        return "BRIGHTNESS";
    }
}
