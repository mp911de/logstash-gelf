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

    private final PoolHolder poolHolder;

    public PoolingGelfMessage(PoolHolder poolHolder) {
        this.poolHolder = poolHolder;
    }

    public PoolingGelfMessage(String shortMessage, String fullMessage, long timestamp, String level, PoolHolder poolHolder) {
        super(shortMessage, fullMessage, timestamp, level);
        this.poolHolder = poolHolder;
    }

    @Override
    public void toJson(ByteBuffer byteBuffer, String additionalFieldPrefix) {
        toJson(OutputAccessor.from(poolHolder.getOutputAccessorPoolHolder(), byteBuffer), additionalFieldPrefix);
    }

    @Override
    public ByteBuffer[] toUDPBuffers(ByteBuffer buffer, ByteBuffer tempBuffer) {

        try {
            toJson(buffer, "_");

            OutputAccessor.asStream(poolHolder.getOutputAccessorPoolHolder(), tempBuffer);

            ReusableGzipOutputStream gz = poolHolder.getReusableGzipOutputStream();
            gz.reset();
            gz.writeHeader();

            buffer.mark();
            gzip(buffer, gz);
            gz.finish();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // calculate the length of the datagrams array

        if (tempBuffer.position() > getMaximumMessageSize()) {

            int diagramsLength = tempBuffer.position() / getMaximumMessageSize();
            // In case of a remainder, due to the integer division, add a extra datagram
            if (tempBuffer.position() % getMaximumMessageSize() != 0) {
                diagramsLength++;
            }

            buffer.clear();
            return sliceDatagrams((ByteBuffer) tempBuffer.flip(), diagramsLength, buffer);
        } else {
            ByteBuffer[] byteBuffers = poolHolder.getSingleBuffer();
            byteBuffers[0] = (ByteBuffer) tempBuffer.flip();
            return byteBuffers;
        }
    }

    private void gzip(ByteBuffer source, ReusableGzipOutputStream gz) throws IOException {
        int size = source.position();

        if (source.isDirect()) {

            int read = 0;
            source.position(0);

            byte[] bytes = poolHolder.getByteArray();
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
