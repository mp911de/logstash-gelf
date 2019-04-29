package biz.paluch.logging.gelf.log4j2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

/**
 * Configuration for dynamic log fields pulled from MDC.
 *
 * @author Mark Paluch
 */
@Plugin(name = "DynamicMdcFieldTypes", category = "Core", printObject = true)
public class GelfDynamicMdcLogFieldTypes {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private final String regex;
    private final String type;

    public GelfDynamicMdcLogFieldTypes(String regex, String type) {
        this.regex = regex;
        this.type = type;
    }

    public String getRegex() {
        return regex;
    }

    public String getType() {
        return type;
    }

    @PluginFactory
    public static GelfDynamicMdcLogFieldTypes createField(@PluginConfiguration final Configuration config,
                                                          @PluginAttribute("regex") String regex,
                                                          @PluginAttribute("type") String type) {

        if (Strings.isEmpty(regex) || Strings.isEmpty(type)) {
            LOGGER.error(String.format("regex=%s or type=%s is empty", regex, type));
            return null;
        }

        return new GelfDynamicMdcLogFieldTypes(regex, type);
    }
}
