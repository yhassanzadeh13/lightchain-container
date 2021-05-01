package underlay.responses;

import java.security.PublicKey;

/**
 * Represents a response which returns a PublicKey.
 */
public class PublicKeyResponse extends GenericResponse{
    public PublicKey response;

    public PublicKeyResponse(PublicKey response) {
        this.response = response;
    }
}
