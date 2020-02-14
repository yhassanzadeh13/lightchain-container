package delay;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

// This tracks generated delays between nodes and generates delays if they are not found.
public class DelayTracker {
    // The mean parameter used to generate delays following a normal distribution
    private double mean;
    // The standard deviation parameter used to generate delays following a normal distribution
    private double stddev;
    // Stores all the generated stored delays
    private ConcurrentHashMap<AbstractMap.SimpleEntry<String, String>, Integer> map;

    private static Random rnd = new Random();
    private static DelayTracker _instance = new DelayTracker();

    // threshold for number of rerolls to find a valid delay
    private static final int threshold = 6;

    // Creates a Delay tracker which generates delates with mean and stddev equal to the parameters which most closely resemble real distributions found during testing
    // mean = 159.92 ms
    // stddev = 95.93 ms
    public DelayTracker(){
        this(159.92,95.93);
//        this(0,0);
    }

    // Creates a Delay tracker which generates delays with mean equal to mean and stddev equal to stddev
    public DelayTracker(double mean, double stddev){
        this.mean = mean;
        this.stddev = stddev;
        map = new ConcurrentHashMap<>();
    }

    public static DelayTracker getInstance(){
        return _instance;
    }

    public int getDelay(String senderAddress, String receiverAddress){
        AbstractMap.SimpleEntry<String, String> pr = new AbstractMap.SimpleEntry<String, String>(senderAddress, receiverAddress);
        int delay = map.getOrDefault(pr, -1);
        if(delay==-1){
            delay = generateDelay();
            map.put(new AbstractMap.SimpleEntry<String, String>(senderAddress, receiverAddress), delay);
        }
        return delay;
    }

    public int generateDelay(){
        return generateDelay(mean, stddev);
    }

    public int generateDelay(double mean, double stddev){
        int delay = -1;
        for(int i = 0; i<threshold; i++){
            delay = (int)(rnd.nextGaussian()*stddev + mean);
            if(delay > 0) break;
        }
        return delay;
    }


}
