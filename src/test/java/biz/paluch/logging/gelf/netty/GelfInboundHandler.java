package biz.paluch.logging.gelf.netty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Mark Paluch
 * @since 10.11.13 10:35
 */
public class GelfInboundHandler extends ChannelInboundHandlerAdapter {
    private static final byte[] GELF_CHUNKED_ID = new byte[] { 0x1e, 0x0f };
    private static final byte[] GZIP_ID = new byte[] { 0x1f, 0xffffff8b };

    private final Map<ChunkId, List<Chunk>> chunks = new HashMap<ChunkId, List<Chunk>>();
    private final List<Object> values = new ArrayList<Object>();
    private ByteArrayOutputStream intermediate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {

            boolean requireNullEnd = false;
            ByteBuf buffer = null;

            if (msg instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket) msg;
                buffer = packet.content();
            } else if (msg instanceof ByteBuf) {
                buffer = (ByteBuf) msg;
                requireNullEnd = true;
            }

            ByteArrayOutputStream temporaryBuffer = new ByteArrayOutputStream();
            while (buffer.readableBytes() != 0) {
                buffer.readBytes(temporaryBuffer, buffer.readableBytes());
            }

            byte bytes[] = temporaryBuffer.toByteArray();
            if (bytes.length > 2) {
                // if chunked 0x1e, 0x0f ,8 bytes id, number, size
                // if gzip 0x1f 0x8b

                if (startsWith(bytes, GELF_CHUNKED_ID)) {

                    byte id[] = new byte[8];
                    System.arraycopy(bytes, 2, id, 0, 8);
                    byte number = bytes[10];
                    byte count = bytes[11];
                    ChunkId chunkId = new ChunkId(id, count);

                    byte message[] = new byte[bytes.length - 12];
                    System.arraycopy(bytes, 12, message, 0, message.length);

                    synchronized (chunks) {

                        List<Chunk> messageChunks;
                        if (chunks.containsKey(chunkId)) {
                            messageChunks = chunks.get(chunkId);
                        } else {
                            messageChunks = new ArrayList<Chunk>();
                            chunks.put(chunkId, messageChunks);
                        }
                        messageChunks.add(new Chunk(message, number));
                        if (messageChunks.size() == count) {
                            Collections.sort(messageChunks);
                            bytes = getBytes(messageChunks);
                            chunks.remove(chunkId);
                        } else {
                            return;
                        }
                    }
                }

                if (requireNullEnd) {
                    if (bytes[bytes.length - 1] != 0) {
                        intermediate = new ByteArrayOutputStream();
                        intermediate.write(bytes, 0, bytes.length);
                        return;
                    } else if (intermediate != null) {
                        intermediate.write(bytes, 0, bytes.length);
                        bytes = intermediate.toByteArray();
                        intermediate = null;
                    }
                }

                InputStream is = null;

                if (startsWith(bytes, GZIP_ID)) {
                    is = new GZIPInputStream(new ByteArrayInputStream(bytes));
                } else {
                    if (bytes[bytes.length - 1] == 0) {
                        is = new ByteArrayInputStream(bytes, 0, bytes.length - 1);
                    } else {
                        is = new ByteArrayInputStream(bytes);
                    }
                }

                Object parse = objectMapper.readValue(is, Map.class);
                synchronized (values) {
                    values.add(parse);
                }

            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private byte[] getBytes(List<Chunk> messageChunks) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (Chunk messageChunk : messageChunks) {
            buffer.write(messageChunk.bytes);
        }
        return buffer.toByteArray();
    }

    private boolean startsWith(byte[] base, byte[] compareWith) {
        if (base.length >= compareWith.length) {
            for (int i = 0; i < compareWith.length; i++) {
                if (base[i] != compareWith[i]) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public List<Object> getJsonValues() {
        synchronized (values) {
            return new ArrayList<Object>(values);
        }
    }

    public void clear() {
        synchronized (values) {
            values.clear();
            chunks.clear();
        }
    }

    private class Chunk implements Comparable<Chunk> {
        private byte[] bytes;
        private int seq;

        private Chunk(byte[] bytes, int seq) {
            this.bytes = bytes;
            this.seq = seq;
        }

        @Override
        public int compareTo(Chunk o) {
            return seq - o.seq;
        }
    }

    private class ChunkId {

        private byte[] id;
        private byte count;

        private ChunkId(byte[] id, byte count) {
            this.id = id;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ChunkId)) {
                return false;
            }

            ChunkId chunkId = (ChunkId) o;

            if (count != chunkId.count) {
                return false;
            }
            if (!Arrays.equals(id, chunkId.id)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? Arrays.hashCode(id) : 0;
            result = 31 * result + (int) count;
            return result;
        }
    }
}
