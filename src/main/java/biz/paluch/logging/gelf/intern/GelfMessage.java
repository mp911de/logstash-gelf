package biz.paluch.logging.gelf.intern;

import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * (c) https://github.com/t0xa/gelfj
 */
public class GelfMessage {

    public static final String FIELD_HOST = "host";
    public static final String FIELD_SHORT_MESSAGE = "short_message";
    public static final String FIELD_FULL_MESSAGE = "full_message";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_FACILITY = "facility";
    public static final String ID_NAME = "id";

    public static final String GELF_VERSION = "1.0";
    public static final String DEFAULT_FACILITY = "logstash-gelf";
    public static final int DEFAULT_MESSAGE_SIZE = 8192;

    private static final byte[] GELF_CHUNKED_ID = new byte[] { 0x1e, 0x0f };
    private static final BigDecimal TIME_DIVISOR = new BigDecimal(1000);

    private String version = GELF_VERSION;
    private String host;
    private byte[] hostBytes = lastFourAsciiBytes("none");
    private String shortMessage;
    private String fullMessage;
    private long javaTimestamp;
    private String level;
    private String facility = DEFAULT_FACILITY;
    private Map<String, String> additonalFields = new HashMap<String, String>();
    private int maximumMessageSize = DEFAULT_MESSAGE_SIZE;

    public GelfMessage() {
    }

    public GelfMessage(String shortMessage, String fullMessage, long timestamp, String level) {

        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.javaTimestamp = timestamp;
        this.level = level;
    }

    public String toJson(String additionalFieldPrefix) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (!isEmpty(getHost())) {
            map.put(FIELD_HOST, getHost());
        }

        if (!isEmpty(shortMessage)) {
            map.put(FIELD_SHORT_MESSAGE, getShortMessage());
        }

        if (!isEmpty(getFullMessage())) {
            map.put(FIELD_FULL_MESSAGE, getFullMessage());
        }

        map.put(FIELD_TIMESTAMP, getTimestamp());

        if (!isEmpty(getLevel())) {
            map.put(FIELD_LEVEL, getLevel());
        }

        if (!isEmpty(getFacility())) {
            map.put(FIELD_FACILITY, getFacility());
        }

        for (Map.Entry<String, String> additionalField : additonalFields.entrySet()) {
            if (!ID_NAME.equals(additionalField.getKey())) {
                // try adding the value as a double
                Object value;
                try {
                    value = Double.parseDouble(additionalField.getValue());
                } catch (NumberFormatException ex) {
                    // fallback on the string value
                    value = additionalField.getValue();
                }
                map.put(additionalFieldPrefix + additionalField.getKey(), value);
            }
        }

        return JSONValue.toJSONString(map);
    }

    public String toJson() {
        return toJson("_");
    }

    public ByteBuffer[] toUDPBuffers() {
        byte[] messageBytes = gzipMessage(toJson());
        // calculate the length of the datagrams array
        int diagrams_length = messageBytes.length / maximumMessageSize;
        // In case of a remainder, due to the integer division, add a extra datagram
        if (messageBytes.length % maximumMessageSize != 0) {
            diagrams_length++;
        }
        ByteBuffer[] datagrams = new ByteBuffer[diagrams_length];
        if (messageBytes.length > maximumMessageSize) {
            sliceDatagrams(messageBytes, datagrams);
        } else {
            datagrams[0] = ByteBuffer.allocate(messageBytes.length);
            datagrams[0].put(messageBytes);
            datagrams[0].flip();
        }
        return datagrams;
    }

    public ByteBuffer toTCPBuffer() {
        byte[] messageBytes;
        // Do not use GZIP, as the headers will contain \0 bytes
        // graylog2-server uses \0 as a delimiter for TCP frames
        // see: https://github.com/Graylog2/graylog2-server/issues/127
        String json = toJson();
        json += '\0';
        messageBytes = Charsets.utf8(json);

        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }

    private void sliceDatagrams(byte[] messageBytes, ByteBuffer[] datagrams) {
        int messageLength = messageBytes.length;
        byte[] messageId = ByteBuffer.allocate(8).putInt(getCurrentMillis()) // 4 least-significant-bytes of the time in millis
                .put(hostBytes) // 4 least-significant-bytes of the host
                .array();

        // Reuse length of datagrams array since this is supposed to be the correct number of datagrams
        int num = datagrams.length;
        for (int idx = 0; idx < num; idx++) {
            byte[] header = concatByteArray(GELF_CHUNKED_ID, concatByteArray(messageId, new byte[] { (byte) idx, (byte) num }));
            int from = idx * maximumMessageSize;
            int to = from + maximumMessageSize;
            if (to >= messageLength) {
                to = messageLength;
            }
            byte[] datagram = concatByteArray(header, Arrays.copyOfRange(messageBytes, from, to));
            datagrams[idx] = ByteBuffer.allocate(datagram.length);
            datagrams[idx].put(datagram);
            datagrams[idx].flip();
        }
    }

    public int getCurrentMillis() {
        return (int) System.currentTimeMillis();
    }

    private byte[] gzipMessage(String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            GZIPOutputStream stream = new GZIPOutputStream(bos);
            byte[] bytes = Charsets.utf8(message);

            stream.write(bytes);
            stream.finish();
            Closer.close(stream);
            byte[] zipped = bos.toByteArray();
            Closer.close(bos);
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] lastFourAsciiBytes(String host) {
        final String shortHost = host.length() >= 4 ? host.substring(host.length() - 4) : host;
        return Charsets.ascii(shortHost);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        if (host != null) {
            this.hostBytes = lastFourAsciiBytes(host);
        }
    }

    public String getShortMessage() {
        return !isEmpty(shortMessage) ? shortMessage : "<empty>";
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getTimestamp() {
        return new BigDecimal(javaTimestamp).divide(TIME_DIVISOR).toPlainString();
    }

    public Long getJavaTimestamp() {
        return javaTimestamp;
    }

    public void setJavaTimestamp(long javaTimestamp) {
        this.javaTimestamp = javaTimestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * Add multiple fields (key/value pairs)
     * 
     * @param fields
     * @return the current GelfMessage.
     */
    public GelfMessage addFields(Map<String, String> fields) {

        if (fields == null) {
            throw new IllegalArgumentException("fields is null");
        }
        getAdditonalFields().putAll(fields);
        return this;
    }

    /**
     * Add a particular field.
     * 
     * @param key
     * @param value
     * @return the current GelfMessage.
     */
    public GelfMessage addField(String key, String value) {
        getAdditonalFields().put(key, value);
        return this;
    }

    public Map<String, String> getAdditonalFields() {
        return additonalFields;
    }

    public boolean isValid() {
        return isShortOrFullMessagesExists() && !isEmpty(version) && !isEmpty(host) && !isEmpty(facility);
    }

    private boolean isShortOrFullMessagesExists() {
        return !isEmpty(shortMessage) || !isEmpty(fullMessage);
    }

    public boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    private byte[] concatByteArray(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public int getMaximumMessageSize() {
        return maximumMessageSize;
    }

    public void setMaximumMessageSize(int maximumMessageSize) {
        this.maximumMessageSize = maximumMessageSize;
    }

    public String getField(String fieldName) {
        return getAdditonalFields().get(fieldName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GelfMessage)) {
            return false;
        }

        GelfMessage that = (GelfMessage) o;

        if (javaTimestamp != that.javaTimestamp) {
            return false;
        }
        if (maximumMessageSize != that.maximumMessageSize) {
            return false;
        }
        if (additonalFields != null ? !additonalFields.equals(that.additonalFields) : that.additonalFields != null) {
            return false;
        }
        if (facility != null ? !facility.equals(that.facility) : that.facility != null) {
            return false;
        }
        if (fullMessage != null ? !fullMessage.equals(that.fullMessage) : that.fullMessage != null) {
            return false;
        }
        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (!Arrays.equals(hostBytes, that.hostBytes)) {
            return false;
        }
        if (level != null ? !level.equals(that.level) : that.level != null) {
            return false;
        }
        if (shortMessage != null ? !shortMessage.equals(that.shortMessage) : that.shortMessage != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (hostBytes != null ? Arrays.hashCode(hostBytes) : 0);
        result = 31 * result + (shortMessage != null ? shortMessage.hashCode() : 0);
        result = 31 * result + (fullMessage != null ? fullMessage.hashCode() : 0);
        result = 31 * result + (int) (javaTimestamp ^ (javaTimestamp >>> 32));
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (facility != null ? facility.hashCode() : 0);
        result = 31 * result + (additonalFields != null ? additonalFields.hashCode() : 0);
        result = 31 * result + maximumMessageSize;
        return result;
    }

}
