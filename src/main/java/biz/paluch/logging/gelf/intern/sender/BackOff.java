package biz.paluch.logging.gelf.intern.sender;

public interface BackOff {
    BackOffExecution start();
}
