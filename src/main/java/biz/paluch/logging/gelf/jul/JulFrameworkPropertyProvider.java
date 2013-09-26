package biz.paluch.logging.gelf.jul;

import biz.paluch.logging.gelf.FrameworkPropertyProvider;

import java.util.logging.LogManager;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:04
 */
public class JulFrameworkPropertyProvider implements FrameworkPropertyProvider
{

    private final Class<?> configurationBase;
    private final String prefix;
    private final LogManager logManager;

    public JulFrameworkPropertyProvider(Class<?> configurationBase) {
        this.configurationBase = configurationBase;

        prefix = getClass().getName();
        logManager = LogManager.getLogManager();

    }

    @Override
    public String getProperty(String propertyName) {
        return logManager.getProperty(prefix + "." + propertyName);
    }
}
