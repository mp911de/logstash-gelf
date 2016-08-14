package biz.paluch.logging.gelf.intern;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a Gelf message using resource pooling. Special caution is required with methods accepting {@link ByteBuffer}:
 * Buffer construction is generally thread-safe but the resulting buffers must not cross thread boundaries because buffers are
 * pooled on a per-thread level.
 *
 * @author Mark Paluch
 */
class PoolingGelfMessage extends GelfMessage {

    private static final ThreadLocal<byte[]> BYTE_ARRAY_POOL = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[8192 * 2];
        }
    };

    private static final ThreadLocal<ReusableGzipOutputStream> STREAM_POOL = new ThreadLocal<ReusableGzipOutputStream>() {
        @Override
        protected ReusableGzipOutputStream initialValue() {
            try {
                return new ReusableGzipOutputStream(OutputAccessor.pooledStream());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    };

    private static final ThreadLocal<ByteBuffer[]> SINGLE_BUFFER = new ThreadLocal<ByteBuffer[]>() {
        @Override
        protected ByteBuffer[] initialValue() {
            return new ByteBuffer[1];
        }
    };

    public PoolingGelfMessage() {
    }

    public PoolingGelfMessage(String shortMessage, String fullMessage, long timestamp, String level) {
        super(shortMessage, fullMessage, timestamp, level);
    }

    public ByteBuffer[] toUDPBuffers(ByteBuffer buffer, ByteBuffer tempBuffer) {

        try {
            toJson(buffer, "_");

            OutputAccessor.asStream(tempBuffer);

            ReusableGzipOutputStream gz = STREAM_POOL.get();
            gz.reset();
            gz.writeHeader();

            buffer.mark();
            gzip(buffer, gz);
            gz.finish();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // calculate the length of the datagrams array

        if (tempBuffer.position() > getMaximumMessageSize()) {

            int diagrams_length = tempBuffer.position() / getMaximumMessageSize();
            // In case of a remainder, due to the integer division, add a extra datagram
            if (tempBuffer.position() % getMaximumMessageSize() != 0) {
                diagrams_length++;
            }

            buffer.clear();
            return sliceDatagrams((ByteBuffer) tempBuffer.flip(), diagrams_length, buffer);
        } else {
            ByteBuffer[] byteBuffers = SINGLE_BUFFER.get();
            byteBuffers[0] = (ByteBuffer) tempBuffer.flip();
            return byteBuffers;
        }
    }

    private void gzip(ByteBuffer source, ReusableGzipOutputStream gz) throws IOException {
        int size = source.position();

        if (source.isDirect()) {

            int read = 0;
            source.position(0);

            byte[] bytes = BYTE_ARRAY_POOL.get();
            while (size > read) {

                if ((size - read) > bytes.length) {
                    read += bytes.length;
                    source.get(bytes);
                    gz.write(bytes);
                } else {
                    int remain = size - read;
                    read += remain;
                    source.get(bytes, 0, remain);
                    gz.write(bytes, 0, remain);
                }
            }
        } else {
            gz.write(source.array(), source.arrayOffset(), source.position());
        }
    }
}
