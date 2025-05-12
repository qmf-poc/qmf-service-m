package qmf.poc.service.qmf.index.indexer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static qmf.poc.service.qmf.index.Fixtures.*;

public class AppldataParserTest {


    @ParameterizedTest
    @ValueSource(strings = {proc2, query2, queryDoc2, queryDoc1, proc1, query1, proc1withTail,})
    public void testParser(String appldata) {
        // Arrange
        final AppldataParseResult r = AppldataParser.parse(appldata);
        // Act
        // Assert
        assertEquals(
                r.items.length,
                r.modifiableItems.size() +
                        r.nonmodifiableItems.size() +
                        1 +
                        Arrays.stream(r.items).filter(x -> x.startsWith("+")).count());
    }

}
