package biz.paluch.logging.gelf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 16.07.14 21:00
 */
public class SillyTest {

    public static final String NAME = "name";

    @Test
    public void testMdcMessageField() throws Exception {
        MdcMessageField field = new MdcMessageField(NAME, "mdcName");
        assertEquals(MdcMessageField.class.getSimpleName() + " [name='name', mdcName='mdcName']", field.toString());
    }

    @Test
    public void testLogMessageField() throws Exception {
        LogMessageField field = new LogMessageField(NAME, LogMessageField.NamedLogField.byName("SourceMethodName"));
        assertEquals(LogMessageField.class.getSimpleName() + " [name='name', namedLogField=SourceMethodName]", field.toString());
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
}
