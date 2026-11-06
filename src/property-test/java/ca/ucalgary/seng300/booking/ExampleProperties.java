package ca.ucalgary.seng300.booking;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.constraints.IntRange;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Demonstrates the tool only. Replace or supplement this with your booking property. */
class ExampleProperties {
    @Property(tries = 100, seed = "3002026", generation = GenerationMode.RANDOMIZED)
    void addingPositiveMinutesMovesTimeForward(@ForAll @IntRange(min = 1, max = 1000) int minutes) {
        assertTrue(TestFixtures.at(minutes).isAfter(TestFixtures.NOW));
    }
}
