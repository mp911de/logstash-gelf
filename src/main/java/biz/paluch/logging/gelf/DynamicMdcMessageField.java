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

    public Pattern getPattern()
    {
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
    public String getName()
    {
        return null;
    }
}
