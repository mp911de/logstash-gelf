package biz.paluch.logging.gelf.intern.sender;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * @author Mark Paluch
 */
class UriParser {
    private static final Map<String, TimeUnit> TIME_UNIT_MAP;

    static {
        Map<String, TimeUnit> unitMap = new HashMap<String, TimeUnit>();
        unitMap.put("ns", TimeUnit.NANOSECONDS);
        unitMap.put("us", TimeUnit.MICROSECONDS);
        unitMap.put("ms", TimeUnit.MILLISECONDS);
        unitMap.put("s", TimeUnit.SECONDS);
        unitMap.put("m", TimeUnit.MINUTES);
        unitMap.put("h", TimeUnit.HOURS);
        unitMap.put("d", TimeUnit.DAYS);
        TIME_UNIT_MAP = Collections.unmodifiableMap(unitMap);
    }

    /**
     * Parse the query part of an {@link URI} to a single-valued key-value map. All keys are tranformed to lower-case.
     * 
     * @param uri
     * @return the key-value map.
     */
    static Map<String, String> parse(URI uri) {
        Map<String, String> result = new HashMap<String, String>();

        String queryString = uri.getQuery();
        if (queryString == null && uri.getSchemeSpecificPart() != null && uri.getSchemeSpecificPart().contains("?")) {
            queryString = uri.getSchemeSpecificPart().substring(uri.getSchemeSpecificPart().indexOf('?') + 1);
        }

        if (queryString == null) {
            return result;
        }

        StringTokenizer st = new StringTokenizer(queryString, "&;");
        while (st.hasMoreTokens()) {
            String queryParam = st.nextToken();
            int equalsIndex = queryParam.indexOf('=');
            if (equalsIndex != -1) {
                String key = queryParam.substring(0, equalsIndex);
                String value = queryParam.substring(equalsIndex + 1);

                result.put(key.toLowerCase(), value);
            }
        }

        return result;
    }

    static long getTimeAsMs(Map<String, String> map, String key, long defaultTimeMs) {
        String value = map.get(key.toLowerCase());
        if (value == null || value.trim().equals("")) {
            return defaultTimeMs;
        }

        int numbersEnd = 0;
        while (numbersEnd < value.length() && Character.isDigit(value.charAt(numbersEnd))) {
            numbersEnd++;
        }

        if (numbersEnd == 0) {
            return defaultTimeMs;
        }
        String numbers = value.substring(0, numbersEnd);
        long timeValue = Long.parseLong(numbers);

        String suffix = value.substring(numbersEnd);
        TimeUnit timeoutUnit = TIME_UNIT_MAP.get(suffix);
        if (timeoutUnit == null) {
            timeoutUnit = TimeUnit.MILLISECONDS;
        }

        return timeoutUnit.toMillis(timeValue);
    }

    public static int getInt(Map<String, String> map, String key, int defaultValue) {
        String value = map.get(key.toLowerCase());
        if (value == null || value.trim().equals("")) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static boolean getString(Map<String, String> map, String key, boolean defaultValue) {
        String value = map.get(key.toLowerCase());
        if (value == null || value.trim().equals("")) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }

    public static String getHost(URI uri) {

        String host = uri.getHost();
        if (host == null) {
            host = uri.getSchemeSpecificPart();
        }

        if (host.contains("?")) {
            host = host.substring(0, host.indexOf("?"));
        }
        return host;
    }
}
