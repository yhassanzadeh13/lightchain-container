package underlay;

import blockchain.LightChainInterface;
import blockchain.Transaction;
import fixture.Fixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import signature.SignedBytes;
import skipGraph.NodeInfo;
import skipGraph.SkipNodeInterface;
import underlay.requests.lightchain.GetPublicKeyRequest;
import underlay.requests.lightchain.PoVRequest;
import underlay.requests.skipgraph.GetLeftNodeRequest;
import underlay.requests.skipgraph.GetRightNumIDRequest;
import underlay.responses.IntegerResponse;
import underlay.responses.NodeInfoResponse;
import underlay.responses.PublicKeyResponse;
import underlay.responses.SignatureResponse;
import underlay.rmi.RMIUnderlay;

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
  private SkipNodeInterface targetSkipNode;
  private RMIUnderlay underlay;
  private LightChainInterface targetLightChain;

  @BeforeEach
  public void init() {
    targetSkipNode = mock(SkipNodeInterface.class);
    targetLightChain = mock(LightChainInterface.class);
    underlay = new RMIUnderlay(Fixtures.PortFixture());
    underlay.setSkipNode(targetSkipNode);
    underlay.setLightChainNode(targetLightChain);
  }

  @AfterEach
  public void destroyRMI() {
    underlay.terminate();
  }

  /**
   * A test for the GetRightNumIDRequest message for the RMIUnderlay
   *
   * @throws RemoteException
   * @throws FileNotFoundException
   */
  @Test
  public void RightNumIDTest() throws RemoteException, FileNotFoundException {
    when(targetSkipNode.getRightNumID(0, 60)).thenReturn(80);
    IntegerResponse response = IntegerResponseOf(underlay.answer(new GetRightNumIDRequest(0, 60)));
    assertEquals(response.result, 80);
  }

  /**
   * A test for the GetLeftNodeRequest message for the RMIUnderlay
   *
   * @throws RemoteException
   * @throws FileNotFoundException
   */
  @Test
  public void GetLeftNodeTest() throws RemoteException, FileNotFoundException {
    NodeInfo result = Fixtures.NodeInfoFixture();
    when(targetSkipNode.getLeftNode(1, 2)).thenReturn(result);
    NodeInfoResponse response = NodeInfoResponseOf(underlay.answer(new GetLeftNodeRequest(1, 2)));
    assertEquals(result, response.result);
  }


    /**
     * A test for the PoVRequest message for the RMIUnderlay
     * @throws RemoteException
    * @throws FileNotFoundException
     */
     @Test
    public void PoVRequestTest() throws RemoteException, FileNotFoundException {
        Transaction testTransaction = Fixtures.TransactionFixture();
        SignedBytes testResult = Fixtures.SignedBytesFixture();
        when(targetLightChain.PoV(testTransaction)).thenReturn(testResult);
        SignatureResponse response = SignatureResponseOf(underlay.answer(new
                PoVRequest(testTransaction)));
        assertEquals(testResult, response.result);
    }


    /**
     * A test for the GetPublicKeyRequest message for the RMIUnderlay
     * @throws RemoteException
     * @throws FileNotFoundException
     */
    @Test
    public void GetPublicKeyRequestTest() throws RemoteException, FileNotFoundException {
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
        when(targetLightChain.getPublicKey()).thenReturn(testResult);
        PublicKeyResponse response = PublicKeyResponseOf(underlay.answer(new
                GetPublicKeyRequest()));
        assertEquals(testResult, response.result);
    }
 }
