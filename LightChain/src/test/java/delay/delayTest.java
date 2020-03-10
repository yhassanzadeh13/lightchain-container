package delay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class delayTest {
    @Test
    public void testWhetherDelayIsSame(){
        String adrs1 = "adrs1";
        String adrs2 = "adrs2";
        String adrs3 = "adrs3";
        DelayTracker dt = DelayTracker.getInstance();
        int delay12 = dt.getDelay(adrs1, adrs2);

        int delay21 = dt.getDelay(adrs2, adrs1);
        int delay12Repeated = dt.getDelay(adrs1, adrs2);

        assertEquals(delay12, delay12Repeated);
    }
}
