package biz.paluch.logging.gelf.jul;

import java.util.logging.LogManager;

import biz.paluch.logging.gelf.PropertyProvider;

/**
 * @author Mark Paluch
 * @since 26.09.13 15:04
 */
public class JulPropertyProvider implements PropertyProvider {

    private final Class<?> configurationBase;
    private final String prefix;
    private final LogManager logManager;

    public JulPropertyProvider(Class<?> configurationBase) {
        this.configurationBase = configurationBase;

        prefix = configurationBase.getName();
        logManager = LogManager.getLogManager();

    }

    @Override
    public String getProperty(String propertyName) {
        return logManager.getProperty(prefix + "." + propertyName);
    }
}
