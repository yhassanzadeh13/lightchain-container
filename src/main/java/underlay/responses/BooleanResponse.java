package underlay.responses;

/** Represents a response which returns a boolean. */
public final class BooleanResponse extends GenericResponse {
  /** The variable that'll be returned. */
  public final boolean result;

  /**
   * The constructor.
   *
   * @param res The boolean value which will be returned.
   */
  public BooleanResponse(final boolean res) {
    this.result = res;
  }

  public static BooleanResponse BooleanResponseOf(GenericResponse response){
    return (BooleanResponse) response;
  }
}
