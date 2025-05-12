package qmf.poc.service.qmf.index.indexer;

import com.google.gson.Gson;

import java.util.*;

class AppldataParser {
    private static class HT<H> {
        public final H head;
        public final String[] tail;

        public HT(H head, String[] tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    private static final String JsonObjectKey = "JsonObject";

    private static <T> HT<List<T>> ht(List<T> head, String[] items) {
        return new HT<>(head, Arrays.copyOfRange(items, head.size() + 1, items.length));
    }

    private static final String NonmodifiablePrefix = "Warning=Do not modify the codes below.";
    private final static String AppldataDelimiter = "--%";
    private static final String JsonObjectPrefix = JsonObjectKey + "=";
    private static final String JsonObjectContinuation = "+";

    private static HT<List<String>> parseModifiableItems(String[] items) {
        final List<String> result = new LinkedList<>();
        for (String item : items) {
            if (item.startsWith(NonmodifiablePrefix)) {
                return ht(result, items);
            } else {
                result.add(item.trim());
            }
        }
        return new HT<>(result, new String[0]);
    }

    private static HT<String> parseJsonObject(String[] items) {
        final String init = items[0].substring(JsonObjectPrefix.length());
        final StringBuilder jsonObject = new StringBuilder(init);
        for (int i = 1; i < items.length; i++) {
            if (items[i].startsWith(JsonObjectContinuation)) {
                jsonObject.append(items[i].substring(JsonObjectContinuation.length()).trim());
            } else {
                return new HT<>(new String(Base64.getDecoder().decode(jsonObject.toString())), Arrays.copyOfRange(items, i, items.length));
            }
        }
        return new HT<>(new String(Base64.getDecoder().decode(jsonObject.toString())), new String[0]);
    }

    private static List<String> parseNonModifiableItems(String[] items) {
        final List<String> result = new LinkedList<>();
        for (String item : items) {
            if (item.startsWith(JsonObjectPrefix)) {
                final HT<String> jsonObject = parseJsonObject(Arrays.copyOfRange(items, result.size(), items.length));
                result.add(JsonObjectPrefix + jsonObject.head);
                final List<String> rest = parseNonModifiableItems(jsonObject.tail);
                result.addAll(rest);
                return result;
            } else {
                result.add(item.trim());
            }
        }
        return result;
    }

    public static AppldataParseResult parse(String appldata) {
        final String[] items = appldata.split(AppldataDelimiter);
        final HT<List<String>> resultModifiable = parseModifiableItems(items);
        final List<String> modifiableItems = resultModifiable.head;
        final List<String> nonModifiableItems = parseNonModifiableItems(resultModifiable.tail);

        final Map<String, Object> properties = new HashMap<>();
        for (String item : nonModifiableItems) {
            final String[] parts = item.split("=", 2);
            if (parts.length == 2) {
                final String key = parts[0].trim();
                final String value = parts[1].trim();
                if (key.equals(JsonObjectKey)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> obj = new Gson().fromJson(value, Map.class);
                    properties.put(key, obj);
                } else {
                    try {
                        int i = Integer.parseInt(value);
                        properties.put(key, i);
                    } catch (NumberFormatException ignored) {
                        try {
                            long l = Long.parseLong(value);
                            properties.put(key, l);
                        } catch (NumberFormatException ignored2) {
                            try {
                                double d = Double.parseDouble(value);
                                properties.put(key, d);
                            } catch (NumberFormatException ignored3) {
                                // Not a number, treat as string
                                properties.put(key, value);
                            }
                        }
                    }
                }
            }
        }

        return new AppldataParseResult(items, modifiableItems, nonModifiableItems, modifiableItems, properties);
    }
}
