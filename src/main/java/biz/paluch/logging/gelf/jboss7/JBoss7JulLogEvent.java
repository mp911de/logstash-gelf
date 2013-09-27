package biz.paluch.logging.gelf.jboss7;

import biz.paluch.logging.gelf.MdcLogEvent;
import biz.paluch.logging.gelf.jul.JulLogEvent;
import org.apache.log4j.MDC;

import java.util.logging.LogRecord;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 18:32
 */
public class JBoss7JulLogEvent extends JulLogEvent implements MdcLogEvent {

    public JBoss7JulLogEvent(LogRecord logRecord) {
        super(logRecord);
    }

    @Override
    public Object getMDC(String item) {
        return MDC.get(item);
    }
}
