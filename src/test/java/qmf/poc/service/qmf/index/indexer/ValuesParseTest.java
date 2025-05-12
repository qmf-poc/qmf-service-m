package qmf.poc.service.qmf.index.indexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValuesParseTest {
    @Test
    public void testIntThrowsIfLong() {
        // Arrange
        final String str = "12345678901234567890";
        // Act & Assert
        assertThrows(NumberFormatException.class, () -> Integer.parseInt(str));
    }
    @Test
    public void testLongThrowsIfDouble() {
        // Arrange
        final String str = "12345678901234567890.1234567890";
        // Act & Assert
        assertThrows(NumberFormatException.class, () -> Long.parseLong(str));
    }
}
