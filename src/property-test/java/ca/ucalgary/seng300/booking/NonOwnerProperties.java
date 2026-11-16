package ca.ucalgary.seng300.booking;
import java.util.List;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.constraints.IntRange;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Part 6: Option A - Generate inputs
 *
 * Checks that a non-owner cannot change a booking by attempting to cancel.
 * Generates different owners, callers, and states.
 * Checks rejection and unchanged storage.
 * Runs 100 test cases with random seed 67.
 * Uses jqwik for PBT (property-based testing) 
 * https://jqwik.net/
 * GenAI used for jqwik help.
 */


public class NonOwnerProperties {
    @Property(tries = 100, seed = "67", generation = GenerationMode.RANDOMIZED)
        
   void nonOwnerTest(@ForAll @IntRange (min = 1, max = 1000) int idNum, @ForAll BookingState state){
   

   // Owners and callers have unique IDs
   String owner = "owner" + idNum;
   String caller = "caller" + idNum;

   // New test Fixture for fresh storage
   TestFixtures f = new TestFixtures();

   // Create bookings
   f.seed("b1", "R201", owner, 30, 60, state);
   f.seed("b2", "R202", "test-owner", 60, 120, BookingState.ACTIVE);

   // Snapshot to keep track of current data
   List<Booking> before = f.repository.all();

   //Try to cancel booking with non-owner
   CancelStatus result = f.service.cancel("b1", caller);
   assertEquals(CancelStatus.DENIED, result, "non-owner cannot cancel booking - DENIED");
   assertEquals(before, f.repository.all(), "non-owner cannot change any stored booking");
   assertEquals(List.of(), f.notifier.requests(), "denied cancellation has no notifications");

   }

}
