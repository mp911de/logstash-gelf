package biz.paluch.logging.gelf.standalone;

import java.util.HashMap;
import java.util.Map;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.MessagePostprocessingErrorReporter;

/**
 * Default Gelf sender configuration for standalone use.
 *
 * @author Mark Paluch
 * @since 21.07.14 17:34
 */
public class DefaultGelfSenderConfiguration implements GelfSenderConfiguration {

    private ErrorReporter errorReporter;
    private String host;
    private int port;
    protected Map<String, Object> specificConfigurations = new HashMap<>();

    public DefaultGelfSenderConfiguration() {
        errorReporter = new MessagePostprocessingErrorReporter(new Slf4jErrorReporter());
    }

    public DefaultGelfSenderConfiguration(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Map<String, Object> getSpecificConfigurations() {
        return specificConfigurations;
    }

    public void setSpecificConfigurations(Map<String, Object> specificConfigurations) {
        this.specificConfigurations = specificConfigurations;
    }

}
