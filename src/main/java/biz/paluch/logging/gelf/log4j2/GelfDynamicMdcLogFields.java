package biz.paluch.logging.gelf.log4j2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Strings;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * Configuration for dynamic log fields pulled from MDC.
 */
@Plugin(name = "DynamicMdcFields", category = "Core", printObject = true)
public class GelfDynamicMdcLogFields {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private String regex;

    public GelfDynamicMdcLogFields(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    @PluginFactory
    public static GelfDynamicMdcLogFields createField(@PluginConfiguration final Configuration config,
            @PluginAttribute("regex") String regex) {

        if (Strings.isEmpty(regex)) {
            LOGGER.error("The regex is empty");
            return null;
        }

        return new GelfDynamicMdcLogFields(regex);
    }
}
