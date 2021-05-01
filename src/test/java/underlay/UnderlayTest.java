package underlay;

import org.junit.jupiter.api.Test;
import skipGraph.NodeInfo;
import skipGraph.RMIInterface;
import underlay.requests.GetLeftNodeRequest;
import underlay.requests.GetRightNumIDRequest;
import underlay.responses.NodeInfoResponse;
import underlay.responses.NumIDResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnderlayTest {
    private Underlay underlay1 = new Underlay();

    @Test
    public void RightNumIDTest() throws RemoteException, FileNotFoundException {
        RMIInterface targetRMI = mock(RMIInterface.class);
        underlay1.setTargetRMI(targetRMI);          // method created just for testing purposes
        when(targetRMI.getRightNumID(0,60)).thenReturn(80);
        assertEquals(((NumIDResponse) underlay1.sendMessage(new GetRightNumIDRequest(0,60), "1.1.1.1")).numID
                , 80);
    }

    @Test
    public void GetLeftNodeTest() throws RemoteException, FileNotFoundException {
        RMIInterface targetRMI = mock(RMIInterface.class);
        underlay1.setTargetRMI(targetRMI);          // method created just for testing purposes
        NodeInfo result = new NodeInfo("1.1.1.1", 12, "test");
        when(targetRMI.getLeftNode(1,2)).thenReturn(result);
        assertEquals(((NodeInfoResponse) underlay1.sendMessage(new GetLeftNodeRequest(1,2), "1.1.1.1")).responseResult
                , result);
    }
}
