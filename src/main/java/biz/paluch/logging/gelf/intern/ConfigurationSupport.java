package biz.paluch.logging.gelf.intern;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.StaticMessageField;

/**
 * @author Mark Paluch
 */
public class ConfigurationSupport {
    public static final String MULTI_VALUE_DELIMITTER = ",";
    public static final char EQ = '=';

    private ConfigurationSupport() {
    }

    /**
     * Set the additional (static) fields.
     * 
     * @param spec field=value,field1=value1, ...
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setAdditionalFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] properties = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : properties) {
                final int index = field.indexOf(EQ);
                if (-1 == index) {
                    continue;
                }
                gelfMessageAssembler.addField(new StaticMessageField(field.substring(0, index), field.substring(index + 1)));
            }
        }
    }

    /**
     * Set the MDC fields.
     * 
     * @param spec field, field2, field3
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setMdcFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] fields = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : fields) {
                gelfMessageAssembler.addField(new MdcMessageField(field.trim(), field.trim()));
            }
        }
    }

    /**
     * Set the dynamic MDC fields.
     * 
     * @param spec field, .*FieldSuffix, fieldPrefix.*
     * @param gelfMessageAssembler the {@link GelfMessageAssembler}.
     */
    public static void setDynamicMdcFields(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] fields = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : fields) {
                gelfMessageAssembler.addField(new DynamicMdcMessageField(field.trim()));
            }
        }
    }

    /**
     * Set the additional field types.
     * 
     * @param spec field=String,field1=Double, ... See {@link GelfMessage} for supported types.
     * @param gelfMessageAssembler the Gelf message assembler to apply the configuration
     */
    public static void setAdditionalFieldTypes(String spec, GelfMessageAssembler gelfMessageAssembler) {
        if (null != spec) {
            String[] properties = spec.split(MULTI_VALUE_DELIMITTER);

            for (String field : properties) {
                final int index = field.indexOf(EQ);
                if (-1 != index) {
                    gelfMessageAssembler.setAdditionalFieldType(field.substring(0, index), field.substring(index + 1));
                }
            }
        }
    }

}
