package biz.paluch.logging.gelf.log4j;

import static biz.paluch.logging.gelf.LogMessageField.NamedLogField.*;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import biz.paluch.logging.gelf.LogMessageField;
import biz.paluch.logging.gelf.MdcGelfMessageAssembler;
import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Log layout format for GELF (Graylog Extended Logging Format). Following parameters are supported:
 * <ul>
 * <li>extractStackTrace (Optional): Post Stack-Trace to StackTrace field, default false</li>
 * <li>filterStackTrace (Optional): Perform Stack-Trace filtering (true/false), default false</li>
 * <li>includeFullMdc (Optional): Include all fields from the MDC, default false</li>
 * <li>mdcProfiling (Optional): Perform Profiling (Call-Duration) based on MDC Data. See <a href="#mdcProfiling">MDC Profiling</a>, default
 * false</li>
 * </ul>
 */
public class GelfLayout extends Layout {

	private final MdcGelfMessageAssembler gelfMessageAssembler;

	public GelfLayout() {
		super();
		gelfMessageAssembler = new MdcGelfMessageAssembler();
		gelfMessageAssembler.addFields(LogMessageField.getDefaultMapping(Time, Severity, ThreadName, SourceClassName, SourceMethodName,
				SourceLineNumber, SourceSimpleClassName, LoggerName, NDC));
	}

	@Override
	public String format(LoggingEvent loggingEvent) {
		GelfMessage gelfMessage = gelfMessageAssembler.createGelfMessage(new Log4jLogEvent(loggingEvent));
		return gelfMessage.toJson("") + Layout.LINE_SEP;
	}

	@Override
	public boolean ignoresThrowable() {
		return false;
	}

	@Override
	public void activateOptions() {

	}

	public void setMdcProfiling(boolean mdcProfiling) {
		gelfMessageAssembler.setMdcProfiling(mdcProfiling);
	}

	public boolean isMdcProfiling() {
		return gelfMessageAssembler.isMdcProfiling();
	}

	public void setIncludeFullMdc(boolean includeFullMdc) {
		gelfMessageAssembler.setIncludeFullMdc(includeFullMdc);
	}

	public boolean isIncludeFullMdc() {
		return gelfMessageAssembler.isIncludeFullMdc();
	}

	public void setExtractStackTrace(boolean extractStacktrace) {
		gelfMessageAssembler.setExtractStackTrace(extractStacktrace);
	}

	public boolean isExtractStackTrace() {
		return gelfMessageAssembler.isExtractStackTrace();
	}

	public void setFilterStackTrace(boolean filterStackTrace) {
		gelfMessageAssembler.setFilterStackTrace(filterStackTrace);
	}

	public boolean isFilterStackTrace() {
		return gelfMessageAssembler.isFilterStackTrace();
	}
}
