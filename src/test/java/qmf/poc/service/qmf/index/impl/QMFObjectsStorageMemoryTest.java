package qmf.poc.service.qmf.index.impl;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.index.models.QMFObjectDocument;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static qmf.poc.service.qmf.index.Fixtures.*;

@ExtendWith(VertxExtension.class)
public class QMFObjectsStorageMemoryTest {
    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of((List.of(
                        new QMFObjectCatalog(
                                "owner1",
                                "name1",
                                "QUERY",
                                "foo",
                                1,
                                "bar",
                                "baz",
                                "qux",
                                "quux",
                                "corge",
                                query1,
                                "garply"),
                        new QMFObjectCatalog(
                                "owner2",
                                "name2",
                                "QUERY",
                                "foo",
                                1,
                                "bar",
                                "baz",
                                "qux",
                                "quux",
                                "corge",
                                query2,
                                "garply"),
                        new QMFObjectCatalog(
                                "owner1",
                                "name3",
                                "PROC",
                                "foo",
                                1,
                                "bar",
                                "baz",
                                "qux",
                                "quux",
                                "corge",
                                proc1,
                                "garply"),
                        new QMFObjectCatalog(
                                "owner2",
                                "name4",
                                "PROC",
                                "foo",
                                1,
                                "bar",
                                "baz",
                                "qux",
                                "quux",
                                "corge",
                                proc2,
                                "garply")
                ))));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testLoad(@NotNull List<QMFObjectCatalog> qmfObjects, Vertx vertx, @NotNull VertxTestContext testContext) {
        // Arrange
        final QMFObjectsStorageMemory storage = new QMFObjectsStorageMemory();
        // Act
        storage
                .load("agentId", qmfObjects)
                .compose(v -> storage.queryObjects("", 0))
                .onComplete(testContext.succeeding(result -> {
                    List<QMFObjectDocument> documents = result.toList();
                    testContext.verify(() -> {
                        assertEquals(qmfObjects.size(), documents.size());
                        testContext.completeNow();
                    });
                }));
        // Assert
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testGetById(@NotNull List<QMFObjectCatalog> qmfObjects, Vertx vertx, @NotNull VertxTestContext testContext) {
        // Arrange
        final QMFObjectsStorageMemory storage = new QMFObjectsStorageMemory();
        // Act
        storage
                .load("agentId", qmfObjects)
                .compose(v -> storage.getObject("agentId-owner1-name3-PROC"))
                .onComplete(
                        testContext.succeeding(result -> {
                            testContext.verify(() -> {
                                assertEquals("name3", result.name());
                                assertEquals("owner1", result.owner());
                                testContext.completeNow();
                            });
                        })
                );
        // Assert
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testGetByIdFailed(@NotNull List<QMFObjectCatalog> qmfObjects, Vertx vertx, @NotNull VertxTestContext testContext) {
        // Arrange
        final QMFObjectsStorageMemory storage = new QMFObjectsStorageMemory();
        // Act
        storage
                .load("agentId", qmfObjects)
                .compose(v -> storage.getObject("foo-agentId-owner1-name3-PROC"))
                .onComplete(
                        testContext.failing(result -> {
                            testContext.verify(() -> {
                                assertEquals("Object not found for ID: foo-agentId-owner1-name3-PROC", result.getMessage());
                                testContext.completeNow();
                            });
                        })
                );
        // Assert
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testSearchQueryByIndex(@NotNull List<QMFObjectCatalog> qmfObjects, Vertx vertx, @NotNull VertxTestContext testContext) {
        // Arrange
        final QMFObjectsStorageMemory storage = new QMFObjectsStorageMemory();
        // Act
        storage
                .load("agentId", qmfObjects)
                .compose(v -> storage.queryObjects("ALES_PERSO",0))
                .onComplete(
                        testContext.succeeding(stream -> {
                            List<QMFObjectDocument> result = stream.toList();
                            testContext.verify(() -> {
                                assertEquals(1, result.size());
                                assertEquals("name1", result.getFirst().name());
                                assertEquals("owner1", result.getFirst().owner());
                                testContext.completeNow();
                            });
                        })
                );
        // Assert
    }
}
