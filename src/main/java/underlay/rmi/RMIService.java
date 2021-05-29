package underlay.rmi;

import underlay.InterfaceType;
import underlay.requests.GenericRequest;
import underlay.responses.GenericResponse;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Represents a Java RMI Service. A RMI service only has a single function that dispatches the received request
 * to the local `RequestHandler` instance.
 */
public interface RMIService extends Remote {
    GenericResponse answer(GenericRequest req) throws RemoteException, FileNotFoundException;
}
