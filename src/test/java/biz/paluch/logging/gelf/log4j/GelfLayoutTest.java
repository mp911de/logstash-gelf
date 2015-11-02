package biz.paluch.logging.gelf.log4j;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GelfLayoutTest {

	private static Logger logger;

	@BeforeClass
	public static void beforeClass() {
		DOMConfigurator.configure(GelfLayoutTest.class.getResource("/log4j-gelf-layout.xml"));
		logger = Logger.getLogger(GelfLayoutTest.class);
	}

	@Before
	public void before() {
		TestAppender.clearLoggedLines();
	}

	@Test
	public void test() {
		logger.info("test1");
		logger.info("test2");
		logger.info("test3");
		String[] loggedLines = TestAppender.getLoggedLines();
		assertThat(loggedLines.length).isEqualTo(3);
		assertThat(parseToJSONObject(loggedLines[0]).get("full_message")).isEqualTo("test1");
		assertThat(parseToJSONObject(loggedLines[1]).get("full_message")).isEqualTo("test2");
		assertThat(parseToJSONObject(loggedLines[2]).get("full_message")).isEqualTo("test3");
	}

	private JSONObject parseToJSONObject(String value) {
		return (JSONObject) JSONValue.parse(value);
	}
}