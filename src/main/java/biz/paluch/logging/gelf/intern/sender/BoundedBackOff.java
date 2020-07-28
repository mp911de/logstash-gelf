package biz.paluch.logging.gelf.intern.sender;

import java.util.concurrent.TimeUnit;

/**
 * Bounded {@link BackOff} implementation that return a {@link BackOffExecution#STOP} signal if the total awaited time is
 * greater than the specified limit. The max time can exceeded in favor to backoff created by the delegate. Any subsequent
 * backoff requests return {@link BackOffExecution#STOP}.
 *
 * @author Mark Paluch
 */
class BoundedBackOff implements BackOff {

    private final BackOff delegate;

    private final long capMs;

    public BoundedBackOff(BackOff delegate, long timeout, TimeUnit timeUnit) {
        this.delegate = delegate;
        this.capMs = timeUnit.toMillis(timeout);
    }

    @Override
    public BackOffExecution start() {
        return new BoundedBackOffExecution(delegate.start(), capMs);
    }

    static class BoundedBackOffExecution implements BackOffExecution {

        private final BackOffExecution delegate;

        private final long capMs;

        private long pastBackOff;

        public BoundedBackOffExecution(BackOffExecution delegate, long capMs) {
            this.delegate = delegate;
            this.capMs = capMs;
        }

        @Override
        public long nextBackOff() {

            if (pastBackOff >= capMs) {
                return STOP;
            }

            long backOff = delegate.nextBackOff();

            pastBackOff += backOff;

            return backOff;
        }

    }

}
