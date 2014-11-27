package biz.paluch.logging.gelf;

import static org.junit.Assert.*;

import org.junit.Test;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.jboss7.JBoss7GelfLogHandler;
import biz.paluch.logging.gelf.jul.GelfLogHandler;
import biz.paluch.logging.gelf.log4j.GelfLogAppender;
import biz.paluch.logging.gelf.logback.GelfLogbackAppender;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 16.07.14 21:00
 */
public class SillyTest {

    public static final String NAME = "name";
    public static final String FACILITY = "facility";
    public static final String HOST = "host";
    public static final int GRAYLOG_PORT = 1;
    public static final int MAXIMUM_MESSAGE_SIZE = 1234;
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss,SSSS";

    @Test
    public void testMdcMessageField() throws Exception {
        MdcMessageField field = new MdcMessageField(NAME, "mdcName");
        assertEquals(MdcMessageField.class.getSimpleName() + " [name='name', mdcName='mdcName']", field.toString());
    }

    @Test
    public void testLogMessageField() throws Exception {
        LogMessageField field = new LogMessageField(NAME, LogMessageField.NamedLogField.byName("SourceMethodName"));
        assertEquals(LogMessageField.class.getSimpleName() + " [name='name', namedLogField=SourceMethodName]", field.toString());
    }

    @Test
    public void testStaticMessageField() throws Exception {
        StaticMessageField field = new StaticMessageField(NAME, "value");
        assertEquals(StaticMessageField.class.getSimpleName() + " [name='name', value='value']", field.toString());
    }

    @Test
    public void testDynamicMdcMessageField() throws Exception {
        DynamicMdcMessageField field = new DynamicMdcMessageField(".*");
        assertEquals(DynamicMdcMessageField.class.getSimpleName() + " [regex='.*']", field.toString());
    }

    @Test
    public void testSameFieldsGelfLogHandler() {
        GelfLogHandler sut = new GelfLogHandler();
        sut.setAdditionalFields("");
        sut.setExtractStackTrace(true);
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

        assertTrue(sut.isExtractStackTrace());
        assertTrue(sut.isFilterStackTrace());
    }

    @Test
    public void testSameFieldsJBoss7GelfLogHandler() {
        JBoss7GelfLogHandler sut = new JBoss7GelfLogHandler();

        sut.setDynamicMdcFields(".*");
        sut.setIncludeFullMdc(true);
        sut.setMdcFields("");
        sut.setMdcProfiling(true);

        assertTrue(sut.isIncludeFullMdc());
        assertTrue(sut.isMdcProfiling());
    }

    @Test
    public void testSameFieldsGelfLogAppender12x() {
        GelfLogAppender sut = new GelfLogAppender();
        sut.setAdditionalFields("");
        sut.setExtractStackTrace(true);
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setDynamicMdcFields(".*");
        sut.setIncludeFullMdc(true);
        sut.setMdcFields("");
        sut.setMdcProfiling(true);

        assertEquals(FACILITY, sut.getFacility());
        assertEquals(HOST, sut.getGraylogHost());
        assertEquals(HOST, sut.getHost());
        assertEquals(GRAYLOG_PORT, sut.getPort());
        assertEquals(GRAYLOG_PORT, sut.getGraylogPort());
        assertEquals(MAXIMUM_MESSAGE_SIZE, sut.getMaximumMessageSize());
        assertEquals(TIMESTAMP_PATTERN, sut.getTimestampPattern());
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, sut.getOriginHost());

        assertTrue(sut.isExtractStackTrace());
        assertTrue(sut.isFilterStackTrace());
        assertTrue(sut.isIncludeFullMdc());
        assertTrue(sut.isMdcProfiling());
    }

    @Test
    public void testSameFieldsGelfLogbackAppender() {
        GelfLogbackAppender sut = new GelfLogbackAppender();

        sut.setAdditionalFields("");
        sut.setExtractStackTrace(true);
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setDynamicMdcFields(".*");
        sut.setIncludeFullMdc(true);
        sut.setMdcFields("");
        sut.setMdcProfiling(true);

        assertEquals(FACILITY, sut.getFacility());
        assertEquals(HOST, sut.getGraylogHost());
        assertEquals(HOST, sut.getHost());
        assertEquals(GRAYLOG_PORT, sut.getPort());
        assertEquals(GRAYLOG_PORT, sut.getGraylogPort());
        assertEquals(MAXIMUM_MESSAGE_SIZE, sut.getMaximumMessageSize());
        assertEquals(TIMESTAMP_PATTERN, sut.getTimestampPattern());
        assertEquals(RuntimeContainer.FQDN_HOSTNAME, sut.getOriginHost());

        assertTrue(sut.isExtractStackTrace());
        assertTrue(sut.isFilterStackTrace());
        assertTrue(sut.isIncludeFullMdc());
        assertTrue(sut.isMdcProfiling());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPort() throws Exception {

        GelfLogbackAppender sut = new GelfLogbackAppender();
        sut.setPort(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaximumMessageSize() throws Exception {

        GelfLogbackAppender sut = new GelfLogbackAppender();
        sut.setMaximumMessageSize(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion() throws Exception {

        GelfLogbackAppender sut = new GelfLogbackAppender();
        sut.setVersion("7");
    }
}
