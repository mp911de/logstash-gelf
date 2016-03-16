package biz.paluch.logging.gelf;

import static org.junit.Assert.assertEquals;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.jboss7.JBoss7JulLogEvent;
import org.jboss.logmanager.ExtLogRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class GelfUtilTest {

    @Test public void testProfilingString() throws Exception {

        Map mdcMap = new HashMap();
        mdcMap.put(GelfUtil.MDC_REQUEST_START_MS, "" + (System.currentTimeMillis() - 12000));
        ExtLogRecord extLogRecord = new ExtLogRecord(Level.INFO, "test", "");
        extLogRecord.setMdc(mdcMap);
        GelfMessage message = new GelfMessage();

        GelfUtil.addMdcProfiling(new JBoss7JulLogEvent(extLogRecord), message);

        assertEquals("12sec", message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION));

    }

    @Test public void testProfilingLong() throws Exception {

        Map mdcMap = new HashMap();
        mdcMap.put(GelfUtil.MDC_REQUEST_START_MS, (System.currentTimeMillis() - 12000));
        ExtLogRecord extLogRecord = new ExtLogRecord(Level.INFO, "test", "");
        extLogRecord.setMdc(mdcMap);
        GelfMessage message = new GelfMessage();

        GelfUtil.addMdcProfiling(new JBoss7JulLogEvent(extLogRecord), message);

        assertEquals("12sec", message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION));

    }

    @Test public void addDefaultPortIfMissing() {
        String url = GelfUtil.addDefaultPortIfMissing("http://example.com/foo", String.valueOf(1234));
        assertEquals("http://example.com:1234/foo", url);
        String url2 = GelfUtil.addDefaultPortIfMissing("http://example.com:8080/foo", String.valueOf(1234));
        assertEquals("http://example.com:8080/foo", url2);
    }
}
