package biz.paluch.logging.gelf.intern.sender;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URI;
import java.util.Map;

import org.fest.assertions.MapAssert;
import org.junit.jupiter.api.Test;

/**
 * @author Mark Paluch
 */
public class QueryStringParserUnitTests {

    @Test
    public void testParse() throws Exception {
        Map<String, String> result = QueryStringParser.parse(URI.create("tcp:12345?KeY=value"));
        assertThat(result).includes(MapAssert.entry("key", "value"));
        assertThat(result).excludes(MapAssert.entry("KeY", "value"));
    }

    @Test
    public void getHost() throws Exception {
        assertThat(QueryStringParser.getHost(URI.create("tcp:12345?KeY=value"))).isEqualTo("12345");
        assertThat(QueryStringParser.getHost(URI.create("tcp:12345"))).isEqualTo("12345");
        assertThat(QueryStringParser.getHost(URI.create("tcp://12345?KeY=value"))).isEqualTo("12345");
        assertThat(QueryStringParser.getHost(URI.create("tcp://12345"))).isEqualTo("12345");
    }

    @Test
    public void testGetTimeAsMsNoSuffix() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        long result = QueryStringParser.getTimeAsMs(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetTimeAsMsNoSeconds() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = QueryStringParser.getTimeAsMs(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetTimeAsMsDefaultFallback() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = QueryStringParser.getTimeAsMs(map, "not here", -1);
        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void testGetInt() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = QueryStringParser.getInt(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetIntDefault() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = QueryStringParser.getInt(map, "not here", -1);
        assertThat(result).isEqualTo(-1);
    }
}
