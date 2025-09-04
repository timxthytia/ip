package tim.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Parser} class.
 * Verifies that date and date-time parsing behaves as expected,
 * including acceptance of valid formats and rejection of invalid ones.
 */
public class ParserTest {

    /**
     * Tests that a strict date-only input string is correctly parsed
     * into a LocalDateTime at midnight.
     */
    @Test
    @DisplayName("Parses strict date-only input")
    void parseStrictDate_only() {
        LocalDateTime got = Parser.parseStrictDateOrDateTime("2019-10-15");
        assertEquals(LocalDateTime.parse("2019-10-15T00:00:00"), got);
    }

    /**
     * Tests that a strict date-time input string in yyyy-MM-dd HHmm format
     * is correctly parsed into a LocalDateTime with the given hour and minute.
     */
    @Test
    @DisplayName("Parses strict date-time input")
    void parseStrictDate_time() {
        LocalDateTime got = Parser.parseStrictDateOrDateTime("2019-10-15 1800");
        assertEquals(LocalDateTime.parse("2019-10-15T18:00:00"), got);
    }

    /**
     * Tests that invalid date and date-time input strings are rejected
     * by throwing an IllegalArgumentException.
     */
    @Test
    @DisplayName("Rejects invalid date/time formats")
    void parseStrictDateOrDateTime_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parseStrictDateOrDateTime("15/10/2019"));
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parseStrictDateOrDateTime("2019-10-15 18:00")); // colon not allowed in HHmm
        assertThrows(IllegalArgumentException.class, () ->
                Parser.parseStrictDateOrDateTime("not-a-date"));
    }
}
