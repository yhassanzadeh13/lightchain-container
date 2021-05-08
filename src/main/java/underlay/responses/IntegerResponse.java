package underlay.responses;

/** Represents a response which returns an integer. */
public class IntegerResponse extends GenericResponse {
  public int result;

  public IntegerResponse(int result) {
    this.result = result;
  }
}
