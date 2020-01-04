package biz.paluch.logging.gelf.intern.sender;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * Utility to create TCP/UDP buffers from a {@link GelfMessage} by working with {@link ByteBuffer}. Buffers are provided by
 * {@link ThreadLocal} and enlarged if the buffer size is exceeded. Enlarged buffers are returned to their originating
 * {@link ThreadLocal}.
 *
 * @author Mark Paluch
 */
class GelfBuffers {

    private GelfBuffers() {
        // no instance allowed
    }

    /**
     * Create UDP buffers and apply auto-buffer-enlarging, if necessary.
     *
     * @param message
     * @param writeBuffers
     * @param tempBuffers
     * @return
     */
    protected static ByteBuffer[] toUDPBuffers(GelfMessage message, ThreadLocal<ByteBuffer> writeBuffers,
            ThreadLocal<ByteBuffer> tempBuffers) {

        while (true) {

            try {
                return message.toUDPBuffers(getBuffer(writeBuffers), getBuffer(tempBuffers));
            } catch (BufferOverflowException e) {
                enlargeBuffer(writeBuffers);
                enlargeBuffer(tempBuffers);
            }
        }
    }

    /**
     * Create TCP buffer and apply auto-buffer-enlarging, if necessary.
     *
     * @param message
     * @param writeBuffers
     * @return
     */
    protected static ByteBuffer toTCPBuffer(GelfMessage message, ThreadLocal<ByteBuffer> writeBuffers) {

        while (true) {

            try {
                return message.toTCPBuffer(getBuffer(writeBuffers));
            } catch (BufferOverflowException e) {
                enlargeBuffer(writeBuffers);
            }
        }
    }

    private static void enlargeBuffer(ThreadLocal<ByteBuffer> buffers) {

        ByteBuffer newBuffer = ByteBuffer.allocateDirect(calculateNewBufferSize(buffers.get().capacity()));
        buffers.set(newBuffer);
    }

    private static ByteBuffer getBuffer(ThreadLocal<ByteBuffer> buffers) {
        return (ByteBuffer) buffers.get().clear();
    }

    private static int calculateNewBufferSize(int capacity) {
        return (int) (capacity * 1.5);
    }
}
