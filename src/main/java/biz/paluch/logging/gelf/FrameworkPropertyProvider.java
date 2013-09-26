package biz.paluch.logging.gelf;

/**
 * Provides access to Log-Framework properties.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:04
 */
public interface FrameworkPropertyProvider
{
    String getProperty(String propertyName);
}
