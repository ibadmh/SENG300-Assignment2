package ca.ucalgary.seng300.booking;

import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for rule R4 (cancellation time):
 * an owner can cancel an ACTIVE booking at least 120 minutes before its start.
 * Exactly 120 is allowed. Less than 120 returns TOO_LATE.
 *
 * How every test is built (see evidence/R4_test_plan.md):
 *  - "Now" is fixed by TestFixtures (2026-11-01T10:00:00Z). at(m) means m minutes from now.
 *  - Every test makes its own TestFixtures and seeds two bookings with f.seed(...):
 *      b1 (R204, owner student-a), the booking being cancelled, and
 *      b2 (R305, owner student-c, at(300) to at(360), ACTIVE), which must never change.
 *  - Only the time remaining before b1 starts is varied. Owner, caller and state stay valid,
 *    except in the precedence tests, which check that R3 is decided before R4.
 *  - A rejection is checked on status, saved data and notifications, not on status alone.
 *    A status-only check would miss a bug that saves CANCELLED and then answers TOO_LATE.
 *  - Expected answers come from the rule text, not from the service code.
 */
class R4CancellationTimeTest {

    private static final String ROOM = "R204";
    private static final String OWNER = "student-a";
    private static final String NON_OWNER = "student-b";

    /** Seeds the untouched second booking. Returns it so tests can compare against it. */
    private static Booking seedOther(TestFixtures f) {
        return f.seed("b2", "R305", "student-c", 300, 360, BookingState.ACTIVE);
    }

    /** The request was refused with the given status and nothing was saved or notified. */
    private static void assertRejectedUnchanged(TestFixtures f, CancelStatus expected,
                                                CancelStatus actual, List<Booking> savedBefore) {
        assertEquals(expected, actual, "wrong cancel status");
        assertEquals(savedBefore, f.repository.all(),
            "a rejected cancel must not change any saved booking");
        assertEquals(List.of(), f.notifier.requests(),
            "a rejected cancel must not request a notification");
    }

    /** b1 was cancelled: status, saved state, exactly one notification, b2 unchanged. */
    private static void assertCancelled(TestFixtures f, CancelStatus actual,
                                        Booking b1Before, Booking b2Before) {
        Booking b1After = b1Before.withState(BookingState.CANCELLED);
        assertEquals(CancelStatus.CANCELLED, actual, "owner cancel in time must succeed");
        assertEquals(b1After, f.repository.find("b1"),
            "b1 must be saved as CANCELLED with every other field unchanged");
        assertEquals(List.of(b1After, b2Before), f.repository.all(),
            "only b1 may change, and no booking may be added or removed");
        assertEquals(List.of(b1After), f.notifier.requests(),
            "exactly one notification, for the cancelled b1");
    }

    // ---------------------------------------------------------------------------------
    // Too late: owner, ACTIVE, less than 120 minutes before start (groups T1 to T4)
    // ---------------------------------------------------------------------------------

    /**
     * (start, end) in minutes from now.
     *  -30,30   T1 already started (seeded directly; time passes after creation)
     *    0,60   T2 starts exactly now
     *   60,120  T3 clearly too late
     *  119,179  T4 one minute under the edge
     */
    @ParameterizedTest(name = "start={0},end={1}")
    @CsvSource({"-30,30", "0,60", "60,120", "119,179"})
    void tooLate_returnsTooLateAndChangesNothing(long start, long end) {
        TestFixtures f = new TestFixtures();
        f.seed("b1", ROOM, OWNER, start, end, BookingState.ACTIVE);
        seedOther(f);
        List<Booking> savedBefore = f.repository.all();

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertRejectedUnchanged(f, CancelStatus.TOO_LATE, status, savedBefore);
    }

    // ---------------------------------------------------------------------------------
    // In time: owner, ACTIVE, at least 120 minutes before start (groups T5 to T7)
    // ---------------------------------------------------------------------------------

    /**
     *  120,180    T5 exactly the edge: "exactly 120 is allowed" (kills minutes <= 120)
     *  121,181    T6 one minute over the edge
     *  1440,1500  T7 clearly in time (one day)
     */
    @ParameterizedTest(name = "start={0},end={1}")
    @CsvSource({"120,180", "121,181", "1440,1500"})
    void inTime_cancelsSavesAndNotifiesOnce(long start, long end) {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, start, end, BookingState.ACTIVE);
        Booking b2 = seedOther(f);

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertCancelled(f, status, b1, b2);
    }

    // ---------------------------------------------------------------------------------
    // End independence: the rule measures to the start, not the end
    // ---------------------------------------------------------------------------------

    /** A long booking that starts in 119 minutes is still too late. */
    @Test
    void tooLate_longBookingStillTooLate() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", ROOM, OWNER, 119, 600, BookingState.ACTIVE);
        seedOther(f);
        List<Booking> savedBefore = f.repository.all();

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertRejectedUnchanged(f, CancelStatus.TOO_LATE, status, savedBefore);
    }

    /** A one-minute booking that starts in exactly 120 minutes can still be cancelled. */
    @Test
    void edge_shortBookingStillCancels() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, 120, 121, BookingState.ACTIVE);
        Booking b2 = seedOther(f);

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertCancelled(f, status, b1, b2);
    }

    // ---------------------------------------------------------------------------------
    // Precedence with R3: time is too late in every row, but R3 decides first
    // ---------------------------------------------------------------------------------

    /** Decision table D8: non-owner, ACTIVE, too late. Ownership is checked first. */
    @Test
    void tooLate_nonOwnerGetsDenied() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", ROOM, OWNER, 30, 90, BookingState.ACTIVE);
        seedOther(f);
        List<Booking> savedBefore = f.repository.all();

        CancelStatus status = f.service.cancel("b1", NON_OWNER);

        assertRejectedUnchanged(f, CancelStatus.DENIED, status, savedBefore);
    }

    /** Decision table D4: owner, CANCELLED, too late. State is checked before time. */
    @Test
    void tooLate_cancelledBookingGetsInactive() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", ROOM, OWNER, 30, 90, BookingState.CANCELLED);
        seedOther(f);
        List<Booking> savedBefore = f.repository.all();

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertRejectedUnchanged(f, CancelStatus.INACTIVE, status, savedBefore);
    }

    /** Decision table D6: owner, COMPLETED, too late. State is checked before time. */
    @Test
    void tooLate_completedBookingGetsInactive() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", ROOM, OWNER, 30, 90, BookingState.COMPLETED);
        seedOther(f);
        List<Booking> savedBefore = f.repository.all();

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertRejectedUnchanged(f, CancelStatus.INACTIVE, status, savedBefore);
    }

    // ---------------------------------------------------------------------------------
    // Other bookings
    // ---------------------------------------------------------------------------------

    /**
     * b2 is in a different room, has a different owner and is itself too late to cancel.
     * Cancelling b1 at the edge must leave b2 exactly as it was.
     */
    @Test
    void cancel_leavesOtherBookingsUnchanged() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OWNER, 120, 180, BookingState.ACTIVE);
        Booking b2 = f.seed("b2", "R305", "student-c", 60, 120, BookingState.ACTIVE);

        CancelStatus status = f.service.cancel("b1", OWNER);

        assertCancelled(f, status, b1, b2);
        assertEquals(b2, f.repository.find("b2"), "b2 must be untouched");
    }
}
