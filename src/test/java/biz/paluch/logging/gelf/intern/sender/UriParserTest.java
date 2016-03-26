package biz.paluch.logging.gelf.intern.sender;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URI;
import java.util.Map;

import org.fest.assertions.MapAssert;
import org.junit.Test;

/**
 * @author Mark Paluch
 */
public class UriParserTest {

    @Test
    public void testParse() throws Exception {
        Map<String, String> result = UriParser.parse(URI.create("tcp:12345?KeY=value"));
        assertThat(result).includes(MapAssert.entry("key", "value"));
        assertThat(result).excludes(MapAssert.entry("KeY", "value"));
    }

    @Test
    public void getHost() throws Exception {
        assertThat(UriParser.getHost(URI.create("tcp:12345?KeY=value"))).isEqualTo("12345");
        assertThat(UriParser.getHost(URI.create("tcp:12345"))).isEqualTo("12345");
        assertThat(UriParser.getHost(URI.create("tcp://12345?KeY=value"))).isEqualTo("12345");
        assertThat(UriParser.getHost(URI.create("tcp://12345"))).isEqualTo("12345");
    }

    @Test
    public void testGetTimeAsMsNoSuffix() throws Exception {
        Map<String, String> map = UriParser.parse(URI.create("tcp:12345?timeout=1000"));
        long result = UriParser.getTimeAsMs(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetTimeAsMsNoSeconds() throws Exception {
        Map<String, String> map = UriParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = UriParser.getTimeAsMs(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetTimeAsMsDefaultFallback() throws Exception {
        Map<String, String> map = UriParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = UriParser.getTimeAsMs(map, "not here", -1);
        assertThat(result).isEqualTo(-1);
    }

    @Test
    public void testGetInt() throws Exception {
        Map<String, String> map = UriParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = UriParser.getInt(map, "timeout", -1);
        assertThat(result).isEqualTo(1000);
    }

    @Test
    public void testGetIntDefault() throws Exception {
        Map<String, String> map = UriParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = UriParser.getInt(map, "not here", -1);
        assertThat(result).isEqualTo(-1);
    }
}