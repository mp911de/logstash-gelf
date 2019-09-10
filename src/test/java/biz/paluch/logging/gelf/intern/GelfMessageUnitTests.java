package biz.paluch.logging.gelf.intern;

import static biz.paluch.logging.gelf.GelfMessageBuilder.newInstance;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jboss.as.protocol.StreamUtils;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.StackTraceFilter;
import biz.paluch.logging.gelf.GelfMessageBuilder;

class GelfMessageUnitTests {

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
        }
    };

    @Test
    void testBuilder() throws Exception {
        GelfMessage gelfMessage = buildGelfMessage();

        assertThat(gelfMessage.getFacility()).isEqualTo(FACILITY);
        assertThat(gelfMessage.getField("a")).isEqualTo("b");
        assertThat(gelfMessage.getFullMessage()).isEqualTo(FULL_MESSAGE);
        assertThat(gelfMessage.getHost()).isEqualTo(HOST);
        assertThat(gelfMessage.getLevel()).isEqualTo(LEVEL);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(SHORT_MESSAGE);
        assertThat(gelfMessage.getJavaTimestamp().longValue()).isEqualTo(TIMESTAMP);
        assertThat(gelfMessage.getVersion()).isEqualTo(VERSION);
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(MESSAGE_SIZE);

    }

    @Test
    void testGelfMessage() throws Exception {
        GelfMessage gelfMessage = createGelfMessage();

        assertThat(gelfMessage.getFacility()).isEqualTo(FACILITY);
        assertThat(gelfMessage.getField("a")).isEqualTo("b");
        assertThat(gelfMessage.getFullMessage()).isEqualTo(FULL_MESSAGE);
        assertThat(gelfMessage.getHost()).isEqualTo(HOST);
        assertThat(gelfMessage.getLevel()).isEqualTo(LEVEL);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(SHORT_MESSAGE);
        assertThat(gelfMessage.getJavaTimestamp().longValue()).isEqualTo(TIMESTAMP);
        assertThat(gelfMessage.getVersion()).isEqualTo(VERSION);
        assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(MESSAGE_SIZE);

        assertThat(gelfMessage.toJson()).contains("\"_int\":2");
        assertThat(gelfMessage.toJson()).contains("\"_doubleNoDecimals\":2.0");
        assertThat(gelfMessage.toJson()).contains("\"_doubleWithDecimals\":2.1");
    }

    @Test
    void testEncoded() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        String message = gelfMessage.toJson("_");

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        gelfMessage.toJson(buffer, "_");

        String string = toString(buffer);

        assertThat(string).isEqualTo(message);
    }

    @Test
    void testTcp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

        ByteBuffer oldWay = gelfMessage.toTCPBuffer();
        ByteBuffer newWay = gelfMessage.toTCPBuffer(buffer);

        assertThat(newWay.remaining()).isEqualTo(oldWay.remaining());

        byte[] oldBytes = new byte[oldWay.remaining()];
        byte[] newBytes = new byte[newWay.remaining()];

        oldWay.get(oldBytes);
        newWay.get(newBytes);

        assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
    }

    @Test
    void testUdp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(8192);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = gelfMessage.toUDPBuffers(buffer, buffer2);

        assertThat(newWay.length).isEqualTo(oldWay.length);

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
            assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
        }
    }

    @Test
    void testUdpChunked() throws Exception {

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

        assertThat(newWay.length).isEqualTo(oldWay.length);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
        }
    }

    @Test
    void testGenerateMsgId() {
        GelfMessage gelfMessage = new GelfMessage() {
            @Override
            long getRandomLong() {
                return 0x804020100804A201L;
            }

            @Override
            long getCurrentTimeMillis() {
                return 0x90C06030090C1683L;
            }
        };

        assertThat(gelfMessage.generateMsgId()).isEqualTo(0x804020100804B683L);
    }

    String toString(ByteBuffer allocate) {
        if (allocate.hasArray()) {
            return new String(allocate.array(), 0, allocate.arrayOffset() + allocate.position());
        } else {
            final byte[] b = new byte[allocate.remaining()];
            allocate.duplicate().get(b);
            return new String(b);
        }
    }

    @Test
    void testGelfMessageEmptyField() throws Exception {
        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.addField("something", null);

        assertThat(gelfMessage.toJson().contains("something")).isFalse();

    }

    @Test
    void testGelf_v1_0() throws Exception {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);

        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_0);
        assertThat(gelfMessage.toJson()).contains("\"level\":\"6\"");
        assertThat(gelfMessage.toJson()).contains("\"timestamp\":\"123.456");
    }

    @Test
    void testGelf_v1_1() throws Exception {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);
        gelfMessage.setVersion(GelfMessage.GELF_VERSION_1_1);

        assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        assertThat(gelfMessage.toJson()).contains("\"level\":6");
        assertThat(gelfMessage.toJson()).contains("\"timestamp\":123.456");

    }

    @Test
    void testGelfMessageEquality() throws Exception {
        GelfMessage created = createGelfMessage();
        GelfMessage build = buildGelfMessage();

        assertThat(created.equals(build)).isTrue();
        assertThat(build).isEqualTo(created);
        assertThat(build.hashCode()).isEqualTo(created.hashCode());

        build.setFacility("other");
        assertThat(created).isNotEqualTo(build);
    }

    @Test
    void testGelfMessageDefaults() throws Exception {
        GelfMessage created = new GelfMessage();
        GelfMessage build = newInstance().build();

        assertThat(created.equals(build)).isTrue();
        assertThat(build.hashCode()).isEqualTo(created.hashCode());
    }

    private GelfMessage createGelfMessage() {

        GelfMessage gelfMessage = new GelfMessage() {
            @Override
            long generateMsgId() {
                return 0x8040201008048683L;
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
