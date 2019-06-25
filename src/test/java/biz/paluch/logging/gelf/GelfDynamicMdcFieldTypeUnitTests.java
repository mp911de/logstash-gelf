package biz.paluch.logging.gelf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import biz.paluch.logging.gelf.log4j2.GelfDynamicMdcFieldType;

/**
 * @author Thomas Herzog
 */
@RunWith(JUnit4.class)
public class GelfDynamicMdcFieldTypeUnitTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testWithNullRegex() {
        // -- Given --
        String regex = null;
        String type = "String";

        // -- Then --
        expectedException.expect(IllegalArgumentException.class);

        // -- When --
        GelfDynamicMdcFieldType.createField(regex, type);
    }

    @Test
    public void testWithNullType() {
        // -- Given --
        String regex = ".*";
        String type = null;

        // -- Then --
        expectedException.expect(IllegalArgumentException.class);

        // -- When --
        GelfDynamicMdcFieldType.createField(regex, type);
    }

    @Test
    public void testWithInvalidRegex() {
        // -- Given --
        String regex = "*";
        String type = "String";

        // -- Then --
        expectedException.expect(IllegalArgumentException.class);

        GelfDynamicMdcFieldType.createField(regex, type);
    }

    @Test
    public void testWithValidRegexAndType() {
        // -- Given --
        String regex = ".*";
        String type = "String";

        // -- When --
        GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(regex, type);

        // -- Then --
        assertThat(fieldType).isNotNull();
        assertThat(fieldType.getPattern().pattern()).isEqualTo(regex);
        assertThat(fieldType.getType()).isEqualTo(type);
    }
}
