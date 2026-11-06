package ca.ucalgary.seng300.booking;
public interface Notifier {
    /** Request cancellation notification. Actual delivery is outside this assignment. */
    void cancellationRequested(Booking booking);
}
