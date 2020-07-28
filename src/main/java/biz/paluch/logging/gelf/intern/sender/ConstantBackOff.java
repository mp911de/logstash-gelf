package biz.paluch.logging.gelf.intern.sender;

import java.util.concurrent.TimeUnit;

/**
 * Constant {@link BackOff} implementation.
 *
 * @author Mark Paluch
 */
class ConstantBackOff implements BackOff, BackOffExecution {

    private final long backoffTimeMs;

    public ConstantBackOff(long backoffTime, TimeUnit timeUnit) {
        this.backoffTimeMs = timeUnit.toMillis(backoffTime);
    }

    @Override
    public long nextBackOff() {
        return backoffTimeMs;
    }

    @Override
    public BackOffExecution start() {
        return this;
    }

}
