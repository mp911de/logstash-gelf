package biz.paluch.logging.gelf.log4j;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.MdcLogEvent;
import biz.paluch.logging.gelf.PropertyProvider;
import biz.paluch.logging.gelf.intern.GelfMessage;
import org.apache.log4j.MDC;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:05
 */
public class MdcGelfMessageAssembler extends GelfMessageAssembler {

    public static final String PROPERTY_MDC_PROFILING = "mdcProfiling";
    public static final String PROPERTY_MDC_FIELD = "mdcField.";

    private Set<String> mdcFields;
    private boolean mdcProfiling;

    public void initialize(PropertyProvider propertyProvider) {

        super.initialize(propertyProvider);
        mdcProfiling = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_MDC_PROFILING));

        setupMdcFields(propertyProvider);

    }

    public GelfMessage createGelfMessage(MdcLogEvent logEvent) {

        GelfMessage gelfMessage = super.createGelfMessage(logEvent);
        if (mdcProfiling) {
            GelfUtil.addMdcProfiling(gelfMessage);
        }

        for (String mdcField : mdcFields) {
            Object value = MDC.get(mdcField);
            if (value != null && !value.toString().equals("")) {
                gelfMessage.addField(mdcField, value.toString());
            }
        }

        return gelfMessage;
    }

    private void setupMdcFields(PropertyProvider propertyProvider) {
        int fieldNumber = 0;
        mdcFields = new HashSet<String>();
        while (true) {
            final String property = propertyProvider.getProperty(PROPERTY_MDC_FIELD + fieldNumber);
            if (null == property) {
                break;
            }
            mdcFields.add(property);

            fieldNumber++;
        }
    }

    public Set<String> getMdcFields() {
        return mdcFields;
    }

    public void setMdcFields(Set<String> mdcFields) {
        this.mdcFields = mdcFields;
    }

    public boolean isMdcProfiling() {
        return mdcProfiling;
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        this.mdcProfiling = mdcProfiling;
    }

}
