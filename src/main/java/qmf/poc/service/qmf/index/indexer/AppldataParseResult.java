package qmf.poc.service.qmf.index.indexer;

import java.util.List;
import java.util.Map;

public class AppldataParseResult {
    final String[] items;
    final List<String> modifiableItems;
    final List<String> nonmodifiableItems;
    public final List<String> body;
    final Map<String, Object> properties;

    public AppldataParseResult(String[] items, List<String> modifiableItems, List<String> nonmodifiableItems, List<String> body, Map<String, Object> properties) {
        this.items = items;
        this.modifiableItems = modifiableItems;
        this.nonmodifiableItems = nonmodifiableItems;
        this.body = body;
        this.properties = properties;
    }
}
