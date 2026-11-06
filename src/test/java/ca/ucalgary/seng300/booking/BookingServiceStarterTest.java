package ca.ucalgary.seng300.booking;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static ca.ucalgary.seng300.booking.TestFixtures.*;

/** Passing examples. This suite intentionally leaves important cases unchecked. */
class BookingServiceStarterTest {
    @Test void createsFutureBooking() {
        TestFixtures f = new TestFixtures();
        CreateResult result = f.service.create("R204", "student-a", at(180), at(240));
        assertEquals(CreateStatus.CREATED, result.status());
        assertNotNull(result.bookingId());
    }
    @Test void ownerCanCancelEarly() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", "R204", "student-a", 180, 240, BookingState.ACTIVE);
        assertEquals(CancelStatus.CANCELLED, f.service.cancel("b1", "student-a"));
    }
    @Test void otherStudentIsDenied() {
        TestFixtures f = new TestFixtures();
        f.seed("b1", "R204", "student-a", 180, 240, BookingState.ACTIVE);
        assertEquals(CancelStatus.DENIED, f.service.cancel("b1", "student-b"));
    }
}
