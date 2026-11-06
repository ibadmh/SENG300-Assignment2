package ca.ucalgary.seng300.booking;
import java.time.Instant;
/** Immutable stored value. Tests may construct historical or inactive fixtures. */
public record Booking(String id, String roomId, String ownerId,
                      Instant start, Instant end, BookingState state) {
    public Booking withState(BookingState next) {
        return new Booking(id, roomId, ownerId, start, end, next);
    }
}
