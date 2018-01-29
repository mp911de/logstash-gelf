package biz.paluch.logging.gelf.log4j2;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.*;
import static org.apache.logging.log4j.core.layout.PatternLayout.newBuilder;

import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

import biz.paluch.logging.RuntimeContainer;
import biz.paluch.logging.gelf.*;
import biz.paluch.logging.gelf.intern.*;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Java-Util-Logging Handler creates GELF Messages and posts
 * them using UDP (default) or TCP. Following parameters are supported/needed:
 * <ul>
 * <li>host (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * <li>See docs for more details</li>
 * </ul>
 * </li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>version (Optional): GELF Version 1.0 or 1.1, default 1.0</li>
 * <li>originHost (Optional): Originating Hostname, default FQDN Hostname</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field (true/false/throwable reference [0 = throwable, 1 = throwable.cause, -1 = root cause]), default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
 * <li>additionalFieldTypes (Optional): Type specification for additional and MDC fields. Supported types: String, long, Long,
 * double, Double and discover (default if not specified, discover field type on parseability). Eg.
 * field=String,field2=double</li>
 * <li>ignoreExceptions (Optional): The default is <code>true</code>, causing exceptions encountered while appending events to
 * be internally logged and then ignored. When set to <code>false</code> exceptions will be propagated to the caller, instead.
 * You must set this to false when wrapping this Appender in a <code>FailoverAppender</code>.</li>
 * </ul>
 *
 * <h2>Fields</h2>
 *
 * <p>
 * Log4j v2 supports an extensive and flexible configuration in contrast to other log frameworks (JUL, log4j v1). This allows
 * you to specify your needed fields you want to use in the GELF message. An empty field configuration results in a message
 * containing only
 * </p>
 *
 * <ul>
 * <li>timestamp</li>
 * <li>level (syslog level)</li>
 * <li>host</li>
 * <li>facility</li>
 * <li>message</li>
 * <li>short_message</li>
 * </ul>
 *
 * <p>
 * You can add different fields:
 * </p>
 *
 * <ul>
 * <li>Static Literals</li>
 * <li>MDC Fields</li>
 * <li>Log-Event fields (using <a href="http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout">Pattern
 * Layout</a>)</li>
 * </ul>
 *
 * In order to do so, use nested Field elements below the Appender element.
 *
 * <h3>Static Literals</h3> <code>
      &lt;Field name="fieldName1" literal="your literal value" /&gt;
 * </code>
 *
 * <h3>MDC Fields</h3> <code>
    &lt;Field name="fieldName1" mdc="name of the MDC entry" /&gt;
 * </code>
 *
 * <h3>Dynamic MDC Fields</h3> <code>
     &lt;DynamicMdcFields regex="mdc.*"  /&gt;
 * </code>
 *
 *
 * <h3>Log-Event fields</h3>
 * <p>
 * See also: <a href="http://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout">Pattern Layout</a>
 * </p>
 *
 * <p>
 * You can use all built-in Pattern Fields:
 * </p>
 * <code>
    &lt;Field name="simpleClassName" pattern="%C{1}" /&gt;
    &lt;Field name="timestamp" pattern="%d{dd MMM yyyy HH:mm:ss,SSS}" /&gt;
    &lt;Field name="level" pattern="%level" /&gt;
 </code>
 *
 * <p>
 * Additionally, you can add the <strong>host</strong>-Field, which can supply you either the FQDN hostname, the simple hostname
 * or the local address.
 * </p>
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" style="border-bottom:1px solid #9eadc0;" summary=
 * "Details for the %host formatter">
 * <tbody>
 * <tr>
 * <th class="colFirst">Option</th>
 * <th class="colLast">Description</th>
 * </tr>
 * <tr class="altColor">
 * <td class="colFirst" align="center"><b>host</b> &nbsp;&nbsp;{["fqdn"<br>
 * &nbsp;&nbsp;|"simple"<br>
 * &nbsp;&nbsp;|"address"]}</td>
 * <td class="colLast">
 * <p>
 * Outputs either the FQDN hostname, the simple hostname or the local address.
 * </p>
 * <p>
 * You can follow the throwable conversion word with an option in the form <b>%host{option}</b>.
 * </p>
 * <p>
 * <b>%host{fqdn}</b> default setting, outputs the FQDN hostname, e.g. www.you.host.name.com.
 * </p>
 * <p>
 * <b>%host{simple}</b> outputs simple hostname, e.g. www.
 * </p>
 * <p>
 * <b>%host{address}</b> outputs the local IP address of the found hostname, e.g. 1.2.3.4 or affe:affe:affe::1.
 * </p>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 *
 *
 * <a name="mdcProfiling"></a>
 * <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one value in the MDC:
 * <ul>
 * <li>profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)</li>
 * </ul>
 * <p>
 * Two values are set by the Log Appender:
 * </p>
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 *
 * The {@link #append(LogEvent)} method is thread-safe and may be called by different threads at any time.
 */
@Plugin(name = "Gelf", category = "Core", elementType = "appender", printObject = true)
public class GelfLogAppender extends AbstractAppender {

    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final ErrorReporter ERROR_REPORTER = new ErrorReporter() {
        @Override
        public void reportError(String message, Exception e) {
            LOGGER.error(message, e, 0);
        }
    };

    private final ErrorReporter PROPAGATING_ERROR_REPORTER = new ErrorReporter() {
        @Override
        public void reportError(String message, Exception e) {

            if (e != null) {
                throw new AppenderLoggingException(e);
            }

            LOGGER.error(message, null, 0);
        }
    };

    protected GelfSender gelfSender;
    private final MdcGelfMessageAssembler gelfMessageAssembler;
    private final ErrorReporter errorReporter;

    public GelfLogAppender(String name, Filter filter, MdcGelfMessageAssembler gelfMessageAssembler, boolean ignoreExceptions) {

        super(name, filter, null, ignoreExceptions);
        this.gelfMessageAssembler = gelfMessageAssembler;

        ErrorReporter errorReporter = getErrorReporter(ignoreExceptions);

        this.errorReporter = new MessagePostprocessingErrorReporter(errorReporter);
    }

    private ErrorReporter getErrorReporter(boolean ignoreExceptions) {
        return ignoreExceptions ? ERROR_REPORTER : PROPAGATING_ERROR_REPORTER;
    }

    @PluginFactory
    public static GelfLogAppender createAppender(@PluginConfiguration final Configuration config,
            @PluginAttribute("name") String name, @PluginElement("Filter") Filter filter,
            @PluginElement("Field") final GelfLogField[] fields,
            @PluginElement("DynamicMdcFields") final GelfDynamicMdcLogFields[] dynamicFieldArray,
            @PluginAttribute("graylogHost") String graylogHost, @PluginAttribute("host") String host,
            @PluginAttribute("graylogPort") String graylogPort, @PluginAttribute("port") String port,
            @PluginAttribute("version") String version, @PluginAttribute("extractStackTrace") String extractStackTrace,
            @PluginAttribute("originHost") String originHost, @PluginAttribute("includeFullMdc") String includeFullMdc,
            @PluginAttribute("facility") String facility, @PluginAttribute("filterStackTrace") String filterStackTrace,
            @PluginAttribute("mdcProfiling") String mdcProfiling,
            @PluginAttribute("maximumMessageSize") String maximumMessageSize,
            @PluginAttribute("additionalFieldTypes") String additionalFieldTypes,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {

        RuntimeContainer.initialize(ERROR_REPORTER);

        MdcGelfMessageAssembler mdcGelfMessageAssembler = new MdcGelfMessageAssembler();

        if (name == null) {
            LOGGER.error("No name provided for " + GelfLogAppender.class.getSimpleName());
            return null;
        }

        if (Strings.isEmpty(host) && Strings.isEmpty(graylogHost)) {
            LOGGER.error("No host provided for " + GelfLogAppender.class.getSimpleName());
            return null;
        }

        if (Strings.isNotEmpty(host)) {
            mdcGelfMessageAssembler.setHost(host);
        }

        if (Strings.isNotEmpty(graylogHost)) {
            mdcGelfMessageAssembler.setHost(graylogHost);
        }

        if (Strings.isNotEmpty(port)) {
            mdcGelfMessageAssembler.setPort(Integer.parseInt(port));
        }

        if (Strings.isNotEmpty(graylogPort)) {
            mdcGelfMessageAssembler.setPort(Integer.parseInt(graylogPort));
        }

        if (Strings.isNotEmpty(version)) {
            mdcGelfMessageAssembler.setVersion(version);
        }

        if (Strings.isNotEmpty(originHost)) {
            PatternLayout patternLayout = newBuilder().withPattern(originHost).withConfiguration(config)
                    .withNoConsoleNoAnsi(false).withAlwaysWriteExceptions(false).build();

            mdcGelfMessageAssembler.setOriginHost(patternLayout.toSerializable(new Log4jLogEvent()));
        }

        if (facility != null) {
            mdcGelfMessageAssembler.setFacility(facility);
        }

        if (extractStackTrace != null) {
            mdcGelfMessageAssembler.setExtractStackTrace(extractStackTrace);
        }

        if (filterStackTrace != null) {
            mdcGelfMessageAssembler.setFilterStackTrace("true".equals(filterStackTrace));
        }

        if (mdcProfiling != null) {
            mdcGelfMessageAssembler.setMdcProfiling("true".equals(mdcProfiling));
        }

        if (includeFullMdc != null) {
            mdcGelfMessageAssembler.setIncludeFullMdc("true".equals(includeFullMdc));
        }

        if (maximumMessageSize != null) {
            mdcGelfMessageAssembler.setMaximumMessageSize(Integer.parseInt(maximumMessageSize));
        }

        if (additionalFieldTypes != null) {
            ConfigurationSupport.setAdditionalFieldTypes(additionalFieldTypes, mdcGelfMessageAssembler);
        }

        configureFields(mdcGelfMessageAssembler, fields, dynamicFieldArray);

        return new GelfLogAppender(name, filter, mdcGelfMessageAssembler, ignoreExceptions);
    }

    /**
     * Configure fields (literals, MDC, layout).
     *
     * @param mdcGelfMessageAssembler the assembler
     * @param fields static field array
     * @param dynamicFieldArray dynamic field array
     */
    private static void configureFields(MdcGelfMessageAssembler mdcGelfMessageAssembler, GelfLogField[] fields,
            GelfDynamicMdcLogFields[] dynamicFieldArray) {

    	if (fields == null || fields.length == 0) {
            mdcGelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName,
                    SourceMethodName, SourceLineNumber, SourceSimpleClassName, LoggerName, Marker));
            return;
        }

        for (GelfLogField field : fields) {

            if (Strings.isNotEmpty(field.getMdc())) {
                mdcGelfMessageAssembler.addField(new MdcMessageField(field.getName(), field.getMdc()));
            }

            if (Strings.isNotEmpty(field.getLiteral())) {
                mdcGelfMessageAssembler.addField(new StaticMessageField(field.getName(), field.getLiteral()));
            }

            if (field.getPatternLayout() != null) {
                mdcGelfMessageAssembler.addField(new PatternLogMessageField(field.getName(), null, field.getPatternLayout()));
            }
        }

        if (dynamicFieldArray != null) {
            for (GelfDynamicMdcLogFields gelfDynamicMdcLogFields : dynamicFieldArray) {
                mdcGelfMessageAssembler.addField(new DynamicMdcMessageField(gelfDynamicMdcLogFields.getRegex()));
            }
        }
    }

    @Override
    public void append(LogEvent event) {

    	if (event == null) {
            return;
        }

        try {
            GelfMessage message = createGelfMessage(event);
            if (!message.isValid()) {
                reportError("GELF Message is invalid: " + message.toJson(), null);
                return;
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null);
            }
        } catch (AppenderLoggingException e) {
            throw e;
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e);
        }
    }

    protected GelfMessage createGelfMessage(final LogEvent logEvent) {
        return gelfMessageAssembler.createGelfMessage(new Log4j2LogEvent(logEvent));
    }

    public void reportError(String message, Exception exception) {
        errorReporter.reportError(message, exception);
    }

    @Override
    public void stop() {
        if (null != gelfSender) {
            Closer.close(gelfSender);
            gelfSender = null;
        }
        super.stop();
    }

    @Override
    public void start() {
        if (null == gelfSender) {
            gelfSender = createGelfSender();
        }
        super.start();
    }

    protected GelfSender createGelfSender() {
        return GelfSenderFactory.createSender(gelfMessageAssembler, errorReporter, Collections.EMPTY_MAP);
    }
}
