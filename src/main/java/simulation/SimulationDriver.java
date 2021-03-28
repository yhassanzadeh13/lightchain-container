package simulation;

import blockchain.Parameters;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import util.PropertyManager;

public class SimulationDriver {
    public static void main(String[] args){
        initializeLogger();
        propMng = new PropertyManager("simulation.config");
        Parameters params = new Parameters();
        params.setAlpha(getIntProperty("alpha", "12"));
        params.setTxMin(getIntProperty("txmin","5"));
        params.setSignaturesThreshold(getIntProperty("signaturesThreshold","5"));
        params.setInitialBalance(getIntProperty("initialBalance", "20"));
        params.setLevels(getIntProperty("levels", "30"));
        params.setValidationFees(getIntProperty("validationFees", "1"));
        params.setMode(getBoolProperty("Mode", "True"));
        params.setInitialToken(getIntProperty("token", "20"));
        params.setChain(getBoolProperty("ContractMode", "True"));
        int nodeCount = getIntProperty("nodeCount", "20");
        int iterations = getIntProperty("iterations", "50");
        int pace = getIntProperty("pace","1");
        
        Logger lg = Logger.getLogger(SimulationDriver.class);
        lg.info("Starting simulation with parameters: "+ params+" Number of nodes: "+ nodeCount+ "\n Number of iterations: "+ iterations+ "\n Pace: "+ pace);

        simulation.Simulation.startSimulation(params, nodeCount, iterations, pace);
        System.exit(0);
    }

    private static void initializeLogger(){
        PropertyConfigurator.configure("log4j.properties");
    }

    private static PropertyManager propMng;
    private static int getIntProperty(String key, String def){
        return Integer.parseInt(propMng.getProperty(key, def));
    }


    private static boolean getBoolProperty(String key, String def){
        return Boolean.parseBoolean(propMng.getProperty(key, def));
    }
}
