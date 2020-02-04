package simulation;

import blockchain.Parameters;
import util.PropertyManager;

public class SimulationDriver {
    public static void main(String[] args){
        propMng = new PropertyManager("simulation.config");
        Parameters params = new Parameters();
        params.setAlpha(getIntProperty("alpha", "10"));
        params.setTxMin(getIntProperty("txmin","1"));
        params.setSignaturesThreshold(getIntProperty("signaturesThreshold","1"));
        params.setInitialBalance(getIntProperty("initialBalance", "20"));
        params.setLevels(getIntProperty("levels", "30"));
        params.setValidationFees(getIntProperty("validationFees", "1"));
        params.setMode(getBoolProperty("Mode", "True"));

        int nodeCount = getIntProperty("nodeCount", "10");
        int iterations = getIntProperty("iterations", "100");
        int pace = getIntProperty("pace","1");

        Simulation.startSimulation(params, nodeCount, iterations, pace);

    }

    private static PropertyManager propMng;
    private static int getIntProperty(String key, String def){
        return Integer.parseInt(propMng.getProperty(key, def));
    }


    private static boolean getBoolProperty(String key, String def){
        return Boolean.parseBoolean(propMng.getProperty(key, def));
    }
}
