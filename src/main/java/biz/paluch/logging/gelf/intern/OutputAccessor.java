package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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

    /**
     * Create an {@link OutputAccessor} for the given {@link ByteBuffer}. Instances are pooled within the thread scope.
     *
     * @param byteBuffer
     * @return
     */
    public static OutputAccessor from(ByteBuffer byteBuffer) {

        ByteBufferOutputAccessor accessor = new ByteBufferOutputAccessor();
        accessor.byteBuffer = byteBuffer;

        return accessor;
    }

    /**
     * Create an {@link OutputAccessor} for the given {@link ByteBuffer}. Instances are pooled within the thread scope.
     *
     * @param poolHolder
     * @param byteBuffer
     * @return
     */
    public static OutputAccessor from(OutputAccessorPoolHolder poolHolder, ByteBuffer byteBuffer) {

        ByteBufferOutputAccessor accessor = poolHolder.getByteBufferOutputAccessor();
        accessor.byteBuffer = byteBuffer;

        return accessor;
    }

    /**
     * Retrieve a pooled {@link OutputStream}.
     *
     * @param poolHolder
     * @return
     */
    public static OutputStream pooledStream(OutputAccessorPoolHolder poolHolder) {
        return poolHolder.getByteBufferOutputStream();
    }

    /**
     * Retrieved a pooled an {@link OutputStream} for the given {@link ByteBuffer}. Instances are pooled within the thread
     * scope.
     *
     * @param poolHolder
     * @param byteBuffer
     * @return
     */
    public static OutputStream asStream(OutputAccessorPoolHolder poolHolder, ByteBuffer byteBuffer) {

        ByteBufferOutputStream accessor = poolHolder.getByteBufferOutputStream();
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

    /**
     * Holder for {@link ThreadLocal} pools.
     */
    static class OutputAccessorPoolHolder {

        private final ThreadLocal<ByteBufferOutputAccessor> accessorPool = new ThreadLocal<ByteBufferOutputAccessor>() {
            @Override
            protected ByteBufferOutputAccessor initialValue() {
                return new ByteBufferOutputAccessor();
            }
        };

        private final ThreadLocal<ByteBufferOutputStream> streamPool = new ThreadLocal<ByteBufferOutputStream>() {
            @Override
            protected ByteBufferOutputStream initialValue() {
                return new ByteBufferOutputStream(null);
            }
        };

        /**
         * @return a pooled {@link ByteBufferOutputAccessor} instance.
         */
        public ByteBufferOutputAccessor getByteBufferOutputAccessor() {
            return accessorPool.get();
        }

        /**
         * @return a pooled {@link ByteBufferOutputStream} instance.
         */
        public ByteBufferOutputStream getByteBufferOutputStream() {
            return streamPool.get();
        }
    }
}
