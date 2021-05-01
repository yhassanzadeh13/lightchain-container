package underlay.responses;

import signature.SignedBytes;

/**
 * Represents a response which returns SignedBytes.
 */
public class SignatureResponse extends GenericResponse{
    public SignedBytes response;

    public SignatureResponse(SignedBytes response) {
        this.response = response;
    }
}
