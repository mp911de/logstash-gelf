package biz.paluch.logging.gelf.intern;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import biz.paluch.logging.gelf.intern.ValueDiscovery.Result;

/**
 * Unit tests for {@link ValueDiscovery}.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @author Wolfgang Jung
 */
class ValueDiscoveryUnitTests {

    @Test
    void emptyString() {
        assertThat(ValueDiscovery.discover("")).isEqualTo(Result.STRING);
    }

    @Test
    void singleDigitNumbers() {

        for (int i = 0; i < 10; i++) {
            assertThat(ValueDiscovery.discover("" + i)).isEqualTo(Result.LONG);
        }
    }

    @Test
    void twoDigitNumbers() {

        for (int i = 10; i < 99; i++) {
            assertThat(ValueDiscovery.discover("" + i)).isEqualTo(Result.LONG);
        }
    }

    @Test
    void positiveAndNegativeDigitNumbers() {

        for (int i = 0; i < 99; i++) {
            assertThat(ValueDiscovery.discover("-" + i)).isEqualTo(Result.LONG);
            assertThat(ValueDiscovery.discover("+" + i)).isEqualTo(Result.LONG);
        }
    }

    @Test
    void singleDigitDouble() {

        for (int i = 0; i < 10; i++) {
            assertThat(ValueDiscovery.discover("" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void twoDigitDouble() {

        for (int i = 10; i < 99; i++) {
            assertThat(ValueDiscovery.discover("" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void positiveAndNegativeDigitDouble() {

        for (int i = 0; i < 99; i++) {
            assertThat(ValueDiscovery.discover("-" + i + ".0")).isEqualTo(Result.DOUBLE);
            assertThat(ValueDiscovery.discover("+" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void doubleWithChars() {

        assertThat(ValueDiscovery.discover("2e5")).isEqualTo(Result.DOUBLE);
        assertThat(ValueDiscovery.discover("2e5.1")).isEqualTo(Result.STRING);
        assertThat(ValueDiscovery.discover("1.2.3")).isEqualTo(Result.STRING);
        assertThat(ValueDiscovery.discover("A")).isEqualTo(Result.STRING);
        assertThat(ValueDiscovery.discover("9.156013e-002")).isEqualTo(Result.DOUBLE);
        assertThat(ValueDiscovery.discover("0x0.0000000000001P-1022")).isEqualTo(Result.DOUBLE);
        assertThat(ValueDiscovery.discover("0x1.fffffffffffffP+1023")).isEqualTo(Result.DOUBLE);
    }

    @Test
    void shouldDiscoverStringExceedingLength() {
        assertThat(ValueDiscovery.discover("1111111111111111111")).isEqualTo(Result.LONG);
        assertThat(ValueDiscovery.discover("11111111111111111111")).isEqualTo(Result.STRING);
        assertThat(ValueDiscovery.discover("111111111111111111111111111111111")).isEqualTo(Result.STRING);
    }

    @Test
    void testString() {
        assertThat(ValueDiscovery.discover("deadbeef")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testHexString() {
        assertThat(ValueDiscovery.discover("0xdeadbeef")).isEqualTo(Result.LONG);
    }

    @Test
    void testStringWithLeadingZeroAndHex() {
        assertThat(ValueDiscovery.discover("0deadbeef")).isEqualTo(ValueDiscovery.Result.STRING);
        assertThat(ValueDiscovery.discover("0123")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroAndP() {
        assertThat(ValueDiscovery.discover("0p")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroAndX() {
        assertThat(ValueDiscovery.discover("0x")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroXP() {
        assertThat(ValueDiscovery.discover("0xp")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testInfinity() {
        assertThat(ValueDiscovery.discover("Infinity")).isEqualTo(ValueDiscovery.Result.DOUBLE);
        assertThat(ValueDiscovery.discover("+Infinity")).isEqualTo(ValueDiscovery.Result.DOUBLE);
        assertThat(ValueDiscovery.discover("-Infinity")).isEqualTo(ValueDiscovery.Result.DOUBLE);
        assertThat(ValueDiscovery.discover("Infinity1")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testNaN() {
        assertThat(ValueDiscovery.discover("NaN")).isEqualTo(ValueDiscovery.Result.DOUBLE);
        assertThat(ValueDiscovery.discover("+NaN")).isEqualTo(ValueDiscovery.Result.STRING);
        assertThat(ValueDiscovery.discover("-NaN")).isEqualTo(ValueDiscovery.Result.STRING);
        assertThat(ValueDiscovery.discover("NaN1")).isEqualTo(ValueDiscovery.Result.STRING);
    }

}
