package biz.paluch.logging.gelf;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.02.14 09:56
 */
public class DynamicMdcMessageField implements MessageField {
    private String regex;
    private Pattern pattern;

    public DynamicMdcMessageField(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append(" [regex='").append(regex).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicMdcMessageField)) {
            return false;
        }

        DynamicMdcMessageField that = (DynamicMdcMessageField) o;

        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }
        if (regex != null ? !regex.equals(that.regex) : that.regex != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = regex != null ? regex.hashCode() : 0;
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        return result;
    }
}
