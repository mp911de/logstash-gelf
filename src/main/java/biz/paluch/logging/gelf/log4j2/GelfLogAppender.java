package biz.paluch.logging.gelf.log4j2;

import biz.paluch.logging.gelf.intern.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

import biz.paluch.logging.gelf.DynamicMdcMessageField;
import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.MdcMessageField;
import biz.paluch.logging.gelf.StaticMessageField;

/**
 * Logging-Handler for GELF (Graylog Extended Logging Format). This Java-Util-Logging Handler creates GELF Messages and posts
 * them using UDP (default) or TCP. Following parameters are supported/needed:
 * <p/>
 * <ul>
 * <li>host (Mandatory): Hostname/IP-Address of the Logstash Host
 * <ul>
 * <li>tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com</li>
 * <li>udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com</li>
 * <li>(the host) for UDP, e.g. 127.0.0.1 or some.host.com</li>
 * </ul>
 * </li>
 * <li>port (Optional): Port, default 12201</li>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC
 * Profiling</a>, default false</li>
 * <li>facility (Optional): Name of the Facility, default gelf-java</li>
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
 * </br>
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
 * </br>
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
    &lt;Field name="simpleClassName" pattern="%C{1}" /&gt;<br/>
    &lt;Field name="timestamp" pattern="%d{dd MMM yyyy HH:mm:ss,SSS}" /&gt;<br/>
    &lt;Field name="level" pattern="%level" /&gt;
 </code>
 * 
 * <p>
 * Additionally, you can add the <strong>host</strong>-Field, which can supply you either the FQDN hostname, the simple hostname
 * or the local address.
 * </p>
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" style="border-bottom:1px solid #9eadc0;">
 * <tbody>
 * <tr>
 * <th class="colFirst">Option</th>
 * <th class="colLast">Description</th>
 * </tr>
 * <tr class="altColor">
 * <td class="colFirst" align="center">
 * <b>host</b><br>
 * &nbsp;&nbsp;{["fqdn"<br>
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
 * <p/>
 * <a name="mdcProfiling"></a>
 * <h2>MDC Profiling</h2>
 * <p>
 * MDC Profiling allows to calculate the runtime from request start up to the time until the log message was generated. You must
 * set one value in the MDC:
 * <ul>
 * <li>profiling.requestStart.millis: Time Millis of the Request-Start (Long or String)</li>
 * </ul>
 * <p/>
 * Two values are set by the Log Appender:
 * <ul>
 * <li>profiling.requestEnd: End-Time of the Request-End in Date.toString-representation</li>
 * <li>profiling.requestDuration: Duration of the request (e.g. 205ms, 16sec)</li>
 * </ul>
 * <p/>
 * </p>
 */
@Plugin(name = "Gelf", category = "Core", elementType = "appender", printObject = true)
public class GelfLogAppender extends AbstractAppender implements ErrorReporter {
    private static final Logger LOGGER = StatusLogger.getLogger();

    protected GelfSender gelfSender;
    private MdcGelfMessageAssembler gelfMessageAssembler;

    public GelfLogAppender(String name, Filter filter, MdcGelfMessageAssembler gelfMessageAssembler) {
        super(name, filter, null);
        this.gelfMessageAssembler = gelfMessageAssembler;
    }

    /**
     * 
     * @param name
     * @param filter
     * @param fields
     * @param graylogHost
     * @param host
     * @param graylogPort
     * @param port
     * @param extractStackTrace
     * @param facility
     * @param filterStackTrace
     * @param mdcProfiling
     * @param maximumMessageSize
     * @return GelfLogAppender
     */
    @PluginFactory
    public static GelfLogAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter,
            @PluginElement("Field") final GelfLogField[] fields,
            @PluginElement("DynamicMdcFields") final GelfDynamicMdcLogFields[] dynamicFieldArray,
            @PluginAttribute("graylogHost") String graylogHost, @PluginAttribute("host") String host,
            @PluginAttribute("graylogPort") String graylogPort, @PluginAttribute("port") String port,
            @PluginAttribute("extractStackTrace") String extractStackTrace,
            @PluginAttribute("includeFullMdc") String includeFullMdc, @PluginAttribute("facility") String facility,
            @PluginAttribute("filterStackTrace") String filterStackTrace, @PluginAttribute("mdcProfiling") String mdcProfiling,
            @PluginAttribute("maximumMessageSize") String maximumMessageSize) {

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

        if (facility != null) {
            mdcGelfMessageAssembler.setFacility(facility);
        }

        if (extractStackTrace != null) {
            mdcGelfMessageAssembler.setExtractStackTrace(extractStackTrace.equals("true"));
        }

        if (filterStackTrace != null) {
            mdcGelfMessageAssembler.setFilterStackTrace(filterStackTrace.equals("true"));
        }

        if (mdcProfiling != null) {
            mdcGelfMessageAssembler.setMdcProfiling(mdcProfiling.equals("true"));
        }

        if (includeFullMdc != null) {
            mdcGelfMessageAssembler.setIncludeFullMdc(includeFullMdc.equals("true"));
        }

        if (maximumMessageSize != null) {
            mdcGelfMessageAssembler.setMaximumMessageSize(Integer.parseInt(maximumMessageSize));
        }

        configureFields(mdcGelfMessageAssembler, fields, dynamicFieldArray);

        GelfLogAppender result = new GelfLogAppender(name, filter, mdcGelfMessageAssembler);

        return result;

    }

    /**
     * Configure fields (literals, MDC, layout).
     * 
     * @param mdcGelfMessageAssembler
     * @param fields
     * @param dynamicFieldArray
     */
    private static void configureFields(MdcGelfMessageAssembler mdcGelfMessageAssembler, GelfLogField[] fields,
            GelfDynamicMdcLogFields[] dynamicFieldArray) {
        if (fields == null) {
            mdcGelfMessageAssembler.addFields(LogMessageField.getDefaultMapping());
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
            }

            if (null == gelfSender || !gelfSender.sendMessage(message)) {
                reportError("Could not send GELF message", null);
            }
        } catch (Exception e) {
            reportError("Could not send GELF message: " + e.getMessage(), e);
        }
    }

    protected GelfMessage createGelfMessage(final LogEvent logEvent) {
        return gelfMessageAssembler.createGelfMessage(new Log4j2LogEvent(logEvent));
    }

    public void reportError(String message, Exception exception) {
        LOGGER.error(message, exception, 0);
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
            gelfSender = GelfSenderFactory.createSender(gelfMessageAssembler, this);
        }
        super.start();
    }
}
