package biz.paluch.logging.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BoundedBackOff}.
 *
 * @author Mark Paluch
 */
class BoundedBackOffUnitTests {

    BoundedBackOff backOff = new BoundedBackOff(new ConstantBackOff(10, TimeUnit.SECONDS), 15, TimeUnit.SECONDS);

    @Test
    void shouldPassThruBackoff() {
        assertThat(backOff.start().nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
    }

    @Test
    void shouldCapBackoff() {

        BackOffExecution backOffExecution = backOff.start();

        assertThat(backOffExecution.nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
        assertThat(backOffExecution.nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
        assertThat(backOffExecution.nextBackOff()).isEqualTo(BackOffExecution.STOP);
    }

}
