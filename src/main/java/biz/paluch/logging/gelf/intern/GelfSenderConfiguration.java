package biz.paluch.logging.gelf.intern;

public interface GelfSenderConfiguration {
	public String getHost();
	public int getPort();
	public ErrorReporter getErrorReport();
}