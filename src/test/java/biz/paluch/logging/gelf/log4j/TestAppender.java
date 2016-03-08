package biz.paluch.logging.gelf.log4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class TestAppender extends WriterAppender {

	private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private static final OutputStreamWriter writer = new OutputStreamWriter(baos);

	public TestAppender() {
		setWriter(writer);
	}

	public TestAppender(Layout layout) {
		setWriter(writer);
		setLayout(layout);
	}

	public static String[] getLoggedLines() {
		String loggedLines = TestAppender.baos.toString();
		return loggedLines.split(Layout.LINE_SEP);
	}

	public static void clearLoggedLines() {
		baos.reset();
	}
}
