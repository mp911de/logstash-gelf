package biz.paluch.logging.gelf.jboss7;

import static org.junit.Assert.assertTrue;

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

        assertTrue(sut.isIncludeFullMdc());
        assertTrue(sut.isMdcProfiling());
    }

}