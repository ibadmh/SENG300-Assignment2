package ca.ucalgary.seng300.booking;
import java.util.List;
public interface BookingRepository {
    Booking find(String id);
    List<Booking> all();
    /** Save or replace this ID. A RepositoryException must occur before any change. */
    void save(Booking booking);
}
