package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Field with reference to the log event.
 */
public class LogMessageField implements MessageField {

    private static final String DEFAULT_MAPPING = "default-logstash-fields.properties";

    /**
     * Named references to common log event fields.
     */
    public static enum NamedLogField {
        Time("Time"), Severity("Severity"), ThreadName("Thread"), SourceClassName("SourceClassName"), SourceSimpleClassName(
                "SourceSimpleClassName"), SourceMethodName("SourceMethodName"), Server("Server"), LoggerName("LoggerName"), Marker(
                "Marker"), NDC("NDC");

        private final String fieldName;

        NamedLogField(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public static NamedLogField byName(String name) {
            for (NamedLogField namedLogField : values()) {
                if (namedLogField.name().equalsIgnoreCase(name)) {
                    return namedLogField;
                }
            }

            return null;
        }
    }

    private String name;
    private NamedLogField namedLogField;

    public LogMessageField(String name, NamedLogField namedLogField) {
        this.namedLogField = namedLogField;
        this.name = name;
    }

    public NamedLogField getNamedLogField() {
        return namedLogField;
    }

    @Override
    public String getName() {
        return name;
    }

    public static List<LogMessageField> getDefaultMapping(NamedLogField... supportedFields) {

        List<LogMessageField> result = new ArrayList<LogMessageField>();
        List<NamedLogField> supportedLogFields = Arrays.asList(supportedFields);
        InputStream is = null;
        try {
            is = getStream();

            if (is == null) {
                System.out.println("No " + DEFAULT_MAPPING + " resource present, using defaults");
            } else {
                Properties p = new Properties();
                p.load(is);

                if (!p.isEmpty()) {
                    loadFields(p, result, supportedLogFields);
                }

            }

        } catch (IOException e) {
            System.out.println("Could not parse " + DEFAULT_MAPPING + " resource, using defaults");
        } finally {
            Closer.close(is);
        }

        if (result.isEmpty()) {

            for (NamedLogField namedLogField : NamedLogField.values()) {
                if (supportedLogFields.contains(namedLogField)) {
                    result.add(new LogMessageField(namedLogField.fieldName, namedLogField));
                }
            }
        }

        return result;
    }

    private static void loadFields(Properties p, List<LogMessageField> result, List<NamedLogField> supportedLogFields) {
        for (Map.Entry<Object, Object> entry : p.entrySet()) {

            String targetName = entry.getKey().toString();
            String sourceFieldName = entry.getValue().toString();

            NamedLogField namedLogField = NamedLogField.byName(sourceFieldName);
            if (namedLogField != null && supportedLogFields.contains(namedLogField)) {
                result.add(new LogMessageField(targetName, namedLogField));
            }
        }
    }

    private static InputStream getStream() {

        Thread thread = Thread.currentThread();
        InputStream is = LogMessageField.class.getResourceAsStream(DEFAULT_MAPPING);
        if (is == null && thread.getContextClassLoader() != null) {
            is = thread.getContextClassLoader().getResourceAsStream(DEFAULT_MAPPING);
        }
        return is;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append(" [name='").append(name).append('\'');
        sb.append(", namedLogField=").append(namedLogField);
        sb.append(']');
        return sb.toString();
    }
}
