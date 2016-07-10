package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mark Paluch
 */
abstract class OutputAccessor {

    public abstract void write(int b);

    public abstract void write(byte[] b);

    public abstract void write(byte[] b, int off, int len);

    /**
     * Create an {@link OutputAccessor} for the given {@link OutputStream}.
     * 
     * @param outputStream
     * @return
     */
    public static OutputAccessor from(OutputStream outputStream) {
        return new OutputStreamAccessor(outputStream);
    }

    static class OutputStreamAccessor extends OutputAccessor {
        private final OutputStream delegate;

        public OutputStreamAccessor(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            try {
                delegate.write(b, off, len);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void write(byte[] b) {
            try {
                delegate.write(b);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void write(int b) {
            try {
                delegate.write(b);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
