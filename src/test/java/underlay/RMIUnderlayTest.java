package underlay;

import blockchain.LightChainInterface;
import blockchain.Transaction;
import fixture.Fixtures;
import org.junit.jupiter.api.Test;
import signature.SignedBytes;
import skipGraph.NodeInfo;
import skipGraph.SkipNodeInterface;
import underlay.rmi.RMIUnderlay;
import underlay.requests.skipgraph.GetLeftNodeRequest;
import underlay.requests.lightchain.GetPublicKeyRequest;
import underlay.requests.skipgraph.GetRightNumIDRequest;
import underlay.requests.lightchain.PoVRequest;
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
import static underlay.responses.IntegerResponse.IntegerResponseOf;
import static underlay.responses.NodeInfoResponse.NodeInfoResponseOf;
import static underlay.responses.PublicKeyResponse.PublicKeyResponseOf;
import static underlay.responses.SignatureResponse.SignatureResponseOf;

/**
 * RMIUnderlay Unit Tests to make sure messages are sent/received correctly. The tests are done
 * through mocking.
 */
public class RMIUnderlayTest {
  private Fixtures fixtures = new Fixtures();
  /**
   * A test for the GetRightNumIDRequest message for the RMIUnderlay
   *
   * @throws RemoteException
   * @throws FileNotFoundException
   */
}

//    @Test
//    public void RightNumIDTest() throws RemoteException, FileNotFoundException {
//        SkipNodeInterface targetSkipNode = mock(SkipNodeInterface.class);
//        RMIUnderlay.setSkipNode(targetRMI);          // method created just for testing purposes
//        when(targetRMI.getRightNumID(0,60)).thenReturn(80);
//    IntegerResponse response =
//        IntegerResponseOf(
//            RMIUnderlay.sendMessage(new GetRightNumIDRequest(0, 60), fixtures.IPAddressFixture()));
//        assertEquals(response.result, 80);
//    }
//
//    /**
//     * A test for the GetLeftNodeRequest message for the RMIUnderlay
//     * @throws RemoteException
//     * @throws FileNotFoundException
//     */
//    @Test
//    public void GetLeftNodeTest() throws RemoteException, FileNotFoundException {
//        SkipNodeInterface targetRMI = mock(SkipNodeInterface.class);
//        RMIUnderlay.setSkipNode(targetRMI);          // method created just for testing purposes
//        NodeInfo result = fixtures.NodeInfoFixture();
//        when(targetRMI.getLeftNode(1,2)).thenReturn(result);
//        NodeInfoResponse response = NodeInfoResponseOf(RMIUnderlay.sendMessage(new GetLeftNodeRequest(1,2), fixtures.IPAddressFixture()));
//        assertEquals(result, response.result);
//    }
//
//    /**
//     * A test for the PoVRequest message for the RMIUnderlay
//     * @throws RemoteException
//     * @throws FileNotFoundException
//     */
//    @Test
//    public void PoVRequestTest() throws RemoteException, FileNotFoundException {
//        LightChainInterface targetRMI = mock(LightChainInterface.class);
//        RMIUnderlay.setLightChainRMI(targetRMI);
//        Transaction testTransaction = fixtures.TransactionFixture();
//        SignedBytes testResult = fixtures.SignedBytesFixture();
//        when(targetRMI.PoV(testTransaction)).thenReturn(testResult);
//        SignatureResponse response = SignatureResponseOf(RMIUnderlay.sendMessage(new PoVRequest(testTransaction), fixtures.IPAddressFixture(), InterfaceType.LightChainInterface));
//        assertEquals(testResult, response.result);
//    }
//
//    /**
//     * A test for the GetPublicKeyRequest message for the RMIUnderlay
//     * @throws RemoteException
//     * @throws FileNotFoundException
//     */
//    @Test
//    public void GetPublicKeyRequestTest() throws RemoteException, FileNotFoundException {
//        LightChainInterface targetRMI = mock(LightChainInterface.class);
//        RMIUnderlay.setLightChainRMI(targetRMI);
//        PublicKey testResult = new PublicKey() {
//            @Override
//            public String getAlgorithm() {
//                return null;
//            }
//
//            @Override
//            public String getFormat() {
//                return null;
//            }
//
//            @Override
//            public byte[] getEncoded() {
//                return new byte[0];
//            }
//        };
//        when(targetRMI.getPublicKey()).thenReturn(testResult);
//        PublicKeyResponse response = PublicKeyResponseOf(RMIUnderlay.sendMessage(new GetPublicKeyRequest(), fixtures.IPAddressFixture(), InterfaceType.LightChainInterface));
//        assertEquals(testResult, response.result);
//    }
//}
