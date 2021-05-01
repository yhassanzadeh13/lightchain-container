package underlay.responses;

import signature.SignedBytes;

public class SignatureResponse extends GenericResponse{
    public SignedBytes response;

    public SignatureResponse(SignedBytes response) {
        this.response = response;
    }
}
