package dev.infrai.digest.infra;

import java.util.LinkedHashMap;
import java.util.Map;

final class Json {
    private final String source;
    private int cursor;

    private Json(String source) {
        this.source = source;
    }

    static Map<String, Object> parseObject(String source) {
        Object value = new Json(source).readValue();
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected JSON object");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }

    private Object readValue() {
        whitespace();
        if (peek('{')) return readObject();
        if (peek('"')) return readString();
        if (source.startsWith("true", cursor)) { cursor += 4; return true; }
        if (source.startsWith("false", cursor)) { cursor += 5; return false; }
        if (source.startsWith("null", cursor)) { cursor += 4; return null; }
        return readNumber();
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> values = new LinkedHashMap<>();
        whitespace();
        if (peek('}')) { cursor++; return values; }
        while (true) {
            String key = readString();
            whitespace();
            expect(':');
            values.put(key, readValue());
            whitespace();
            if (peek('}')) { cursor++; return values; }
            expect(',');
            whitespace();
        }
    }

    private String readString() {
        expect('"');
        StringBuilder value = new StringBuilder();
        while (cursor < source.length()) {
            char current = source.charAt(cursor++);
            if (current == '"') return value.toString();
            if (current == '\\') {
                char escaped = source.charAt(cursor++);
                value.append(switch (escaped) {
                    case '"', '\\', '/' -> escaped;
                    case 'b' -> '\b';
                    case 'f' -> '\f';
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case 'u' -> (char) Integer.parseInt(source.substring(cursor, cursor += 4), 16);
                    default -> throw new IllegalArgumentException("Invalid JSON escape");
                });
            } else {
                value.append(current);
            }
        }
        throw new IllegalArgumentException("Unclosed JSON string");
    }

    private Number readNumber() {
        int start = cursor;
        while (cursor < source.length() && "-+.eE0123456789".indexOf(source.charAt(cursor)) >= 0) cursor++;
        String number = source.substring(start, cursor);
        return number.contains(".") || number.contains("e") || number.contains("E")
                ? Double.parseDouble(number) : Long.parseLong(number);
    }

    private void whitespace() {
        while (cursor < source.length() && Character.isWhitespace(source.charAt(cursor))) cursor++;
    }

    private boolean peek(char expected) {
        whitespace();
        return cursor < source.length() && source.charAt(cursor) == expected;
    }

    private void expect(char expected) {
        whitespace();
        if (cursor >= source.length() || source.charAt(cursor++) != expected) {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }
}
