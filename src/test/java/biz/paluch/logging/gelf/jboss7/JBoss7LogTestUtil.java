package biz.paluch.logging.gelf.jboss7;

import java.util.logging.Level;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 28.02.14 11:29
 */
public class JBoss7LogTestUtil {
    public static JBoss7GelfLogHandler getJBoss7GelfLogHandler() {
        JBoss7GelfLogHandler handler = new JBoss7GelfLogHandler();

        handler.setGraylogHost("test:localhost");
        handler.setGraylogPort(12201);

        handler.setHost("test:localhost");
        handler.setPort(12201);
        handler.setVersion(GelfMessage.GELF_VERSION_1_1);

        handler.setFacility("java-test");
        handler.setExtractStackTrace(true);
        handler.setFilterStackTrace(true);
        handler.setTimestampPattern("yyyy-MM-dd HH:mm:ss,SSSS");
        handler.setMaximumMessageSize(8192);
        handler.setAdditionalFields("fieldName1=fieldValue1,fieldName2=fieldValue2");
        handler.setLevel(Level.INFO);
        handler.setMdcFields("mdcField1,mdcField2");
        return handler;
    }
}
