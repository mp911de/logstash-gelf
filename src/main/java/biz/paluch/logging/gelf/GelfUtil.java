package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.GelfMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
        Object requestStartMs = logEvent.getMdcValue(MDC_REQUEST_START_MS);
        long timestamp = -1;

        if (requestStartMs instanceof Long) {
            timestamp = ((Long) requestStartMs).longValue();
        }

        if (timestamp == -1 && requestStartMs instanceof String) {
            String requestStartMsString = (String) requestStartMs;
            if (requestStartMsString.length() == 0) {
                return;
            }
            timestamp = Long.parseLong(requestStartMsString);
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

    public static String addDefaultPortIfMissing(String urlString, String defaultPort) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return urlString;
        }
        if (url.getPort() != -1) {
            return urlString;
        }
        String regex = "http://([^/]+)";
        String found = getFirstFound(urlString, regex);
        String replacer = "http://" + found + ":" + defaultPort;

        if (!isEmpty(found)) {
            urlString = urlString.replaceFirst(regex, replacer);
        }
        return urlString;
    }

    public static String getFirstFound(String contents, String regex) {
        List<String> founds = getFound(contents, regex);
        if (isEmpty(founds)) {
            return null;
        }
        return founds.get(0);
    }

    public static List<String> getFound(String contents, String regex) {
        if (isEmpty(regex) || isEmpty(contents)) {
            return null;
        }
        List<String> results = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(contents);

        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                results.add(matcher.group(1));
            } else {
                results.add(matcher.group());
            }
        }
        return results;
    }

    public static boolean isEmpty(List<String> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        if (list.size() == 1 && isEmpty(list.get(0))) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(String str) {
        if (str != null && str.trim().length() > 0) {
            return false;
        }
        return true;
    }
}
