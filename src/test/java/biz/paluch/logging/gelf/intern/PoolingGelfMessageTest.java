package biz.paluch.logging.gelf.intern;

import static org.junit.Assert.assertEquals;
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

public class PoolingGelfMessageTest {

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
    public void testUdp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();
        PoolingGelfMessage poolingGelfMessage = createPooledGelfMessage();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(8192);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = poolingGelfMessage.toUDPBuffers(buffer, buffer2);

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
        PoolingGelfMessage poolingGelfMessage = createPooledGelfMessage();

        gelfMessage.setFullMessage(builder.toString());
        poolingGelfMessage.setFullMessage(builder.toString());

        ByteBuffer buffer = ByteBuffer.allocateDirect(1200000);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(60000);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = poolingGelfMessage.toUDPBuffers(buffer, tempBuffer);

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

    private PoolingGelfMessage createPooledGelfMessage() {

        PoolingGelfMessage gelfMessage = new PoolingGelfMessage() {
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

}
