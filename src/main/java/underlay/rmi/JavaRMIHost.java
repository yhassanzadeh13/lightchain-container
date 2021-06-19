package underlay.rmi;

import underlay.InterfaceType;
import underlay.requests.GenericRequest;
import underlay.responses.GenericResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class JavaRMIHost extends UnicastRemoteObject implements RMIService{

  private final RMIUnderlay underlay;

  public JavaRMIHost(RMIUnderlay underlay) throws RemoteException {
    this.underlay = underlay;
  }

  public GenericResponse answer(GenericRequest req) throws RemoteException, FileNotFoundException {
    return underlay.answer(req);
  }
}
