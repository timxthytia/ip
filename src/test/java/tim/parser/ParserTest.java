package tim.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @DisplayName("Parses strict date-only and date-time inputs")

    void parseStrictDateOrDateTime_valid(String input, String expectedIso) {
        LocalDateTime got = Parser.parseStrictDateOrDateTime(input);
        assertEquals(LocalDateTime.parse(expectedIso), got);
    }

    @Test
    @DisplayName("Rejects invalid date/time formats")
    void parseStrictDateOrDateTime_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parseStrictDateOrDateTime("15/10/2019"));
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parseStrictDateOrDateTime("2019-10-15 18:00")); // colon not allowed in HHmm
        assertThrows(IllegalArgumentException.class,
                () -> Parser.parseStrictDateOrDateTime("not-a-date"));
    }
}