package biz.paluch.logging.gelf.intern.sender;

/**
 * Represent a particular back-off execution.
 *
 * <p>
 * Implementations do not need to be thread safe.
 *
 * @author netudima
 * @see BackOff
 */
interface BackOffExecution {

    /**
     * Return value of {@link #nextBackOff()} that indicates that the operation should not be retried.
     */
    long STOP = -1;

    /**
     * @return time in ms to wait before a next attempt
     */
    long nextBackOff();
}
