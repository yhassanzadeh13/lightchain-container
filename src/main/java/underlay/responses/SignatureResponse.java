package underlay.responses;

import signature.SignedBytes;

/** Represents a response which returns SignedBytes. */
public class SignatureResponse extends GenericResponse {
  public final SignedBytes result;

  public SignatureResponse(SignedBytes result) {
    this.result = result;
  }
  public static SignatureResponse SignatureResponseOf(GenericResponse response) {
    return (SignatureResponse) response;
  }

}
