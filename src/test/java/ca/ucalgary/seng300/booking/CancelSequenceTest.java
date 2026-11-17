package ca.ucalgary.seng300.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;

/*

S1 owner cancels in time -> CANCELLED, then cancels again -> INACTIVE
   (cancelling again adds no notification and changes nothing)
S2 non-owner tries first -> DENIED, then the owner cancels -> CANCELLED
   (the denied try must not change the booking, so the owner can still cancel)

   S3 owner cancels exactly 120 min before start -> CANCELLED, then again -> INACTIVE
   (120 is the edge R4 still allows)
 */
class CancelSequenceTest {
    private static final String ROOM = "R204";
    private static final String OWNER = "student-a";
    private static final String NON_OWNER = "student-b";

    // second booking in another room that neither sequence should touch
    private static Booking seedOther(TestFixtures f) {
        return f.seed("b2", "R305", "student-c", 300, 360, BookingState.ACTIVE);
    }

    // S1
    @Test
    void ownerCancelsThenCancelsAgain_secondIsInactiveAndNoNewNotification() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, 180, 240, BookingState.ACTIVE);
        Booking other = seedOther(f);

        CancelStatus first = f.service.cancel("b1", OWNER);
        assertEquals(CancelStatus.CANCELLED, first);
        assertEquals(BookingState.CANCELLED, f.repository.find("b1").state());
        assertEquals(1, f.notifier.requests().size());

        List<Booking> savedAfterFirst = f.repository.all();
        CancelStatus second = f.service.cancel("b1", OWNER);

        assertEquals(CancelStatus.INACTIVE, second);
        assertEquals(savedAfterFirst, f.repository.all(), "second cancel must not change saved data");
        assertEquals(List.of(b1.withState(BookingState.CANCELLED)), f.notifier.requests(),
                "cancelling again must not add a notification");
        assertEquals(other, f.repository.find("b2"));
    }

    // S2
    @Test
    void nonOwnerDeniedThenOwnerCancels_ownerStillGetsCancelled() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, 180, 240, BookingState.ACTIVE);
        Booking other = seedOther(f);

        CancelStatus denied = f.service.cancel("b1", NON_OWNER);
        assertEquals(CancelStatus.DENIED, denied);
        assertEquals(b1, f.repository.find("b1"), "denied cancel must leave the booking as it was");
        assertEquals(List.of(), f.notifier.requests());

        CancelStatus ownerResult = f.service.cancel("b1", OWNER);

        assertEquals(CancelStatus.CANCELLED, ownerResult);
        assertEquals(BookingState.CANCELLED, f.repository.find("b1").state());
        assertEquals(List.of(b1.withState(BookingState.CANCELLED)), f.notifier.requests());
        assertEquals(other, f.repository.find("b2"));
    }
    // S3
    @Test
    void ownerCancelsAtExactly120Minutes_thenAgain_cancelledThenInactive() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, 120, 180, BookingState.ACTIVE);
        Booking other = seedOther(f);

        CancelStatus first = f.service.cancel("b1", OWNER);
        assertEquals(CancelStatus.CANCELLED, first, "exactly 120 minutes must still be allowed");
        assertEquals(BookingState.CANCELLED, f.repository.find("b1").state());
        assertEquals(List.of(b1.withState(BookingState.CANCELLED)), f.notifier.requests());

        CancelStatus second = f.service.cancel("b1", OWNER);
        assertEquals(CancelStatus.INACTIVE, second);
        assertEquals(1, f.notifier.requests().size(), "cancelling again must not add a notification");
        assertEquals(other, f.repository.find("b2"));
    }
}