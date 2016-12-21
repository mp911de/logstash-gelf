package biz.paluch.logging.gelf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.jboss.logmanager.ExtLogRecord;
import org.junit.Test;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.jboss7.JBoss7JulLogEvent;

/**
 * @author Mark Paluch
 */
public class GelfUtilUnitTests {

    @Test
    public void testProfilingString() throws Exception {

        Map mdcMap = new HashMap();
        mdcMap.put(GelfUtil.MDC_REQUEST_START_MS, "" + (System.currentTimeMillis() - 12000));
        ExtLogRecord extLogRecord = new ExtLogRecord(Level.INFO, "test", "");
        extLogRecord.setMdc(mdcMap);
        GelfMessage message = new GelfMessage();

        GelfUtil.addMdcProfiling(new JBoss7JulLogEvent(extLogRecord), message);

        assertThat(message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION)).isEqualTo("12sec");
    }

    @Test
    public void testProfilingLong() throws Exception {

        Map mdcMap = new HashMap();
        mdcMap.put(GelfUtil.MDC_REQUEST_START_MS, (System.currentTimeMillis() - 12000));
        ExtLogRecord extLogRecord = new ExtLogRecord(Level.INFO, "test", "");
        extLogRecord.setMdc(mdcMap);
        GelfMessage message = new GelfMessage();

        GelfUtil.addMdcProfiling(new JBoss7JulLogEvent(extLogRecord), message);

        assertThat(message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION)).isEqualTo("12sec");
    }

}
