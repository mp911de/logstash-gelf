package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Message-Assembler using MDC.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:05
 */
public class MdcGelfMessageAssembler extends GelfMessageAssembler {

    public static final String PROPERTY_MDC_PROFILING = "mdcProfiling";
    public static final String PROPERTY_MDC_FIELD = "mdcField.";

    private boolean mdcProfiling;

    public void initialize(PropertyProvider propertyProvider) {

        super.initialize(propertyProvider);
        mdcProfiling = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_MDC_PROFILING));

        setupMdcFields(propertyProvider);

    }

    public GelfMessage createGelfMessage(LogEvent logEvent) {

        GelfMessage gelfMessage = super.createGelfMessage(logEvent);
        if (mdcProfiling) {
            GelfUtil.addMdcProfiling(logEvent, gelfMessage);
        }

        return gelfMessage;
    }

    private void setupMdcFields(PropertyProvider propertyProvider) {
        int fieldNumber = 0;
        while (true) {
            final String property = propertyProvider.getProperty(PROPERTY_MDC_FIELD + fieldNumber);
            if (null == property) {
                break;
            }

            MdcMessageField field = new MdcMessageField(property, property);
            addField(field);
            fieldNumber++;
        }
    }

    public boolean isMdcProfiling() {
        return mdcProfiling;
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        this.mdcProfiling = mdcProfiling;
    }

}
