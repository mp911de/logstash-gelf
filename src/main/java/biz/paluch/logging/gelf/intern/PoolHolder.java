package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Holder for {@link ThreadLocal} pools.
 *
 * @author Mark Paluch
 */
abstract class PoolHolder {

    public static PoolHolder noop() {
        return NoOpPoolHolder.noop();
    }

    public static PoolHolder threadLocal() {
        return new ThreadLocalPoolHolder();
    }

    /**
     * @return the {@link OutputAccessor.OutputAccessorPoolHolder}.
     */
    public abstract OutputAccessor.OutputAccessorPoolHolder getOutputAccessorPoolHolder();

    /**
     * @return a pooled {@link ReusableGzipOutputStream} instance.
     */
    public abstract ReusableGzipOutputStream getReusableGzipOutputStream();

    /**
     * @return a pooled {@link ByteBuffer}-array instance.
     */
    public abstract ByteBuffer[] getSingleBuffer();

    /**
     * @return a pooled byte-array instance.
     */
    public abstract byte[] getByteArray();

    private static class NoOpPoolHolder extends PoolHolder {

        private static final NoOpPoolHolder instance = new NoOpPoolHolder();

        public static PoolHolder noop() {
            return instance;
        }

        @Override
        public OutputAccessor.OutputAccessorPoolHolder getOutputAccessorPoolHolder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReusableGzipOutputStream getReusableGzipOutputStream() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteBuffer[] getSingleBuffer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getByteArray() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@link PoolHolder} backed by {@link ThreadLocal}.
     */
    private static class ThreadLocalPoolHolder extends PoolHolder {

        private final OutputAccessor.OutputAccessorPoolHolder outputAccessorPoolHolder;

        private final ThreadLocal<ReusableGzipOutputStream> streamPool = new ThreadLocal<ReusableGzipOutputStream>() {
            @Override
            protected ReusableGzipOutputStream initialValue() {
                try {
                    return new ReusableGzipOutputStream(OutputAccessor.pooledStream(outputAccessorPoolHolder));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        private final ThreadLocal<ByteBuffer[]> singleBufferPool = new ThreadLocal<ByteBuffer[]>() {
            @Override
            protected ByteBuffer[] initialValue() {
                return new ByteBuffer[1];
            }
        };

        private final ThreadLocal<byte[]> byteArrayPool = new ThreadLocal<byte[]>() {
            @Override
            protected byte[] initialValue() {
                return new byte[8192 * 2];
            }
        };

        /**
         * Create a new {@link PoolHolder} instance.
         */
        public ThreadLocalPoolHolder() {
            this(new OutputAccessor.OutputAccessorPoolHolder());
        }

        private ThreadLocalPoolHolder(OutputAccessor.OutputAccessorPoolHolder outputAccessorPoolHolder) {
            this.outputAccessorPoolHolder = outputAccessorPoolHolder;
        }

        /**
         * @return the {@link OutputAccessor.OutputAccessorPoolHolder}.
         */
        public OutputAccessor.OutputAccessorPoolHolder getOutputAccessorPoolHolder() {
            return outputAccessorPoolHolder;
        }

        /**
         * @return a pooled {@link ReusableGzipOutputStream} instance.
         */
        public ReusableGzipOutputStream getReusableGzipOutputStream() {
            return streamPool.get();
        }

        /**
         * @return a pooled {@link ByteBuffer}-array instance.
         */
        public ByteBuffer[] getSingleBuffer() {
            return singleBufferPool.get();
        }

        /**
         * @return a pooled byte-array instance.
         */
        public byte[] getByteArray() {
            return byteArrayPool.get();
        }
    }
}
