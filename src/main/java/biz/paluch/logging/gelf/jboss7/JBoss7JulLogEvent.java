package biz.paluch.logging.gelf.jboss7;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.MessageField;
import biz.paluch.logging.gelf.Values;
import biz.paluch.logging.gelf.jul.JulLogEvent;
import org.apache.log4j.MDC;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogRecord;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 18:32
 */
public class JBoss7JulLogEvent extends JulLogEvent {

    public JBoss7JulLogEvent(LogRecord logRecord) {
        super(logRecord);
    }

    @Override
    public Values getValues(MessageField field) {
        if (field instanceof MdcMessageField) {
            return new Values(field.getName(), getValue((MdcMessageField) field));
        }

        if (field instanceof DynamicMdcMessageField) {
            return getMdcValues((DynamicMdcMessageField) field);
        }

        return super.getValues(field);
    }

    private Values getMdcValues(DynamicMdcMessageField field) {
        Values result = new Values();

        Set<String> mdcNames = getAllMdcNames();

        Set<String> matchingMdcNames = GelfUtil.getMatchingMdcNames(field, mdcNames);

        for (String mdcName : matchingMdcNames) {
            String mdcValue = getMdcValue(mdcName);
            if (mdcName != null) {
                result.setValue(mdcName, mdcValue);
            }
        }

        return result;
    }

    private Set<String> getAllMdcNames() {
        Set<String> mdcNames = new HashSet<String>();

        if (MDC.getContext() != null) {
            mdcNames.addAll(MDC.getContext().keySet());
        }

        if (org.slf4j.MDC.getCopyOfContextMap() != null) {
            mdcNames.addAll(org.slf4j.MDC.getCopyOfContextMap().keySet());
        }
        return mdcNames;
    }

    private String getValue(MdcMessageField field) {

        return getMdcValue(field.getMdcName());
    }

    @Override
    public String getMdcValue(String mdcName) {
        Object value = MDC.get(mdcName);
        if (value != null) {
            return value.toString();
        }
        String slf4jValue = org.slf4j.MDC.get(mdcName);
        return slf4jValue;
    }

    @Override
    public Set<String> getMdcNames() {
        return getAllMdcNames();
    }
}
