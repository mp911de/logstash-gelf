package biz.paluch.logging.gelf.intern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import biz.paluch.logging.gelf.GelfMessageBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GelfMessageTest {

    private static final String FACILITY = "facility";
    private static final String VERSION = "2.0";
    private static final String FULL_MESSAGE = "full";
    private static final String SHORT_MESSAGE = "short";
    private static final String HOST = "host";
    private static final String LEVEL = "5";
    private static final long TIMESTAMP = 42;
    private static final int MESSAGE_SIZE = 5344;

    private static final Map<String, String> ADDITIONAL_FIELDS = new HashMap<String, String>() {
        {
            put("a", "b");
        }
    };

    @Test
    public void testBuilder() throws Exception {
        GelfMessage gelfMessage = buildGelfMessage();

        assertEquals(FACILITY, gelfMessage.getFacility());
        assertEquals("b", gelfMessage.getField("a"));
        assertEquals(FULL_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(HOST, gelfMessage.getHost());
        assertEquals(LEVEL, gelfMessage.getLevel());
        assertEquals(SHORT_MESSAGE, gelfMessage.getShortMessage());
        assertEquals(TIMESTAMP, gelfMessage.getJavaTimestamp().longValue());
        assertEquals(VERSION, gelfMessage.getVersion());
        assertEquals(MESSAGE_SIZE, gelfMessage.getMaximumMessageSize());

    }

    @Test
    public void testGelfMessage() throws Exception {
        GelfMessage gelfMessage = createGelfMessage();

        assertEquals(FACILITY, gelfMessage.getFacility());
        assertEquals("b", gelfMessage.getField("a"));
        assertEquals(FULL_MESSAGE, gelfMessage.getFullMessage());
        assertEquals(HOST, gelfMessage.getHost());
        assertEquals(LEVEL, gelfMessage.getLevel());
        assertEquals(SHORT_MESSAGE, gelfMessage.getShortMessage());
        assertEquals(TIMESTAMP, gelfMessage.getJavaTimestamp().longValue());
        assertEquals(VERSION, gelfMessage.getVersion());
        assertEquals(MESSAGE_SIZE, gelfMessage.getMaximumMessageSize());
    }

    @Test
    public void testGelfMessageEmptyField() throws Exception {
        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.addField("something", null);

        assertFalse(gelfMessage.toJson().contains("something"));

    }

    @Test
    public void testGelfMessageEquality() throws Exception {
        GelfMessage created = createGelfMessage();
        GelfMessage build = buildGelfMessage();

        assertTrue(created.equals(build));
        assertEquals(created, build);
        assertEquals(created.hashCode(), build.hashCode());

        build.setFacility("other");
        assertFalse(created.equals(build));
        assertNotEquals(created, build);
    }

    @Test
    public void testGelfMessageDefaults() throws Exception {
        GelfMessage created = new GelfMessage();
        GelfMessage build = new GelfMessageBuilder().build();

        assertTrue(created.equals(build));
        assertEquals(created.hashCode(), build.hashCode());
    }

    private GelfMessage createGelfMessage() {
        GelfMessage gelfMessage = new GelfMessage();

        gelfMessage.setFacility(FACILITY);
        gelfMessage.setVersion(VERSION);
        gelfMessage.setFullMessage(FULL_MESSAGE);
        gelfMessage.setShortMessage(SHORT_MESSAGE);
        gelfMessage.setHost(HOST);
        gelfMessage.setJavaTimestamp(TIMESTAMP);
        gelfMessage.setLevel(LEVEL);
        gelfMessage.setMaximumMessageSize(MESSAGE_SIZE);
        gelfMessage.addFields(ADDITIONAL_FIELDS);
        return gelfMessage;
    }

    private GelfMessage buildGelfMessage() {
        GelfMessageBuilder builder = new GelfMessageBuilder();
        builder.withFacility(FACILITY);
        builder.withVersion(VERSION);
        builder.withFullMessage(FULL_MESSAGE);
        builder.withShortMessage(SHORT_MESSAGE);
        builder.withHost(HOST);
        builder.withJavaTimestamp(TIMESTAMP);
        builder.withLevel(LEVEL);
        builder.withMaximumMessageSize(MESSAGE_SIZE);
        builder.withFields(ADDITIONAL_FIELDS);

        return builder.build();
    }
}
