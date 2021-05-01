package underlay;

import blockchain.LightChainRMIInterface;
import blockchain.Transaction;
import org.junit.jupiter.api.Test;
import signature.SignedBytes;
import skipGraph.NodeInfo;
import skipGraph.RMIInterface;
import underlay.requests.GetLeftNodeRequest;
import underlay.requests.GetPublicKeyRequest;
import underlay.requests.GetRightNumIDRequest;
import underlay.requests.PoVRequest;
import underlay.responses.NodeInfoResponse;
import underlay.responses.IntegerResponse;
import underlay.responses.PublicKeyResponse;
import underlay.responses.SignatureResponse;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Underlay Unit Tests to make sure messages are sent/received correctly.
 */
public class UnderlayTest {
    private final Underlay underlay = new Underlay();

    @Test
    public void RightNumIDTest() throws RemoteException, FileNotFoundException {
        RMIInterface targetRMI = mock(RMIInterface.class);
        underlay.setTargetRMI(targetRMI);          // method created just for testing purposes
        when(targetRMI.getRightNumID(0,60)).thenReturn(80);
        assertEquals(((IntegerResponse) underlay.sendMessage(new GetRightNumIDRequest(0,60), "test")).result
                , 80);
    }

    @Test
    public void GetLeftNodeTest() throws RemoteException, FileNotFoundException {
        RMIInterface targetRMI = mock(RMIInterface.class);
        underlay.setTargetRMI(targetRMI);          // method created just for testing purposes
        NodeInfo result = new NodeInfo("1.1.1.1", 12, "test");
        when(targetRMI.getLeftNode(1,2)).thenReturn(result);
        assertEquals(result,
                ((NodeInfoResponse) underlay.sendMessage(new GetLeftNodeRequest(1,2), "test")).responseResult);
    }


    // lightchainnode tests

    @Test
    public void PoVRequestTest() throws RemoteException, FileNotFoundException {
        LightChainRMIInterface targetRMI = mock(LightChainRMIInterface.class);
        underlay.setLightChainRMI(targetRMI);
        Transaction testTransaction = new Transaction("", 2, "","1",2);
        SignedBytes testResult = new SignedBytes("test results".getBytes());
        when(targetRMI.PoV(testTransaction)).thenReturn(testResult);
        SignedBytes signature = ((SignatureResponse) underlay.sendMessage(new PoVRequest(testTransaction), "not_a_real_ip", InterfaceTypes.LightChainInterface)).response;
        assertEquals(testResult, signature);
    }

    @Test
    public void GetPublicKeyRequestTest() throws RemoteException, FileNotFoundException {
        LightChainRMIInterface targetRMI = mock(LightChainRMIInterface.class);
        underlay.setLightChainRMI(targetRMI);
        PublicKey testResult = new PublicKey() {
            @Override
            public String getAlgorithm() {
                return null;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        };
        when(targetRMI.getPublicKey()).thenReturn(testResult);
        PublicKey pk = ((PublicKeyResponse) underlay.sendMessage(new GetPublicKeyRequest(), "test address", InterfaceTypes.LightChainInterface)).response;
        assertEquals(testResult, pk);
    }
}
