package biz.paluch.logging.gelf.jboss7;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Mark Paluch
 */
public class JBoss7GelfLogHandlerUnitTests {

    @Test
    public void testSameFieldsJBoss7GelfLogHandler() {
        JBoss7GelfLogHandler sut = new JBoss7GelfLogHandler();

        sut.setDynamicMdcFields(".*");
        sut.setIncludeFullMdc(true);
        sut.setMdcFields("");
        sut.setMdcProfiling(true);

        assertThat(sut.isIncludeFullMdc()).isTrue();
        assertThat(sut.isMdcProfiling()).isTrue();
    }

}
