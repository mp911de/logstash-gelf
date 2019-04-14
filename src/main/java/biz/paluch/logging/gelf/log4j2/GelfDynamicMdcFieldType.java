package biz.paluch.logging.gelf.log4j2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

import java.util.regex.Pattern;

/**
 * Configuration for dynamic log fields pulled from MDC.
 *
 * @author Thomas Herzog
 */
@Plugin(name = "DynamicMdcFieldTypes", category = "Core", printObject = true)
public class GelfDynamicMdcFieldType {

    private static final Logger LOGGER = StatusLogger.getLogger();

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
    public static GelfDynamicMdcFieldType createField(@PluginConfiguration final Configuration config,
                                                      @PluginAttribute("regex") String regex,
                                                      @PluginAttribute("type") String type) {

        Pattern pattern;
        if (Strings.isEmpty(regex) || Strings.isEmpty(type)) {
            throw new IllegalArgumentException(String.format("regex=%s or type=%s is empty or null", regex, type));
        }
        try {
            pattern = Pattern.compile(regex);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Regex is invalid: regex=%s", regex), e);
        }

        return new GelfDynamicMdcFieldType(pattern, type);
    }
}
