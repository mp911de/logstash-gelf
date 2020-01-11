package biz.paluch.logging.gelf.log4j2;

import java.util.regex.Pattern;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.Strings;

/**
 * Configuration for dynamic log fields pulled from MDC.
 *
 * @author Thomas Herzog
 */
@Plugin(name = "DynamicMdcFieldType", category = "Core", printObject = true)
public class GelfDynamicMdcFieldType {

    private final Pattern pattern;
    private final String type;

    public GelfDynamicMdcFieldType(Pattern pattern, String type) {
        this.pattern = pattern;
        this.type = type;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getType() {
        return type;
    }

    @PluginFactory
    public static GelfDynamicMdcFieldType createField(@PluginAttribute("regex") String regex,
            @PluginAttribute("type") String type) {

        if (Strings.isEmpty(regex)) {
            throw new IllegalArgumentException("Regex is empty");
        }

        if (Strings.isEmpty(type)) {
            throw new IllegalArgumentException("Type is empty");
        }

        return new GelfDynamicMdcFieldType(Pattern.compile(regex), type);
    }
}
