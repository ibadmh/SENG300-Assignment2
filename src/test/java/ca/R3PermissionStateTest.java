package ca.ucalgary.seng300.booking;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;

/*Tests for R3
Check ownership first. A non-owner receives DENIED. For an owner, a CANCELLED or 
COMPLETED booking returns INACTIVE.

Decision table rows covered: (shared table in R4_test_plan.md)

D3 owner, CANCELLED, 120 or more minutes before start -> INACTIVE
D5 owner, COMPLETED, 120 or more minutes before start -> INACTIVE
D7 non-owner, ACTIVE, 120 or more minutes before start -> DENIED
D9 non-owner, CANCELLED, any time -> DENIED
D10 non-owner, COMPLETED, any time -> DENIED

 */
class R3PermissionStateTest {
    private static final String ROOM = "R204";
    private static final String OWNER = "student-a";
    private static final String NON_OWNER = "student-b";
    private static final String OTHER_OWNER = "student-c";

    // add a second booking which no test can change
    private static Booking seedOther(TestFixtures f){
        return f.seed("b2", "R305", OTHER_OWNER, 300, 360, BookingState.ACTIVE);
    }

    //request was refused with the expected status; nothing was saved or notified. 
    private static void assertRejectedUnchanged(TestFixtures f, CancelStatus expected, CancelStatus actual, List<Booking> savedBefore){
        assertEquals(expected, actual, "wrong cancel status");
        assertEquals(savedBefore, f.repository.all(), "a rejected cancel must not change any saved booking");
        assertEquals(List.of(), f.notifier.requests(), "a rejected cancel must not request a notification");
        
    }

    //TODO: D3, D5, D7, D9, D10
}

