package biz.paluch.logging;

import org.junit.Before;
import org.junit.Test;

public class StackTraceFilterTest {
    @Before
    public void before() throws Exception {
        StackTraceFilter.loadSetttings(StackTraceFilter.FILTER_SETTINGS);

    }

    @Test
    public void testNull() throws Exception {
        StackTraceFilter.loadSetttings("nonexistent");
    }

    @Test
    public void testOwnProperties() throws Exception {
        StackTraceFilter.loadSetttings("StackTraceFilterTest.properties");

    }
}
