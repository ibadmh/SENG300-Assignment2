package ca.ucalgary.seng300.booking;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Each test should construct its own fixture. This is support code, not a test. */
public class TestFixtures {
    public static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");
    public final InMemoryBookingRepository repository = new InMemoryBookingRepository();
    public final RecordingNotifier notifier = new RecordingNotifier();
    private final AtomicInteger ids = new AtomicInteger();
    public final BookingService service = new BookingService(repository, notifier,
        Clock.fixed(NOW, ZoneOffset.UTC), () -> "created-" + ids.incrementAndGet());

    public static Instant at(long minutesFromNow) { return NOW.plus(minutesFromNow, ChronoUnit.MINUTES); }
    public Booking seed(String id, String room, String owner, long start, long end, BookingState state) {
        Booking booking = new Booking(id, room, owner, at(start), at(end), state);
        repository.save(booking);
        return booking;
    }
    public static class RecordingNotifier implements Notifier {
        private final List<Booking> requests = new ArrayList<>();
        @Override public void cancellationRequested(Booking booking) { requests.add(booking); }
        public List<Booking> requests() { return List.copyOf(requests); }
    }
}
