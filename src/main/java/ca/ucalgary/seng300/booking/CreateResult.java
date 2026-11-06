package ca.ucalgary.seng300.booking;
/** bookingId is present only for CREATED. */
public record CreateResult(CreateStatus status, String bookingId) {}
