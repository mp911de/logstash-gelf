package biz.paluch.logging.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import biz.paluch.logging.RuntimeContainer;

/**
 * @author Mark Paluch
 */
class GelfLogHandlerUnitTests {

    private static final String FACILITY = "facility";
    private static final String HOST = "host";
    private static final int GRAYLOG_PORT = 1;
    private static final int MAXIMUM_MESSAGE_SIZE = 1234;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Test
    void testSameFieldsGelfLogHandler() {
        GelfLogHandler sut = new GelfLogHandler();
        sut.setAdditionalFields("");
        sut.setExtractStackTrace("true");
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setTimestampPattern(TIMESTAMP_PATTERN);

        sut.flush();

        assertThat(sut.getFacility()).isEqualTo(FACILITY);
        assertThat(sut.getGraylogHost()).isEqualTo(HOST);
        assertThat(sut.getHost()).isEqualTo(HOST);
        assertThat(sut.getPort()).isEqualTo(GRAYLOG_PORT);
        assertThat(sut.getGraylogPort()).isEqualTo(GRAYLOG_PORT);
        assertThat(sut.getMaximumMessageSize()).isEqualTo(MAXIMUM_MESSAGE_SIZE);
        assertThat(sut.getTimestampPattern()).isEqualTo(TIMESTAMP_PATTERN);
        assertThat(sut.getOriginHost()).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);

        assertThat(sut.getExtractStackTrace()).isEqualTo("true");
        assertThat(sut.isFilterStackTrace()).isTrue();
    }
}
