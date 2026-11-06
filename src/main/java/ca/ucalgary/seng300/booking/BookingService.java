package ca.ucalgary.seng300.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/** All inputs follow the domain in the assignment. Calls are sequential. */
public class BookingService {
    private final BookingRepository repository;
    private final Notifier notifier;
    private final Clock clock;
    private final Supplier<String> nextId;

    /** nextId must supply a fresh ID that is not already in the repository. */
    public BookingService(BookingRepository repository, Notifier notifier,
                          Clock clock, Supplier<String> nextId) {
        this.repository = repository;
        this.notifier = notifier;
        this.clock = clock;
        this.nextId = nextId;
    }

    public CreateResult create(String roomId, String ownerId, Instant start, Instant end) {
        Instant now = clock.instant();
        if (!start.isAfter(now) && !end.isAfter(start)) {
            return new CreateResult(CreateStatus.INVALID_TIME, null);
        }
        for (Booking existing : repository.all()) {
            if (existing.state() == BookingState.ACTIVE
                    && existing.roomId().equals(roomId)
                    && start.isBefore(existing.end())
                    && existing.start().isBefore(end)) {
                return new CreateResult(CreateStatus.CONFLICT, null);
            }
        }
        Booking booking = new Booking(nextId.get(), roomId, ownerId, start, end, BookingState.ACTIVE);
        repository.save(booking);
        return new CreateResult(CreateStatus.CREATED, booking.id());
    }

    public CancelStatus cancel(String bookingId, String callerId) {
        Booking booking = repository.find(bookingId);
        if (!booking.ownerId().equals(callerId)) {
            return CancelStatus.DENIED;
        }
        if (booking.state() != BookingState.ACTIVE) {
            return CancelStatus.INACTIVE;
        }
        long minutes = Duration.between(clock.instant(), booking.start()).toMinutes();
        if (minutes < 120) {
            return CancelStatus.TOO_LATE;
        }
        Booking cancelled = booking.withState(BookingState.CANCELLED);
        repository.save(cancelled);
        notifier.cancellationRequested(cancelled);
        return CancelStatus.CANCELLED;
    }
}
