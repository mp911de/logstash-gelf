package biz.paluch.logging.gelf;

import java.util.Date;

import org.apache.log4j.MDC;

import biz.paluch.logging.gelf.intern.GelfMessage;

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

	private GelfUtil() {

	}

	public static void addMdcProfiling(LogEvent logEvent, GelfMessage gelfMessage) {
		Object requestStartMs = logEvent.getMdc(MDC_REQUEST_START_MS);
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

		long now = System.currentTimeMillis();
		long duration = now - timestamp;

		String durationText;

		if (duration > 10000) {
			duration = duration / 1000;
			durationText = duration + "sec";
		} else {
			durationText = duration + "ms";
		}
		gelfMessage.addField(MDC_REQUEST_DURATION, durationText);
		gelfMessage.addField(MDC_REQUEST_END, new Date(now).toString());
	}

	public static String getSimpleClassName(String className) {
		int index = className.lastIndexOf('.');
		if (index != -1) {
			return className.substring(index + 1);
		}
		return className;
	}
}
