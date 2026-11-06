package ca.ucalgary.seng300.booking;

import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for rule R1 (valid times):
 * a booking must start after now and end after its start. This is checked before
 * conflicts. Bad times return INVALID_TIME and save nothing.
 *
 * Plain-language guide to how every test is built:
 *  - "Now" is fixed by TestFixtures (2026-11-01T10:00:00Z). at(m) means m minutes from now,
 *    so at(0) is now and at(-1) is one minute ago. No real clock, no sleeping.
 *  - Every test makes its own new TestFixtures, so each one starts with an empty repository,
 *    an empty notification list and a fresh ID counter. Tests cannot affect each other.
 *  - For a rejected request we check five things, not just the answer:
 *      1. the status is INVALID_TIME,
 *      2. there is no booking id,
 *      3. the saved bookings are exactly what they were before (nothing was added or changed),
 *      4. no notification was requested.
 *    Checking only the status could miss a bug that saves the bad booking and then says "no".
 *  - Expected answers come from the rule text, not from reading the service code.
 */
class R1TimeValidationTest {

    private static final String ROOM = "R204";
    private static final String OWNER = "student-a";
    private static final String OTHER_OWNER = "student-b";

    // ---------------------------------------------------------------------------------
    // Helpers (shared checks, so each test stays short and says what it is about)
    // ---------------------------------------------------------------------------------

    /** The request must be refused as INVALID_TIME and leave the world exactly as it was. */
    private static void assertRejectedAsInvalidTime(TestFixtures f, CreateResult result,
                                                    List<Booking> savedBefore) {
        assertNotNull(result, "create() must return a result object");
        assertEquals(CreateStatus.INVALID_TIME, result.status(),
            "bad times must give INVALID_TIME");
        assertNull(result.bookingId(),
            "a rejected request must not hand out a booking id");
        assertEquals(savedBefore, f.repository.all(),
            "a rejected request must not add, change or remove any saved booking");
        assertEquals(List.of(), f.notifier.requests(),
            "a rejected create must not request any notification");
    }

    // ---------------------------------------------------------------------------------
    // Boundaries and groups that must be rejected (empty repository)
    // ---------------------------------------------------------------------------------

    /**
     * Each row is (start minutes from now, end minutes from now). Every row breaks exactly
     * one condition, except the last row which breaks both. In the rows with a good start the
     * end is the bad part, and the other way round, so we know which check did the rejecting.
     *   -1,60    start one minute in the past        (only the start is bad)
     *    0,60    start exactly now: "after now" is strict, so this is invalid (only start bad)
     *  120,119   end one minute before start         (only the end is bad)
     *  120,120   end equal to start (zero length)    (only the end is bad)
     *  -60,-30   in the past, correct order, already over (start bad)
     *  -30,-60   past start AND reversed times       (both bad)
     */
    @ParameterizedTest(name = "start={0},end={1}")
    @CsvSource({
        "-1,60",
        "0,60",
        "120,119",
        "120,120",
        "-60,-30",
        "-30,-60"
    })
    void invalidTimes(long startMin, long endMin) {
        TestFixtures f = new TestFixtures();
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(startMin), at(endMin));

        assertRejectedAsInvalidTime(f, result, savedBefore);
    }

    /**
     * One more sample from the far end of each input group (start long ago, end long before
     * start), so we do not only test the one-minute edges.
     */
    @ParameterizedTest(name = "start={0},end={1}")
    @CsvSource({
        "-1440,60",   // start a whole day ago
        "600,60"      // end ten hours before start
    })
    void invalidTimesFarFromTheEdge(long startMin, long endMin) {
        TestFixtures f = new TestFixtures();
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(startMin), at(endMin));

        assertRejectedAsInvalidTime(f, result, savedBefore);
    }

    // ---------------------------------------------------------------------------------
    // The other side of the edges: the smallest legal values must be accepted
    // ---------------------------------------------------------------------------------

    /**
     * Start one minute after now (the earliest legal start) and the shortest legal booking
     * (end one minute after start). If the service were too strict these would be refused.
     * A success means: CREATED, an id, exactly one ACTIVE booking saved with the requested
     * room, owner and times, and still no notification (creating sends none).
     */
    @ParameterizedTest(name = "start={0},end={1}")
    @CsvSource({
        "1,60",     // earliest legal start, normal length
        "120,121",  // shortest legal booking (one minute long), start well after now
        "1440,1680" // far group: starts a day from now and is four hours long
    })
    void validEdgeTimes(long startMin, long endMin) {
        TestFixtures f = new TestFixtures();

        CreateResult result = f.service.create(ROOM, OWNER, at(startMin), at(endMin));

        assertNotNull(result, "create() must return a result object");
        assertEquals(CreateStatus.CREATED, result.status(),
            "a start after now and an end after the start must be accepted");
        assertNotNull(result.bookingId(), "a created booking must have an id");
        // We do not hard-code "created-1": the id format is not part of rule R1.
        Booking expected = new Booking(result.bookingId(), ROOM, OWNER,
            at(startMin), at(endMin), BookingState.ACTIVE);
        assertEquals(List.of(expected), f.repository.all(),
            "exactly one ACTIVE booking with the requested details must be saved");
        assertEquals(List.of(), f.notifier.requests(),
            "creating a booking must not request a notification");
    }

    // ---------------------------------------------------------------------------------
    // Order of checks: times are checked BEFORE conflicts
    // ---------------------------------------------------------------------------------
    // For these tests the seeded booking is ACTIVE and in the SAME room, because only an
    // active booking in the same room can cause CONFLICT. A cancelled booking or a booking
    // in another room would never conflict, so it would prove nothing about the order.
    // Seeds are added with f.seed (not through create) so they may be in the past.

    /**
     * Control test. This proves the seed below really does cause CONFLICT when the times are
     * valid. Without it, the three "invalid, not conflict" tests could pass for the wrong reason.
     */
    @Test
    void controlSameSeedValidOverlap_isConflict() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OTHER_OWNER, 60, 180, BookingState.ACTIVE);

        CreateResult result = f.service.create(ROOM, OWNER, at(100), at(120));

        assertNotNull(result, "create() must return a result object");
        assertEquals(CreateStatus.CONFLICT, result.status(),
            "valid times overlapping an ACTIVE booking in the same room must conflict");
        assertNull(result.bookingId(), "a conflict must not hand out a booking id");
        assertEquals(List.of(b1), f.repository.all(), "a conflict must save nothing");
        assertEquals(List.of(), f.notifier.requests(), "a conflict must request no notification");
    }

    /**
     * The request starts in the past AND overlaps b1 (which is in the same room and ACTIVE).
     * Time check first, so the answer is INVALID_TIME, not CONFLICT.
     * (Day-to-day: a restaurant refuses "a table for yesterday" before checking if it is free.)
     */
    @Test
    void pastStartThatWouldConflict_isInvalidNotConflict() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OTHER_OWNER, -30, 60, BookingState.ACTIVE);
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(-10), at(30));

        assertRejectedAsInvalidTime(f, result, savedBefore);
        assertEquals(List.of(b1), f.repository.all(), "the seeded booking must be unchanged");
    }

    /**
     * The start is fine but the end is before the start, and the request would overlap b1 if
     * the times were put in the right order. INVALID_TIME must still win over CONFLICT.
     */
    @Test
    void reversedTimesThatWouldConflict_isInvalidNotConflict() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OTHER_OWNER, 60, 180, BookingState.ACTIVE);
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(150), at(90));

        assertRejectedAsInvalidTime(f, result, savedBefore);
        assertEquals(List.of(b1), f.repository.all(), "the seeded booking must be unchanged");
    }

    /**
     * A zero-length request that sits inside the booked slot of b1. Same ordering rule,
     * this time through the "end equals start" edge.
     */
    @Test
    void zeroLengthInsideActive_isInvalidNotConflict() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OTHER_OWNER, 60, 180, BookingState.ACTIVE);
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(100), at(100));

        assertRejectedAsInvalidTime(f, result, savedBefore);
        assertEquals(List.of(b1), f.repository.all(), "the seeded booking must be unchanged");
    }

    // ---------------------------------------------------------------------------------
    // Other bookings must stay unchanged
    // ---------------------------------------------------------------------------------

    /**
     * Several other bookings already exist (active, cancelled, another room, completed).
     * A rejected request must leave every one of them exactly as it was, in the same order.
     * We check the whole list and also each booking by id.
     */
    @Test
    void invalidRequest_leavesAllOtherBookingsUnchanged() {
        TestFixtures f = new TestFixtures();
        Booking activeSameRoom = f.seed("b1", ROOM, OTHER_OWNER, 300, 360, BookingState.ACTIVE);
        Booking activeOtherRoom = f.seed("b2", "R305", OTHER_OWNER, 120, 180, BookingState.ACTIVE);
        Booking cancelled = f.seed("b3", ROOM, OWNER, 200, 260, BookingState.CANCELLED);
        Booking completed = f.seed("b4", ROOM, OTHER_OWNER, -120, -60, BookingState.COMPLETED);
        List<Booking> savedBefore = f.repository.all();

        CreateResult result = f.service.create(ROOM, OWNER, at(0), at(60));

        assertRejectedAsInvalidTime(f, result, savedBefore);
        assertEquals(List.of(activeSameRoom, activeOtherRoom, cancelled, completed),
            f.repository.all(), "all four seeded bookings must be unchanged and in order");
        assertEquals(activeSameRoom, f.repository.find("b1"), "b1 must be unchanged");
        assertEquals(activeOtherRoom, f.repository.find("b2"), "b2 must be unchanged");
        assertEquals(cancelled, f.repository.find("b3"), "b3 must be unchanged");
        assertEquals(completed, f.repository.find("b4"), "b4 must be unchanged");
    }

    /**
     * A valid request next to an existing booking (b1 ends at 120 and the new one starts at
     * 120, a legal "touching" case) saves exactly one new booking and leaves b1 unchanged.
     * Only the times are R1 here; touching is the R2 owner's detail, so we just check the
     * result and the saved data.
     */
    @Test
    void validRequest_addsOneBookingAndLeavesOthersUnchanged() {
        TestFixtures f = new TestFixtures();
        Booking b1 = f.seed("b1", ROOM, OTHER_OWNER, 60, 120, BookingState.ACTIVE);

        CreateResult result = f.service.create(ROOM, OWNER, at(120), at(180));

        assertNotNull(result, "create() must return a result object");
        assertEquals(CreateStatus.CREATED, result.status(), "valid times next to b1 must be accepted");
        assertNotNull(result.bookingId(), "a created booking must have an id");
        Booking added = new Booking(result.bookingId(), ROOM, OWNER, at(120), at(180),
            BookingState.ACTIVE);
        assertEquals(List.of(b1, added), f.repository.all(),
            "b1 must be unchanged and exactly one new ACTIVE booking must be added after it");
        assertEquals(List.of(), f.notifier.requests(), "creating must not request a notification");
    }
}
