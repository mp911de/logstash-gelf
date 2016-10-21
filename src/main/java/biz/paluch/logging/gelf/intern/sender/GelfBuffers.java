package biz.paluch.logging.gelf.intern.sender;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfBuffers {

    /**
     * Create UDP buffers and apply auto-buffer-enlarging, if necessary.
     *
     * @param message
     * @param writeBuffers
     * @param tempBuffers
     * @return
     */
    static ByteBuffer[] toUDPBuffers(GelfMessage message, ThreadLocal<ByteBuffer> writeBuffers,
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
    static ByteBuffer toTCPBuffer(GelfMessage message, ThreadLocal<ByteBuffer> writeBuffers) {

        while (true) {

            try {
                return message.toTCPBuffer(getBuffer(writeBuffers));
            } catch (BufferOverflowException e) {
                enlargeBuffer(writeBuffers);
            }
        }
    }

    static private void enlargeBuffer(ThreadLocal<ByteBuffer> buffers) {

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
