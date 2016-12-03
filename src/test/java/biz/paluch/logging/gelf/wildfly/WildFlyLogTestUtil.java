package biz.paluch.logging.gelf.wildfly;

import java.util.logging.Level;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 11.08.14 08:36
 */
public class WildFlyLogTestUtil {

    public static WildFlyGelfLogHandler getWildFlyGelfLogHandler() {

        WildFlyGelfLogHandler handler = new WildFlyGelfLogHandler();

        handler.setGraylogHost("test:localhost");
        handler.setGraylogPort(12201);

        handler.setHost("test:localhost");
        handler.setPort(12201);
        handler.setVersion(GelfMessage.GELF_VERSION_1_1);

        handler.setFacility("java-test");
        handler.setExtractStackTrace("0");
        handler.setFilterStackTrace(true);
        handler.setTimestampPattern("yyyy-MM-dd HH:mm:ss,SSSS");
        handler.setMaximumMessageSize(8192);
        handler.setAdditionalFields("fieldName1=fieldValue1,fieldName2=fieldValue2");
        handler.setLevel(Level.INFO);
        handler.setMdcFields("mdcField1,mdcField2");

        return handler;
    }
}
