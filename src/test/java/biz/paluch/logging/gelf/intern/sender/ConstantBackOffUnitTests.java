package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ConstantBackOff}.
 *
 * @author Mark Paluch
 */
class ConstantBackOffUnitTests {

    @Test
    void shouldReturnConstantBackoff() {

        ConstantBackOff backOff = new ConstantBackOff(10, TimeUnit.SECONDS);

        assertThat(backOff.start().nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
    }

}
