package biz.paluch.logging.gelf.intern.sender;

public class ConstantBackOff implements BackOff {
    private final int backoffTimeMs;

    public ConstantBackOff(int backoffTimeMs) {
        this.backoffTimeMs = backoffTimeMs;
    }

    @Override
    public BackOffExecution start() {
        return new BackOffExecution() {
            @Override
            public int nextBackOff() {
                return backoffTimeMs;
            }
        };
    }
}
