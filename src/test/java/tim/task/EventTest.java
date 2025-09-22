package tim.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Event}.
 * <p>
 * These tests verify constructor validation (nulls and end-before-start),
 * getters and optional accessors, formatting via {@link Event#toStorageString()}
 * and {@link Event#toString()}, and values via {@link Event#equals(Object)}
 * and {@link Event#hashCode()}.
 * </p>
 */
public class EventTest {

    /**
     * Verifies that constructing an {@link Event} with a null start time
     * throws an {@link NullPointerException}.
     */
    @Test
    void constructor_nullStart_throwsNpe() {
        LocalDateTime end = LocalDateTime.parse("2025-09-21T10:00");
        assertThrows(NullPointerException.class, () -> new Event("desc", null, end),
                "Null start should throw NPE");
    }

    /**
     * Verifies that constructing an {@link Event} with a null end time
     * throws an {@link NullPointerException}.
     */
    @Test
    void constructor_nullEnd_throwsNpe() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00");
        assertThrows(NullPointerException.class, () -> new Event("desc", start, null),
                "Null end should throw NPE");
    }

    /**
     * Verifies that constructing an {@link Event} with an end time before the start time
     * throws an {@link IllegalArgumentException}.
     */
    @Test
    void constructor_endBeforeStart_throwsIae() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T10:00");
        LocalDateTime end = LocalDateTime.parse("2025-09-21T09:00");
        assertThrows(IllegalArgumentException.class, () -> new Event("desc", start, end),
                "end < start should throw IAE");
    }

    /**
     * Verifies that {@link Event#getStart()} and {@link Event#getEnd()} return
     * the exact values supplied to the constructor.
     */
    @Test
    void getters_returnSuppliedTimes() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00");
        LocalDateTime end = LocalDateTime.parse("2025-09-21T10:00");
        Event ev = new Event("standup", start, end);
        assertSame(start, ev.getStart());
        assertSame(end, ev.getEnd());
    }

    /**
     * Verifies that the optional accessors {@link Event#getPrimaryTriggerTime()} and
     * {@link Event#getEndTime()} contain the start and end respectively.
     */
    @Test
    void optionalAccessors_wrapStartAndEnd() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00");
        LocalDateTime end = LocalDateTime.parse("2025-09-21T10:00");
        Event ev = new Event("standup", start, end);
        assertTrue(ev.getPrimaryTriggerTime().isPresent());
        assertEquals(start, ev.getPrimaryTriggerTime().get());
        assertTrue(ev.getEndTime().isPresent());
        assertEquals(end, ev.getEndTime().get());
    }

    /**
     * Verifies the storage format of {@link Event#toStorageString()} matches:
     * <pre>{@code E | {0/1} | {description} | {startIso} to {endIso}}</pre>
     * where start/end are ISO-8601 local date-times.
     */
    @Test
    void toStorageString_matchesSpecifiedFormat() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00:00");
        LocalDateTime end = LocalDateTime.parse("2025-09-21T10:30:00");
        Event ev = new Event("sprint review", start, end);

        String s = ev.toStorageString();

        // Example expected structure:
        // E | 0 | sprint review | 2025-09-21T09:00:00 to 2025-09-21T10:30:00
        assertTrue(s.startsWith("E | "), "Should start with type code E and separator");
        assertTrue(s.contains(" | sprint review | "), "Should contain description delimited by ' | '");
        assertTrue(s.contains(" to "), "Should contain ' to ' as range separator");
        assertTrue(s.contains("2025-09-21T09:00:00"), "Start should be ISO_LOCAL_DATE_TIME");
        assertTrue(s.contains("2025-09-21T10:30:00"), "End should be ISO_LOCAL_DATE_TIME");
    }

    /**
     * Verifies the display format of {@link Event#toString()} uses the
     * <pre>{@code [E]{Task#toString()} (from: MMM dd yyyy HH:mm to: MMM dd yyyy HH:mm)}</pre>
     * pattern when start and end differ.
     */
    @Test
    void toString_usesFromTo_whenTimesDiffer() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00");
        LocalDateTime end = LocalDateTime.parse("2025-09-21T10:00");
        Event ev = new Event("standup", start, end);

        String shown = ev.toString();
        assertTrue(shown.startsWith("[E]"), "Should include [E] prefix");
        assertTrue(shown.contains("(from:"), "Should show 'from:' when times differ");
        assertTrue(shown.contains("to:"), "Should show 'to:' when times differ");
        // Spot-check formatted month pattern (e.g., "Sep 21 2025 09:00")
        assertTrue(shown.matches(".*\\((from:|on:) .*\\).*"), "Should include formatted date(s)");
    }

    /**
     * Verifies the display format of {@link Event#toString()} uses the
     * <pre>{@code (on: MMM dd yyyy HH:mm)}</pre> pattern when start equals end.
     */
    @Test
    void toString_usesOn_whenTimesEqual() {
        LocalDateTime start = LocalDateTime.parse("2025-09-21T09:00");
        Event ev = new Event("one-off", start, start);

        String shown = ev.toString();
        assertTrue(shown.contains("(on:"), "Should show 'on:' when start == end");
        assertFalse(shown.contains("(from:"), "Should not show 'from:' when start == end");
    }

    /**
     * Verifies that {@link Event#equals(Object)} considers description, start, end,
     * and completion state; and that {@link Event#hashCode()} is consistent.
     */
    @Test
    void equalsAndHashCode_considerAllRelevantFields() {
        LocalDateTime s1 = LocalDateTime.parse("2025-09-21T09:00");
        LocalDateTime e1 = LocalDateTime.parse("2025-09-21T10:00");
        Event a = new Event("standup", s1, e1);
        Event b = new Event("standup", s1, e1);

        assertEquals(a, b, "Same description/start/end and default completion should be equal");
        assertEquals(a.hashCode(), b.hashCode(), "Equal objects must have equal hashCodes");

        // Change completion state to ensure inequality
        a.markAsDone();
        assertNotEquals(a, b, "Different completion state should result in inequality");
    }
}
