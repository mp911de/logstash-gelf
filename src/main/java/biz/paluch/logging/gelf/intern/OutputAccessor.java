package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Mark Paluch
 */
abstract class OutputAccessor {

    private static final ThreadLocal<ByteBufferOutputAccessor> accessors = new ThreadLocal<ByteBufferOutputAccessor>() {
        @Override
        protected ByteBufferOutputAccessor initialValue() {
            return new ByteBufferOutputAccessor();
        }
    };

    private static final ThreadLocal<ByteBufferOutputStream> streams = new ThreadLocal<ByteBufferOutputStream>() {
        @Override
        protected ByteBufferOutputStream initialValue() {
            return new ByteBufferOutputStream(null);
        }
    };

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

    /**
     * Create an {@link OutputAccessor} for the given {@link ByteBuffer}. Instances are pooled within the thread scope.
     *
     * @param byteBuffer
     * @return
     */
    public static OutputAccessor from(ByteBuffer byteBuffer) {

        ByteBufferOutputAccessor accessor = accessors.get();
        accessor.byteBuffer = byteBuffer;

        return accessor;
    }

    /**
     * Retrieve a pooled {@link OutputStream}.
     *
     * @return
     */
    public static OutputStream pooledStream() {
        return streams.get();
    }

    /**
     * Retrieved a pooled an {@link OutputStream} for the given {@link ByteBuffer}. Instances are pooled within the thread scope.
     *
     * @param byteBuffer
     * @return
     */
    public static OutputStream asStream(ByteBuffer byteBuffer) {

        ByteBufferOutputStream accessor = streams.get();
        accessor.byteBuffer = byteBuffer;

        return accessor;
    }

    static class ByteBufferOutputAccessor extends OutputAccessor {
        private ByteBuffer byteBuffer;

        @Override
        public void write(byte[] b, int off, int len) {
            byteBuffer.put(b, off, len);
        }

        @Override
        public void write(byte[] b) {
            byteBuffer.put(b);
        }

        @Override
        public void write(int b) {
            byteBuffer.put((byte) b);
        }
    }

    static class ByteBufferOutputStream extends OutputStream {
        private ByteBuffer byteBuffer;

        public ByteBufferOutputStream(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            byteBuffer.put(b, off, len);
        }

        @Override
        public void write(byte[] b) {
            byteBuffer.put(b);
        }

        @Override
        public void write(int b) {
            byteBuffer.put((byte) b);
        }
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
