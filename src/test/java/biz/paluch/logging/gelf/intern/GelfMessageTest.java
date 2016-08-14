package biz.paluch.logging.gelf.intern;

import static biz.paluch.logging.gelf.GelfMessageBuilder.newInstance;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jboss.as.protocol.StreamUtils;
import org.junit.Test;

import biz.paluch.logging.StackTraceFilter;
import biz.paluch.logging.gelf.GelfMessageBuilder;

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
            put("doubleNoDecimals", "2.0");
            put("doubleWithDecimals", "2.1");
            put("int", "2");
            put("exception1", StackTraceFilter.getFilteredStackTrace(new IOException(new Exception(new Exception()))));
            put("exception2",
                    StackTraceFilter.getFilteredStackTrace(new IllegalStateException(new Exception(new Exception()))));
            put("exception3", StackTraceFilter
                    .getFilteredStackTrace(new IllegalArgumentException(new Exception(new IllegalArgumentException()))));
            put("exception4", StackTraceFilter.getFilteredStackTrace(new Exception(new Exception(new Exception()))));
            put("exception5", StackTraceFilter.getFilteredStackTrace(new Exception(new Exception(new ConnectException()))));
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

        assertThat(gelfMessage.toJson(), containsString("\"_int\":2"));
        assertThat(gelfMessage.toJson(), containsString("\"_doubleNoDecimals\":2.0"));
        assertThat(gelfMessage.toJson(), containsString("\"_doubleWithDecimals\":2.1"));
    }

    @Test
    public void testEncoded() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        String message = gelfMessage.toJson("_");

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        gelfMessage.toJson(buffer, "_");

        String string = toString(buffer);

        assertEquals(message, string);
    }

    @Test
    public void testTcp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

        ByteBuffer oldWay = gelfMessage.toTCPBuffer();
        ByteBuffer newWay = gelfMessage.toTCPBuffer(buffer);

        assertEquals(oldWay.remaining(), newWay.remaining());

        byte[] oldBytes = new byte[oldWay.remaining()];
        byte[] newBytes = new byte[newWay.remaining()];

        oldWay.get(oldBytes);
        newWay.get(newBytes);

        assertTrue(Arrays.equals(newBytes, oldBytes));
    }

    @Test
    public void testUdp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(8192);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = gelfMessage.toUDPBuffers(buffer, buffer2);

        assertEquals(oldWay.length, newWay.length);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(newBytes));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamUtils.copyStream(gzipInputStream, baos);
            assertTrue(Arrays.equals(newBytes, oldBytes));
        }
    }

    @Test
    public void testUdpChunked() throws Exception {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            int charId = (int) (Math.random() * Character.MAX_CODE_POINT);
            builder.append(charId);
        }

        GelfMessage gelfMessage = createGelfMessage();
        gelfMessage.setFullMessage(builder.toString());

        ByteBuffer buffer = ByteBuffer.allocateDirect(1200000);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(60000);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = gelfMessage.toUDPBuffers(buffer, tempBuffer);

        assertEquals(oldWay.length, newWay.length);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            assertTrue(Arrays.equals(newBytes, oldBytes));
        }
    }

    protected String toString(ByteBuffer allocate) {
        if (allocate.hasArray()) {
            return new String(allocate.array(), 0, allocate.arrayOffset() + allocate.position());
        } else {
            final byte[] b = new byte[allocate.remaining()];
            allocate.duplicate().get(b);
            return new String(b);
        }
    }

    @Test
    public void testGelfMessageEmptyField() throws Exception {
        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.addField("something", null);

        assertFalse(gelfMessage.toJson().contains("something"));

    }

    @Test
    public void testGelf_v1_0() throws Exception {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);

        assertEquals(GelfMessage.GELF_VERSION_1_0, gelfMessage.getVersion());
        assertThat(gelfMessage.toJson(), containsString("\"level\":\"6\""));
        assertThat(gelfMessage.toJson(), containsString("\"timestamp\":\"123.456"));
    }

    @Test
    public void testGelf_v1_1() throws Exception {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);
        gelfMessage.setVersion(GelfMessage.GELF_VERSION_1_1);

        assertEquals(GelfMessage.GELF_VERSION_1_1, gelfMessage.getVersion());
        assertThat(gelfMessage.toJson(), containsString("\"level\":6"));
        assertThat(gelfMessage.toJson(), containsString("\"timestamp\":123.456"));

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
        GelfMessage build = newInstance().build();

        assertTrue(created.equals(build));
        assertEquals(created.hashCode(), build.hashCode());
    }

    private GelfMessage createGelfMessage() {

        GelfMessage gelfMessage = new GelfMessage() {
            @Override
            public int getCurrentMillis() {
                return 1000;
            }
        };

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
        GelfMessageBuilder builder = newInstance();
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
