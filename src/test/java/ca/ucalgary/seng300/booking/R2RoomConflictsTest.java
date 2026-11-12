package ca.ucalgary.seng300.booking;
import java.util.List;
import org.junit.jupiter.api.Test;
import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for R2: Room Conflicts
 * Checks overlapping, touching, and separate intervals, different rooms,
 * and cancelled or completed bookings.
 *
 * Verifies return status, saved booking details, and unchanged existing bookings
 */
class R2RoomConflictsTest {

    @Test
    void touchingBefore_isCreated() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 30, 60,
                CreateStatus.CREATED);
    }

    @Test
    void touchingAfter_isCreated() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 120, 180,
                CreateStatus.CREATED);
    }

    @Test
    void oneMinStartOverlap_isConflict() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 30, 61,
                CreateStatus.CONFLICT);
    }

    @Test
    void oneMinEndOverlap_isConflict() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 119, 180,
                CreateStatus.CONFLICT);
    }

    @Test
    void insideExisting_isConflict() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 70, 110,
                CreateStatus.CONFLICT);
    }

    @Test
    void coversExisting_isConflict() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 30, 150,
                CreateStatus.CONFLICT);
    }

    @Test
    void identicalInterval_isConflict() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 60, 120,
                CreateStatus.CONFLICT);
    }

    @Test
    void separate_isCreated() {
        checkRoomConflict(BookingState.ACTIVE, "R204", 200, 260,
                CreateStatus.CREATED);
    }

    @Test
    void sameTimeDifferentRoom_isCreated() {
        checkRoomConflict(BookingState.ACTIVE, "R305", 60, 120,
                CreateStatus.CREATED);
    }

    @Test
    void cancelledExistingOverlap_isCreated() {
        checkRoomConflict(BookingState.CANCELLED, "R204", 70, 110,
                CreateStatus.CREATED);
    }

    @Test
    void completedExistingOverlap_isCreated() {
        checkRoomConflict(BookingState.COMPLETED, "R204", 70, 110,
                CreateStatus.CREATED);
    }

    /**
     * Sets up one planned case and checks its response and saved data
     * Each call creates a fresh fixture
     *
     * @param existingState state of the existing booking
     * @param requestedRoom room requested for the new booking
     * @param start requested start in min after the fixed current time
     * @param end requested end in min after the fixed current time
     * @param expectedStatus expected creation status according to R2
     */
    private void checkRoomConflict(
            BookingState existingState,
            String requestedRoom,
            long start,
            long end,
            CreateStatus expectedStatus) {

        //Storage, recording notifier and a fixed clock for each case
        TestFixtures f = new TestFixtures();

        //Add the existing booking
        f.seed("b1", "R204", "Alice", 60, 120, existingState);

        //Save current bookings to compare
        List<Booking> before = f.repository.all();

        //Request new booking
        CreateResult result = f.service.create(
                requestedRoom, "Bob", at(start), at(end));

        assertEquals(expectedStatus, result.status());

        if (expectedStatus == CreateStatus.CONFLICT) {
            //Rejection must return no ID and leave storage unchanged
            assertNull(result.bookingId());
            assertEquals(before, f.repository.all(),
                    "conflict must leave storage unchanged");
        } else {
            //Creation must return an ID and add exactly one booking
            assertNotNull(result.bookingId());
            assertEquals(before.size() + 1, f.repository.all().size(),
                    "creation must add exactly one booking");

            //Build the expected saved booking
            Booking expectedBooking = new Booking(
                    result.bookingId(),
                    requestedRoom,
                    "Bob",
                    at(start),
                    at(end),
                    BookingState.ACTIVE);

            //Check all saved fields
            assertEquals(expectedBooking,
                    f.repository.find(result.bookingId()),
                    "saved booking must match the request");

            //Creation must preserve every existing booking
            for (Booking existing : before) {
                assertEquals(existing, f.repository.find(existing.id()),
                        "creation must leave existing bookings unchanged");
            }
        }
    }
}