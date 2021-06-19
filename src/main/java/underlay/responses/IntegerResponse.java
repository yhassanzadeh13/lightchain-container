package underlay.responses;

/** Represents a response which returns an integer. */
public class IntegerResponse extends GenericResponse {
  public final int result;

  public IntegerResponse(int result) {
    this.result = result;
  }

  public static IntegerResponse IntegerResponseOf(GenericResponse response) {
    return (IntegerResponse) response;
  }
}
