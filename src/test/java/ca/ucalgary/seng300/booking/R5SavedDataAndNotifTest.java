package ca.ucalgary.seng300.booking;

import static ca.ucalgary.seng300.booking.TestFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class R5SavedDataAndNotifTest{

    private TestFixtures fixtures;

    private static final String ROOM = "R204";
    private static final String OWNER = "student-a";
    private static final String NON_OWNER = "student-b";

    // from R4CancellationTimeTest
    private static Booking seedOther(TestFixtures f) {
        return f.seed("b2", "R305", "student-c", 300, 360, BookingState.ACTIVE);
    }

    private static void assertReturn(CancelStatus expected, CancelStatus actual){
        assertEquals(expected, actual, "CancelStatus doesn't match expected");
    }

    private static void assertChangedSave(Booking b1, Booking b2, TestFixtures f){
        Booking b1After = b1.withState(BookingState.CANCELLED);

        assertEquals(b1After, f.repository.find("b1"), "b1 wasn't saved");
        assertEquals(List.of(b1After, b2), f.repository.all(), "Save sate didn't change");
    }

    private static void assertUnchangedSave(List<Booking> expected, TestFixtures f){
        assertEquals(expected, f.repository.all(), "Save state changed");
    }

    private static void assertNotifies(Booking b1, TestFixtures f){
        Booking b1After = b1.withState(BookingState.CANCELLED);

        assertEquals(List.of(b1After), f.notifier.requests(), "Incorrect notifications");
    }

    private static void assertDoesntNotify (TestFixtures f){
        assertEquals(List.of(), f.notifier.requests(), "Notified unexpectedly");
    }

    @BeforeEach
    void newFixtures(){
        fixtures = new TestFixtures();
    }

    // Successfull Cancellation
    @Test
    void successfullCancellationReturnsSavesNotifies(){
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.ACTIVE);
        Booking b2 = seedOther(fixtures);

        Booking b1After = b1.withState(BookingState.CANCELLED);
        // returns
        assertReturn(CancelStatus.CANCELLED, fixtures.service.cancel("b1", OWNER));
        // saves
        assertChangedSave(b1, b2,  fixtures);
        // notifies once
        assertNotifies(b1, fixtures);
    }

    // Inactive Request
    @Test
    void InactiveRequestDoesNothing(){
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 30, 90, BookingState.CANCELLED);
        Booking b2 = seedOther(fixtures);

        // returns
        assertReturn(CancelStatus.INACTIVE, fixtures.service.cancel("b1", OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1, b2), fixtures);
        // doesn't notify
        assertDoesntNotify(fixtures);
    }

    // Denied Request
    @Test
    void DeniedRequestDoesNothing(){
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.CANCELLED);
        Booking b2 = seedOther(fixtures);

        // returns
        assertReturn(CancelStatus.DENIED, fixtures.service.cancel("b1", NON_OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1, b2), fixtures);
        // doesn't notify
        assertDoesntNotify(fixtures);
    }

    // Too Late Request
    @Test
    void TooLateRequestDoesNothing(){
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 0, 30, BookingState.CANCELLED);
        Booking b2 = seedOther(fixtures);

        // returns
        assertReturn(CancelStatus.TOO_LATE, fixtures.service.cancel("b1", OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1, b2), fixtures);
        // doesn't notify
        assertDoesntNotify(fixtures);
    }

    // Booking Created
    @Test
    void BookingCreationDoesntNotify(){
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.ACTIVE);
        // doesn't notify
        assertDoesntNotify(fixtures);
    }

    // Action Sequence D1 -> D3
    @Test
    void SequenceD1D3(){
        // D1
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.ACTIVE);
        Booking b2 = seedOther(fixtures);

        Booking b1After = b1.withState(BookingState.CANCELLED);
        // returns
        assertReturn(CancelStatus.CANCELLED, fixtures.service.cancel("b1", OWNER));
        // saves
        assertChangedSave(b1, b2,  fixtures);
        // notifies once
        assertNotifies(b1, fixtures);

        // D3
        // returns
        assertReturn(CancelStatus.INACTIVE, fixtures.service.cancel("b1", OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1After, b2), fixtures);
        // notifies once
        assertNotifies(b1, fixtures);

    }
    // Action Sequence D1 -> D4
    @Test
    void SequenceD1D4(){
        // D1
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.ACTIVE);
        Booking b2 = seedOther(fixtures);

        Booking b1After = b1.withState(BookingState.CANCELLED);
        // returns
        assertReturn(CancelStatus.CANCELLED, fixtures.service.cancel("b1", OWNER));
        // saves
        assertChangedSave(b1, b2,  fixtures);
        // notifies once
        assertNotifies(b1, fixtures);

        // D4
        b1 = fixtures.seed("b1", ROOM, OWNER, 0, 60, BookingState.CANCELLED);
        b1After = b1.withState(BookingState.CANCELLED);
        // returns
        assertReturn(CancelStatus.INACTIVE, fixtures.service.cancel("b1", OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1After, b2), fixtures);
    }
    // Action Sequence D1 -> D9
    @Test
    void SequenceD1D9(){
        // D1
        Booking b1 = fixtures.seed("b1", ROOM, OWNER, 1440, 1500, BookingState.ACTIVE);
        Booking b2 = seedOther(fixtures);

        Booking b1After = b1.withState(BookingState.CANCELLED);
        // returns
        assertReturn(CancelStatus.CANCELLED, fixtures.service.cancel("b1", OWNER));
        // saves
        assertChangedSave(b1, b2,  fixtures);
        // notifies once
        assertNotifies(b1, fixtures);

        // D9
        // returns
        assertReturn(CancelStatus.DENIED, fixtures.service.cancel("b1", NON_OWNER));
        // doesn't save
        assertUnchangedSave(List.of(b1After, b2), fixtures);
        // notifies once
        assertNotifies(b1, fixtures);
    }
}