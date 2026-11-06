package ca.ucalgary.seng300.booking;

import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Demonstration only, lives on a temporary mutant branch and is never merged.
 * A return-value-only check: it looks at the status and nothing else.
 */
class StatusOnlyDemoTest {
    @Test
    void statusOnly_startEqualToNow() {
        TestFixtures f = new TestFixtures();
        assertEquals(CreateStatus.INVALID_TIME, f.service.create("R204", "student-a", at(0), at(60)).status());
    }
}
