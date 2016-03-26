package biz.paluch.logging.gelf.log4j2;

import static org.apache.logging.log4j.core.layout.PatternLayout.newBuilder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

/**
 * Configuration for a log field.
 *
 * @author Mark Paluch
 */
@Plugin(name = "Field", category = "Core", printObject = true)
public class GelfLogField {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private String name;
    private String literal;
    private String mdc;
    private PatternLayout patternLayout;

    public GelfLogField(String name, String literal, String mdc, PatternLayout patternLayout) {
        this.name = name;
        this.literal = literal;
        this.mdc = mdc;
        this.patternLayout = patternLayout;
    }

    public String getName() {
        return name;
    }

    public String getLiteral() {
        return literal;
    }

    public String getMdc() {
        return mdc;
    }

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }

    @PluginFactory
    public static GelfLogField createField(@PluginConfiguration final Configuration config,
            @PluginAttribute("name") String name, @PluginAttribute("literal") String literalValue,
            @PluginAttribute("mdc") String mdc, @PluginAttribute("pattern") String pattern) {

        final boolean isPattern = Strings.isNotEmpty(pattern);
        final boolean isLiteralValue = Strings.isNotEmpty(literalValue);
        final boolean isMDC = Strings.isNotEmpty(mdc);

        if (Strings.isEmpty(name)) {
            LOGGER.error("The name is empty");
            return null;
        }

        if ((isPattern && isLiteralValue) || (isPattern && isMDC) || (isLiteralValue && isMDC)) {
            LOGGER.error("The pattern, literal, and mdc attributes are mutually exclusive.");
            return null;
        }

        if (isPattern) {

            PatternLayout patternLayout = newBuilder().withPattern(pattern).withConfiguration(config)
                    .withNoConsoleNoAnsi(false).withAlwaysWriteExceptions(false).build();

            return new GelfLogField(name, null, null, patternLayout);
        }

        return new GelfLogField(name, literalValue, mdc, null);
    }
}
