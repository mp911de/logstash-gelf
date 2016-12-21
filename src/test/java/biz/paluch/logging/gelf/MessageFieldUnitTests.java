package biz.paluch.logging.gelf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * @author Mark Paluch
 */
public class MessageFieldUnitTests {

    private static final String NAME = "name";

    @Test
    public void testMdcMessageField() throws Exception {
        MdcMessageField field = new MdcMessageField(NAME, "mdcName");
        assertEquals(MdcMessageField.class.getSimpleName() + " [name='name', mdcName='mdcName']", field.toString());
    }

    @Test
    public void testLogMessageField() throws Exception {
        LogMessageField field = new LogMessageField(NAME, LogMessageField.NamedLogField.byName("SourceMethodName"));
        assertEquals(LogMessageField.class.getSimpleName() + " [name='name', namedLogField=SourceMethodName]",
                field.toString());
    }

    @Test
    public void testStaticMessageField() throws Exception {
        StaticMessageField field = new StaticMessageField(NAME, "value");
        assertEquals(StaticMessageField.class.getSimpleName() + " [name='name', value='value']", field.toString());
    }

    @Test
    public void testDynamicMdcMessageField() throws Exception {
        DynamicMdcMessageField field = new DynamicMdcMessageField(".*");
        assertEquals(DynamicMdcMessageField.class.getSimpleName() + " [regex='.*']", field.toString());
    }

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