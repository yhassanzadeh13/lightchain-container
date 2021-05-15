package underlay;

import underlay.requests.GenericRequest;
import underlay.responses.GenericResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

public abstract class Underlay {
    public abstract GenericResponse sendMessage(GenericRequest req, String targetAddress, InterfaceType interfaceType) throws RemoteException, FileNotFoundException;
}
