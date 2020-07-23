package biz.paluch.logging.gelf.intern.sender;

public interface BackOffExecution {
    /**
     * @return time in ms to wait before a next attempt
     */
    int nextBackOff();
}
