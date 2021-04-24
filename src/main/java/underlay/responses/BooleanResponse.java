package underlay.responses;

public class BooleanResponse extends GenericResponse {
    public boolean result;

    public BooleanResponse(boolean result) {
        this.result = result;
    }
}
