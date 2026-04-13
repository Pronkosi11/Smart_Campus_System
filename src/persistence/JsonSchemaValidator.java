package persistence;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Lightweight JSON Schema validator for this project.
 * Supports the subset used by files in schemas/.
 */
public final class JsonSchemaValidator {

    private JsonSchemaValidator() {
    }

    @SuppressWarnings("unchecked")
    public static void validate(Object data, Map<String, Object> schema, String path) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema is null at " + path);
        }

        Object enumObj = schema.get("enum");
        if (enumObj instanceof List<?>) {
            List<?> allowed = (List<?>) enumObj;
            if (!containsEquivalent(allowed, data)) {
                throw new IllegalArgumentException(path + ": value not in enum");
            }
        }

        Object typeObj = schema.get("type");
        if (typeObj != null && !matchesType(data, typeObj)) {
            throw new IllegalArgumentException(path + ": expected type " + typeObj + " but found " + typeName(data));
        }

        if (data instanceof String) {
            String s = (String) data;
            Object minLen = schema.get("minLength");
            if (minLen instanceof Number && s.length() < ((Number) minLen).intValue()) {
                throw new IllegalArgumentException(path + ": string shorter than minLength");
            }
            Object pattern = schema.get("pattern");
            if (pattern instanceof String && !Pattern.matches((String) pattern, s)) {
                throw new IllegalArgumentException(path + ": string does not match required pattern");
            }
        }

        if (data instanceof Number) {
            double n = ((Number) data).doubleValue();
            Object min = schema.get("minimum");
            Object max = schema.get("maximum");
            if (min instanceof Number && n < ((Number) min).doubleValue()) {
                throw new IllegalArgumentException(path + ": number below minimum");
            }
            if (max instanceof Number && n > ((Number) max).doubleValue()) {
                throw new IllegalArgumentException(path + ": number above maximum");
            }
        }

        if (data instanceof List<?>) {
            List<?> arr = (List<?>) data;
            Object minItems = schema.get("minItems");
            if (minItems instanceof Number && arr.size() < ((Number) minItems).intValue()) {
                throw new IllegalArgumentException(path + ": array shorter than minItems");
            }

            Object items = schema.get("items");
            if (items instanceof Map<?, ?>) {
                Map<String, Object> itemSchema = (Map<String, Object>) items;
                for (int i = 0; i < arr.size(); i++) {
                    validate(arr.get(i), itemSchema, path + "[" + i + "]");
                }
            }
            return;
        }

        if (data instanceof Map<?, ?>) {
            Map<String, Object> obj = (Map<String, Object>) data;

            Object requiredObj = schema.get("required");
            if (requiredObj instanceof List<?>) {
                for (Object req : (List<?>) requiredObj) {
                    if (req != null) {
                        String key = req.toString();
                        if (!obj.containsKey(key)) {
                            throw new IllegalArgumentException(path + ": missing required property '" + key + "'");
                        }
                    }
                }
            }

            boolean additionalAllowed = true;
            Object additionalProps = schema.get("additionalProperties");
            if (additionalProps instanceof Boolean) {
                additionalAllowed = (Boolean) additionalProps;
            }

            Object propsObj = schema.get("properties");
            if (propsObj instanceof Map<?, ?>) {
                Map<String, Object> props = (Map<String, Object>) propsObj;

                if (!additionalAllowed) {
                    for (String key : obj.keySet()) {
                        if (!props.containsKey(key)) {
                            throw new IllegalArgumentException(path + ": unexpected property '" + key + "'");
                        }
                    }
                }

                for (Map.Entry<String, Object> e : props.entrySet()) {
                    String key = e.getKey();
                    Object value = obj.get(key);
                    if (!obj.containsKey(key)) {
                        continue;
                    }
                    if (e.getValue() instanceof Map<?, ?>) {
                        validate(value, (Map<String, Object>) e.getValue(), path + "." + key);
                    }
                }
            }
        }
    }

    private static boolean containsEquivalent(List<?> list, Object value) {
        for (Object candidate : list) {
            if (candidate == null && value == null) {
                return true;
            }
            if (candidate != null && candidate.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesType(Object value, Object typeObj) {
        if (typeObj instanceof String) {
            return matchesSingleType(value, (String) typeObj);
        }
        if (typeObj instanceof List<?>) {
            for (Object type : (List<?>) typeObj) {
                if (type instanceof String && matchesSingleType(value, (String) type)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean matchesSingleType(Object value, String type) {
        switch (type) {
            case "null":
                return value == null;
            case "string":
                return value instanceof String;
            case "boolean":
                return value instanceof Boolean;
            case "number":
                return value instanceof Number;
            case "integer":
                if (!(value instanceof Number)) {
                    return false;
                }
                double d = ((Number) value).doubleValue();
                return Math.floor(d) == d;
            case "array":
                return value instanceof List<?>;
            case "object":
                return value instanceof Map<?, ?>;
            default:
                return true;
        }
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }
}
