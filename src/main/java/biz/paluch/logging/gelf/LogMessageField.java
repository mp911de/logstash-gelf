package biz.paluch.logging.gelf;

import java.util.ArrayList;
import java.util.List;

/**
 * Field with reference to the log event.
 */
public class LogMessageField implements MessageField {

    /**
     * Named references to common log event fields.
     */
    public static enum NamedLogField {
        Time("Time"), Severity("Severity"), ThreadName("Thread"), SourceClassName("SourceClassName"), SourceSimpleClassName(
                "SourceSimpleClassName"), SourceMethodName("SourceMethodName"), Server("Server");

        private final String fieldName;

        NamedLogField(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
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

    public static List<LogMessageField> getDefaultMapping() {

        List<LogMessageField> result = new ArrayList<LogMessageField>();

        for (NamedLogField namedLogField : NamedLogField.values()) {
            result.add(new LogMessageField(namedLogField.fieldName, namedLogField));
        }

        return result;
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
