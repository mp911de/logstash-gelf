package biz.paluch.logging.gelf;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 17.09.13 10:47
 */
public class GelfUtil {

    /**
     * Profiling Start in MDC (msec).
     */
    public static final String MDC_REQUEST_START_MS = "profiling.requestStart.millis";
    /**
     * Profiling End in MDC.
     */
    public static final String MDC_REQUEST_END = "profiling.requestEnd";

    /**
     * Profiling Duration in MDC.
     */
    public static final String MDC_REQUEST_DURATION = "profiling.requestDuration";

    public static final String MDC_REQUEST_DURATION_MILLIS = "profiling.requestDuration.millis";

    private GelfUtil() {

    }

    public static void addMdcProfiling(LogEvent logEvent, GelfMessage gelfMessage) {

		String requestStartMs = logEvent.getMdcValue(MDC_REQUEST_START_MS);
        long timestamp;

        if (requestStartMs != null && !requestStartMs.isEmpty()) {
            timestamp = Long.parseLong(requestStartMs);
        } else {
            return;
        }

        if (timestamp > 0) {
            long now = System.currentTimeMillis();
            long durationMs = now - timestamp;

            String durationText;

            if (durationMs > 10000) {
                long durationSec = durationMs / 1000;
                durationText = durationSec + "sec";
            } else {
                durationText = durationMs + "ms";
            }
            gelfMessage.addField(MDC_REQUEST_DURATION, durationText);
            gelfMessage.addField(MDC_REQUEST_DURATION_MILLIS, "" + durationMs);
            gelfMessage.addField(MDC_REQUEST_END, new Date(now).toString());
        }
    }

    public static String getSimpleClassName(String className) {

        if (className == null) {
            return null;
        }

        int index = className.lastIndexOf('.');
        if (index != -1) {
            return className.substring(index + 1);
        }
        return className;
    }

    public static Set<String> getMatchingMdcNames(DynamicMdcMessageField field, Set<String> mdcNames) {
        Set<String> matchingMdcNames = new HashSet<String>();

        for (String mdcName : mdcNames) {
            if (field.getPattern().matcher(mdcName).matches()) {
                matchingMdcNames.add(mdcName);
            }
        }
        return matchingMdcNames;
    }
}
