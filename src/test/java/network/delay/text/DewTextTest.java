package network.delay.text;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DewTextTest {

    @Test
    void testIsBlank() {
        assertTrue(DewText.isBlank(null));
        assertTrue(DewText.isBlank(""));
        assertTrue(DewText.isBlank("   "));
        assertFalse(DewText.isBlank("dewpoint"));
        assertFalse(DewText.isBlank("  a  "));
    }

    @Test
    void testFormatSize() {
        assertEquals("500 B", DewText.formatSize(500));
        assertEquals("1.0 KB", DewText.formatSize(1024));
        assertEquals("1.5 MB", DewText.formatSize(1024 * 1024 + 512 * 1024));
        assertEquals("2.0 GB", DewText.formatSize(2L * 1024 * 1024 * 1024));
    }

    @Test
    void testSubstringBetween() {
        assertEquals("middle", DewText.substringBetween("left[middle]right", "[", "]"));
        assertEquals("value", DewText.substringBetween("key=value;", "=", ";"));
        assertNull(DewText.substringBetween("no brackets here", "[", "]"));
        assertNull(DewText.substringBetween("only[one", "[", "]"));
        assertNull(DewText.substringBetween(null, "(", ")"));
    }

    @Test
    void testIsNumeric() {
        assertTrue(DewText.isNumeric("123"));
        assertTrue(DewText.isNumeric("0"));
        assertFalse(DewText.isNumeric("123a"));
        assertFalse(DewText.isNumeric("12.3")); // Only digits, not decimals
        assertFalse(DewText.isNumeric(""));
        assertFalse(DewText.isNumeric(null));
    }
}