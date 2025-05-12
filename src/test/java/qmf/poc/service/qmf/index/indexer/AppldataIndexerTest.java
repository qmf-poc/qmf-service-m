package qmf.poc.service.qmf.index.indexer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static qmf.poc.service.qmf.index.Fixtures.*;

public class AppldataIndexerTest {
    public record TestData(
            String type,
            String appldata,
            List<String> expectedIndex
    ) {
    }

    private static Stream<TestData> provideTestData() {
        return Stream.of(
                new TestData("QUERY", query1, List.of(
                        "SALES_DATE",
                        "SALES_PERSON",
                        "DB2INST1.SALES",
                        "SALES",
                        "REGION"
                )),
                new TestData("QUERY", query2, List.of(
                        "PRODUCT_COLOR_CS",
                        "PRODUCT_COLOR_ES",
                        "PRODUCT_COLOR_TC",
                        "PRODUCT_COLOR_EN",
                        "PRODUCT_COLOR_IT",
                        "PRODUCT_COLOR_KO",
                        "PRODUCT_COLOR_MS",
                        "PRODUCT_COLOR_EL",
                        "PRODUCT_COLOR_SV",
                        "PRODUCT_COLOR_ID",
                        "PRODUCT_COLOR_SC",
                        "PRODUCT_COLOR_FR",
                        "PRODUCT_COLOR_HU",
                        "PRODUCT_COLOR_PT",
                        "PRODUCT_COLOR_DE",
                        "PRODUCT_COLOR_NO",
                        "PRODUCT_COLOR_FI",
                        "PRODUCT_COLOR_RU",
                        "PRODUCT_COLOR_PL",
                        "PRODUCT_COLOR_DA",
                        "PRODUCT_COLOR_NL",
                        "GOSALES.PRODUCT_COLOR_LOOKUP",
                        "PRODUCT_COLOR_TH",
                        "PRODUCT_COLOR_CODE",
                        "PRODUCT_COLOR_JA"
                )),
                new TestData("PROC", proc2, List.of("DB2ADMIN.GETQUERIES")),
                new TestData("PROC", proc1, List.of("DB2INST1.QUERY_SALES")),
                new TestData("PROC", proc1withTail, List.of("DB2INST1.QUERY_SALES"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testIndexer(@NotNull TestData testData) {
        // Arrange && Act
        final List<String> index = AppldataIndexer.index(testData.type, testData.appldata);
        // Assert
        assertEquals(testData.expectedIndex, index);
    }

}

