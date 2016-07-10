package biz.paluch.logging.gelf;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mark Paluch
 */
public class JsonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse a JSON string to a {@link Map}
     * 
     * @param jsonAsString
     * @return
     */
    public static Map<String, Object> parseToMap(String jsonAsString) {
        try {
            return objectMapper.readValue(jsonAsString, Map.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
