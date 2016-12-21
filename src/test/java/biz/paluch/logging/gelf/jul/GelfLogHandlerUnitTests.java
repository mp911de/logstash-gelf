package biz.paluch.logging.gelf.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biz.paluch.logging.RuntimeContainer;

/**
 * @author Mark Paluch
 */
public class GelfLogHandlerUnitTests {

    private static final String FACILITY = "facility";
    private static final String HOST = "host";
    private static final int GRAYLOG_PORT = 1;
    private static final int MAXIMUM_MESSAGE_SIZE = 1234;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss,SSSS";

    @Test
    public void testSameFieldsGelfLogHandler() {
        GelfLogHandler sut = new GelfLogHandler();
        sut.setAdditionalFields("");
        sut.setExtractStackTrace("true");
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);

        sut.flush();

        assertEquals(FACILITY, sut.getFacility());
        assertEquals(HOST, sut.getGraylogHost());
        assertEquals(HOST, sut.getHost());
        assertEquals(GRAYLOG_PORT, sut.getPort());
        assertEquals(GRAYLOG_PORT, sut.getGraylogPort());
        assertEquals(MAXIMUM_MESSAGE_SIZE, sut.getMaximumMessageSize());
        assertEquals(TIMESTAMP_PATTERN, sut.getTimestampPattern());
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, sut.getOriginHost());

        assertEquals("true", sut.getExtractStackTrace());
        assertTrue(sut.isFilterStackTrace());
    }
}