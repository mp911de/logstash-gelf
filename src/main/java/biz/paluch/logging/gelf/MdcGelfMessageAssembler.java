package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.GelfMessage;

import java.util.Set;

/**
 * Message-Assembler using MDC.
 * 
 * @author Mark Paluch
 * @since 26.09.13 15:05
 */
public class MdcGelfMessageAssembler extends GelfMessageAssembler {

    public static final String PROPERTY_MDC_PROFILING = "mdcProfiling";
    public static final String PROPERTY_INCLUDE_FULL_MDC = "includeFullMdc";
    public static final String PROPERTY_MDC_FIELD = "mdcField.";
    public static final String PROPERTY_DYNAMIC_MDC_FIELD = "dynamicMdcFields.";

    private boolean mdcProfiling;
    private boolean includeFullMdc;

    public void initialize(PropertyProvider propertyProvider) {

        super.initialize(propertyProvider);
        mdcProfiling = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_MDC_PROFILING));
        includeFullMdc = "true".equalsIgnoreCase(propertyProvider.getProperty(PROPERTY_INCLUDE_FULL_MDC));

    }

    public GelfMessage createGelfMessage(LogEvent logEvent) {

        GelfMessage gelfMessage = super.createGelfMessage(logEvent);
        if (mdcProfiling) {
            GelfUtil.addMdcProfiling(logEvent, gelfMessage);
        }

        if (includeFullMdc) {
            Set<String> mdcNames = logEvent.getMdcNames();
            for (String mdcName : mdcNames) {

                if (mdcName == null) {
                    continue;
                }

                String mdcValue = logEvent.getMdcValue(mdcName);
                if (mdcValue != null) {
                    gelfMessage.addField(mdcName, mdcValue);
                }
            }
        }

        return gelfMessage;
    }

    public boolean isMdcProfiling() {
        return mdcProfiling;
    }

    public void setMdcProfiling(boolean mdcProfiling) {
        this.mdcProfiling = mdcProfiling;
    }

    public boolean isIncludeFullMdc() {
        return includeFullMdc;
    }

    public void setIncludeFullMdc(boolean includeFullMdc) {
        this.includeFullMdc = includeFullMdc;
    }
}
