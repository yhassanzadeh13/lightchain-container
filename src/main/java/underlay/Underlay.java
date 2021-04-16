package underlay;

import blockchain.LightChainRMIInterface;
import underlay.requests.Request;

public interface Underlay {
    public void sendMessage(String address, Request request);

    public LightChainRMIInterface getLightChainRMI(String adrs);

}
