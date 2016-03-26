package biz.paluch.logging.gelf.log4j2;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import biz.paluch.logging.RuntimeContainer;

/**
 * Provides the servername/Hostname.
 *
 * <table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" style="border-bottom:1px solid #9eadc0;" summary="Details for the %host formatter">
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
 * @author Mark Paluch
 */
@Plugin(name = "HostnameConverter", category = "Converter")
@ConverterKeys({ "host", })
public class HostnameConverter extends LogEventPatternConverter {

    public HostnameConverter(String style) {
        super(HostnameConverter.class.getSimpleName(), style);
    }

    public static HostnameConverter newInstance(String[] format) {
        String style = null;
        if (format.length > 0) {
            style = format[0];
        }
        return new HostnameConverter(style);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        if (getStyle() == null || getStyle().equals("") || getStyle().equals("fqdn")) {
            toAppendTo.append(RuntimeContainer.FQDN_HOSTNAME);
        }

        if (getStyle() != null && getStyle().equals("simple")) {
            toAppendTo.append(RuntimeContainer.HOSTNAME);

        }

        if (getStyle() != null && getStyle().equals("address")) {
            toAppendTo.append(RuntimeContainer.ADDRESS);
        }
    }

    public String getStyle() {
        return getStyleClass(null);
    }
}
