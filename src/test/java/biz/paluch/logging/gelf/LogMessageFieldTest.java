package biz.paluch.logging.gelf;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.List;

/**
 * @author Mark Paluch
 */
public class LogMessageFieldTest {

    @Test
    public void testGetMapping() throws Exception {
        List<LogMessageField> result = LogMessageField.getDefaultMapping(false, LogMessageField.NamedLogField.LoggerName,
                LogMessageField.NamedLogField.NDC);

        assertEquals(2, result.size());
    }

    @Test
    public void testGetMappingAllFields() throws Exception {

        List<LogMessageField> result = LogMessageField.getDefaultMapping(false, LogMessageField.NamedLogField.values());

        assertEquals(LogMessageField.NamedLogField.values().length, result.size());
    }

    @Test
    public void testGetMappingAllFieldsWithDefaultFile() throws Exception {

        List<LogMessageField> result = LogMessageField.getDefaultMapping(true, LogMessageField.NamedLogField.values());

        assertEquals(LogMessageField.NamedLogField.values().length, result.size());
    }
}
