package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UTF-8 file I/O and a small JSON reader/writer (no external libraries).
 */
public final class JsonFileHandler {

    private JsonFileHandler() {
    }

    public static String readFile(String relativePath) throws IOException {
        Path p = Paths.get(relativePath);
        if (!Files.exists(p)) {
            return null;
        }
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    public static void writeFile(String relativePath, String content) throws IOException {
        Path p = Paths.get(relativePath);
        Path parent = p.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(p, content, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        JsonReader r = new JsonReader(json.trim());
        Object v = r.readValue();
        if (!(v instanceof Map)) {
            return new LinkedHashMap<>();
        }
        return (Map<String, Object>) v;
    }

    public static String stringifyObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        writeObject(sb, map, 0);
        return sb.toString();
    }

    static void writeValue(StringBuilder sb, Object v) {
        writeValue(sb, v, 0);
    }

    static void writeValue(StringBuilder sb, Object v, int indent) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            writeString(sb, (String) v);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) v;
            writeObject(sb, m, indent);
        } else if (v instanceof List) {
            writeArray(sb, (List<?>) v, indent);
        } else {
            writeString(sb, v.toString());
        }
    }

    private static void writeObject(StringBuilder sb, Map<String, Object> map, int indent) {
        sb.append('{');
        sb.append('\n');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
                sb.append('\n');
            }
            first = false;
            indent(sb, indent + 2);
            writeString(sb, e.getKey());
            sb.append(": ");
            writeValue(sb, e.getValue(), indent + 2);
        }
        if (!map.isEmpty()) {
            sb.append('\n');
            indent(sb, indent);
        }
        sb.append('}');
    }

    private static void writeArray(StringBuilder sb, List<?> list, int indent) {
        sb.append('[');
        if (!list.isEmpty()) {
            sb.append('\n');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                    sb.append('\n');
                }
                indent(sb, indent + 2);
                writeValue(sb, list.get(i), indent + 2);
            }
            sb.append('\n');
            indent(sb, indent);
        }
        sb.append(']');
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void indent(StringBuilder sb, int spaces) {
        for (int i = 0; i < spaces; i++) {
            sb.append(' ');
        }
    }

    private static final class JsonReader {
        private final String s;
        private int pos;

        JsonReader(String s) {
            this.s = s;
            this.pos = 0;
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            skipWs();
            return pos < s.length() ? s.charAt(pos) : 0;
        }

        Object readValue() {
            skipWs();
            if (pos >= s.length()) {
                return null;
            }
            char c = s.charAt(pos);
            if (c == '{') {
                return readObject();
            }
            if (c == '[') {
                return readArray();
            }
            if (c == '"') {
                return readString();
            }
            if (c == '-' || Character.isDigit(c)) {
                return readNumber();
            }
            if (s.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (s.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            if (s.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Unexpected JSON at " + pos);
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWs();
                String key = readString();
                skipWs();
                expect(':');
                Object val = readValue();
                map.put(key, val);
                skipWs();
                char d = s.charAt(pos++);
                if (d == '}') {
                    break;
                }
                if (d != ',') {
                    throw new IllegalArgumentException("Expected , or } in object");
                }
            }
            return map;
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(readValue());
                skipWs();
                char d = s.charAt(pos++);
                if (d == ']') {
                    break;
                }
                if (d != ',') {
                    throw new IllegalArgumentException("Expected , or ] in array");
                }
            }
            return list;
        }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\' && pos < s.length()) {
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(e);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            int code = Integer.parseInt(s.substring(pos, pos + 4), 16);
                            pos += 4;
                            sb.append((char) code);
                            break;
                        default:
                            sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private Number readNumber() {
            int start = pos;
            if (s.charAt(pos) == '-') {
                pos++;
            }
            while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
                pos++;
            }
            if (pos < s.length() && s.charAt(pos) == '.') {
                pos++;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
                    pos++;
                }
                double d = Double.parseDouble(s.substring(start, pos));
                return d;
            }
            long n = Long.parseLong(s.substring(start, pos));
            if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE) {
                return (int) n;
            }
            return n;
        }

        private void expect(char c) {
            skipWs();
            if (pos >= s.length() || s.charAt(pos) != c) {
                throw new IllegalArgumentException("Expected '" + c + "' at " + pos);
            }
            pos++;
        }
    }
}
