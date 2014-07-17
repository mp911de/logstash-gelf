package biz.paluch.logging.gelf.intern;

import static org.junit.Assert.assertEquals;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.Map;

public class GelfMessageTest {

    public static final String FACILTY = "facilty";
    public static final String MESSAGE = "message";
    public static final String HOST = "host";
    public static final int JAVA_TIMESTAMP = 1234;
    public static final String LEVEL = "9";
    public static final int MAXIMUM_MESSAGE_SIZE = 8192;
    public static final String VERSION = "1";

    @Test
    public void testMessage() throws Exception {
        GelfMessage sut = new GelfMessage("", "", 0, "");
        sut.setFacility(FACILTY);
        sut.setFullMessage(MESSAGE);
        sut.setHost(HOST);
        sut.setJavaTimestamp(JAVA_TIMESTAMP);
        sut.setLevel(LEVEL);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setVersion(VERSION);

        Map<String, String> map = (Map<String, String>) new JSONParser().parse(sut.toJson());

        assertEquals(FACILTY, map.get(GelfMessage.FIELD_FACILITY));
        assertEquals(MESSAGE, map.get(GelfMessage.FIELD_FULL_MESSAGE));
        assertEquals("<empty>", map.get(GelfMessage.FIELD_SHORT_MESSAGE));
        assertEquals(LEVEL, map.get(GelfMessage.FIELD_LEVEL));
        assertEquals(VERSION, sut.getVersion());

    }
}
