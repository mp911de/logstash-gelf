package biz.paluch.logging.gelf.intern;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mark Paluch
 */
public class JsonWriterUnitTests {

    private String content;

    @Before
    public void before() throws Exception {

        byte[] bytes = IOUtils.toByteArray(getClass().getResourceAsStream("/utf8.txt"));
        content = new String(bytes, "UTF-8");
    }

    @Test
    public void testUtf8Encoding() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", content);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonWriter.toJSONString(OutputAccessor.from(buffer), map);

        ObjectMapper objectMapper = new ObjectMapper();
        Map parsedByJackson = objectMapper.readValue(buffer.toByteArray(), Map.class);
        assertThat(parsedByJackson).isEqualTo(map);
    }

    @Test
    public void testUtf8EncodingWithJacksonEncoding() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", content);

        ObjectMapper objectMapper = new ObjectMapper();
        Map parsedByJackson = objectMapper.readValue(objectMapper.writeValueAsBytes(map), Map.class);

        assertThat(parsedByJackson).isEqualTo(map);
    }

    @Test
    public void testTypeEncoding() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "string");
        map.put("double", Double.MAX_VALUE);
        map.put("doublePosInfinite", Double.POSITIVE_INFINITY);
        map.put("doubleNegInfinite", Double.NEGATIVE_INFINITY);
        map.put("doubleNaN", Double.NaN);
        map.put("int", 1);

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("key", "string");
        expected.put("double", Double.MAX_VALUE);
        expected.put("doublePosInfinite", "Infinite");
        expected.put("doubleNegInfinite", "-Infinite");
        expected.put("doubleNaN", "NaN");
        expected.put("int", 1);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonWriter.toJSONString(OutputAccessor.from(buffer), map);

        ObjectMapper objectMapper = new ObjectMapper();
        Map parsedByJackson = objectMapper.readValue(buffer.toByteArray(), Map.class);
        assertThat(parsedByJackson).isEqualTo(expected);
    }
}
