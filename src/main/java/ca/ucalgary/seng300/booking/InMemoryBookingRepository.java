package ca.ucalgary.seng300.booking;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    @Override public Booking find(String id) {
        Booking booking = bookings.get(id);
        if (booking == null) throw new IllegalArgumentException("Unknown booking: " + id);
        return booking;
    }
    @Override public List<Booking> all() { return List.copyOf(bookings.values()); }
    @Override public void save(Booking booking) { bookings.put(booking.id(), booking); }
}
