package biz.paluch.logging.gelf.intern;

import static biz.paluch.logging.gelf.intern.JsonWriter.writeKeyValueSeparator;
import static biz.paluch.logging.gelf.intern.JsonWriter.writeMapEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import biz.paluch.logging.gelf.intern.ValueDiscovery.Result;

/**
 * Represents a Gelf message. A Gelf message contains all required Fields according to the Gelf Spec.
 *
 * A {@link GelfMessage} can be converted to {@link #toJson()}, to {@link #toTCPBuffer()} and to {@link #toUDPBuffers()}. It
 * also provides methods accepting {@link ByteBuffer} to reduce GC pressure.
 *
 * @author https://github.com/t0xa/gelfj
 * @author Mark Paluch
 * @author Thomas Herzog
 * @see <a href="http://docs.graylog.org/en/2.0/pages/gelf.html">http://docs.graylog.org/en/2.0/pages/gelf.html</a>
 */
public class GelfMessage {

    private static final Random rand = new Random();

    public static final String FIELD_HOST = "host";
    public static final String FIELD_SHORT_MESSAGE = "short_message";
    public static final String FIELD_FULL_MESSAGE = "full_message";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_FACILITY = "facility";
    public static final String ID_NAME = "id";

    /**
     * Discover the field type by trying to parse it.
     */
    public static final String FIELD_TYPE_DISCOVER = "discover";

    /**
     * String field type.
     */
    public static final String FIELD_TYPE_STRING = "String";

    /**
     * long field type. Zero if value cannot be converted.
     */
    public static final String FIELD_TYPE_LONG = "long";

    /**
     * Long field type. Null if value cannot be converted.
     */
    public static final String FIELD_TYPE_LONG2 = "Long";

    /**
     * double field type. Zero if value cannot be converted.
     */
    public static final String FIELD_TYPE_DOUBLE = "double";

    /**
     * Double field type. Null if value cannot be converted.
     */
    public static final String FIELD_TYPE_DOUBLE2 = "Double";

    /*
     * Default field type: Discover
     */
    public static final String FIELD_TYPE_DEFAULT = FIELD_TYPE_DISCOVER;

    public static final String GELF_VERSION_1_0 = "1.0";
    public static final String GELF_VERSION_1_1 = "1.1";

    public static final String GELF_VERSION = GELF_VERSION_1_0;

    public static final String DEFAULT_FACILITY = "logstash-gelf";
    public static final int DEFAULT_MESSAGE_SIZE = 8192;
    public static final int DEFAUL_LEVEL = 7;

    private static final byte[] GELF_CHUNKED_ID = new byte[] { 0x1e, 0x0f };
    private static final BigDecimal TIME_DIVISOR = new BigDecimal(1000);
    private static final byte[] NONE = lastFourAsciiBytes("none");

    private String version = GELF_VERSION;
    private String host;
    private String shortMessage;
    private String fullMessage;
    private long javaTimestamp;
    private String level;
    private String facility = DEFAULT_FACILITY;
    private Map<String, String> additonalFields = new HashMap<>();
    private Map<String, String> additionalFieldTypes = new HashMap<>();
    private Map<Pattern, String> dynamicMdcFieldTypes = Collections.emptyMap();
    private int maximumMessageSize = DEFAULT_MESSAGE_SIZE;

    public GelfMessage() {
    }

    public GelfMessage(String shortMessage, String fullMessage, long timestamp, String level) {

        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
        this.javaTimestamp = timestamp;
        this.level = level;
    }

    /**
     * Create a JSON representation for this {@link GelfMessage}. Additional fields are prefixed with underscore {@code _}.
     *
     * @return the JSON string.
     */
    public String toJson() {
        return toJson("_");
    }

    /**
     * Create a JSON representation for this {@link GelfMessage}. Additional fields are prefixed with
     * {@code additionalFieldPrefix}.
     *
     * @param additionalFieldPrefix must not be {@literal null}
     * @return the JSON string.
     */
    public String toJson(String additionalFieldPrefix) {
        return new String(toJsonByteArray(additionalFieldPrefix), Charsets.UTF8);
    }

    private byte[] toJsonByteArray(String additionalFieldPrefix) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        toJson(OutputAccessor.from(buffer), additionalFieldPrefix);

        return buffer.toByteArray();
    }

    /**
     * Create a JSON representation for this {@link GelfMessage} and write it to the {@link ByteBuffer}. Additional fields are
     * prefixed with {@code additionalFieldPrefix}.
     *
     * @param byteBuffer must not be {@literal null}
     * @param additionalFieldPrefix must not be {@literal null}
     */
    public void toJson(ByteBuffer byteBuffer, String additionalFieldPrefix) {
        toJson(OutputAccessor.from(byteBuffer), additionalFieldPrefix);
    }

    protected void toJson(OutputAccessor out, String additionalFieldPrefix) {

        JsonWriter.writeObjectStart(out);

        boolean hasFields = writeIfNotEmpty(out, false, FIELD_HOST, getHost());

        if (!isEmpty(shortMessage)) {
            hasFields = writeIfNotEmpty(out, hasFields, FIELD_SHORT_MESSAGE, getShortMessage());
        }

        hasFields = writeIfNotEmpty(out, hasFields, FIELD_FULL_MESSAGE, getFullMessage());

        if (getJavaTimestamp() != 0) {
            if (GELF_VERSION_1_1.equals(version)) {
                hasFields = writeIfNotEmpty(out, hasFields, FIELD_TIMESTAMP, getTimestampAsBigDecimal().doubleValue());
            } else {
                hasFields = writeIfNotEmpty(out, hasFields, FIELD_TIMESTAMP, getTimestampAsBigDecimal().toString());
            }
        }

        if (!isEmpty(getLevel())) {
            if (GELF_VERSION_1_1.equals(version)) {
                int level;
                try {
                    level = Integer.parseInt(getLevel());
                } catch (NumberFormatException ex) {
                    // fallback on the default value
                    level = DEFAUL_LEVEL;
                }
                hasFields = writeIfNotEmpty(out, hasFields, FIELD_LEVEL, level);
            } else {
                hasFields = writeIfNotEmpty(out, hasFields, FIELD_LEVEL, getLevel());
            }
        }

        if (!isEmpty(getFacility())) {
            hasFields = writeIfNotEmpty(out, hasFields, FIELD_FACILITY, getFacility());
        }

        for (Map.Entry<String, String> additionalField : additonalFields.entrySet()) {
            if (!ID_NAME.equals(additionalField.getKey()) && additionalField.getValue() != null) {
                String value = additionalField.getValue();
                String fieldType = additionalFieldTypes.get(additionalField.getKey());
                if (fieldType == null) {
                    fieldType = getMatchingDynamicMdcFieldType(additionalField.getKey());
                    if (fieldType == null) {
                        fieldType = FIELD_TYPE_DEFAULT;
                    }
                }
                Object result = getAdditionalFieldValue(value, fieldType);
                if (result != null) {
                    hasFields = writeIfNotEmpty(out, hasFields, additionalFieldPrefix + additionalField.getKey(), result);
                }
            }
        }

        JsonWriter.writeObjectEnd(out);
    }

    /**
     * Write a Value to JSON if the value is not empty.
     */
    private static boolean writeIfNotEmpty(OutputAccessor out, boolean hasFields, String field, Object value) {

        if (value == null) {
            return hasFields;
        }

        if (value instanceof String) {

            if (GelfMessage.isEmpty((String) value)) {
                return hasFields;
            }

            if (hasFields) {
                writeKeyValueSeparator(out);
            }

            writeMapEntry(out, field, value);

            return true;
        }

        if (hasFields) {
            writeKeyValueSeparator(out);
        }

        writeMapEntry(out, field, value);

        return true;
    }

    /**
     * Get the field value as requested data type.
     *
     * @param value the value as string
     * @param fieldType see field types
     * @return the field value in the appropriate data type or {@literal null}.
     */
    static Object getAdditionalFieldValue(String value, String fieldType) {

        Object result = null;
        if (fieldType.equalsIgnoreCase(FIELD_TYPE_DISCOVER)) {

            Result discoveredType = ValueDiscovery.discover(value);

            if (discoveredType == Result.STRING) {
                return value;
            }

            if (discoveredType == Result.LONG) {
                try {
                    // try adding the value as a long
                    return Long.parseLong(value);
                } catch (NumberFormatException ex) {
                    // fallback on the double value
                    return value;
                }
            }

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                // fallback on the string value
                return value;
            }
        }

        if (fieldType.equalsIgnoreCase(FIELD_TYPE_STRING)) {
            result = value;
        }

        if (fieldType.equals(FIELD_TYPE_DOUBLE) || fieldType.equalsIgnoreCase(FIELD_TYPE_DOUBLE2)) {
            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                if (fieldType.equals(FIELD_TYPE_DOUBLE)) {
                    result = Double.valueOf(0);
                }
            }
        }

        if (fieldType.equals(FIELD_TYPE_LONG) || fieldType.equalsIgnoreCase(FIELD_TYPE_LONG2)) {
            try {
                result = (long) Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                if (fieldType.equals(FIELD_TYPE_LONG)) {
                    result = Long.valueOf(0);
                }
            }
        }

        return result;
    }

    public ByteBuffer[] toUDPBuffers() {

        byte[] messageBytes = gzipMessage(toJsonByteArray("_"));

        if (messageBytes.length > maximumMessageSize) {
            // calculate the length of the datagrams array
            int datagrams_length = messageBytes.length / maximumMessageSize;
            // In case of a remainder, due to the integer division, add a extra datagram
            if (messageBytes.length % maximumMessageSize != 0) {
                datagrams_length++;
            }

            ByteBuffer targetBuffer = ByteBuffer.allocate(messageBytes.length + (datagrams_length * 12));

            return sliceDatagrams(ByteBuffer.wrap(messageBytes), datagrams_length, targetBuffer);
        }

        ByteBuffer[] datagrams = new ByteBuffer[1];
        datagrams[0] = ByteBuffer.allocate(messageBytes.length);
        datagrams[0].put(messageBytes);
        datagrams[0].flip();
        return datagrams;
    }

    public ByteBuffer[] toUDPBuffers(ByteBuffer buffer, ByteBuffer tempBuffer) {

        tempBuffer.put(gzipMessage(toJsonByteArray("_")));

        // calculate the length of the datagrams array

        if (tempBuffer.position() > maximumMessageSize) {

            int diagrams_length = tempBuffer.position() / maximumMessageSize;
            // In case of a remainder, due to the integer division, add a extra datagram
            if (tempBuffer.position() % maximumMessageSize != 0) {
                diagrams_length++;
            }

            buffer.clear();
            return sliceDatagrams((ByteBuffer) tempBuffer.flip(), diagrams_length, buffer);
        }

        return new ByteBuffer[] { (ByteBuffer) tempBuffer.flip() };
    }

    public ByteBuffer toTCPBuffer() {

        // Do not use GZIP, as the headers will contain \0 bytes
        // graylog2-server uses \0 as a delimiter for TCP frames
        // see: https://github.com/Graylog2/graylog2-server/issues/127
        byte[] messageBytes = toJsonByteArray("_");

        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length + 1);
        buffer.put(messageBytes);
        buffer.put((byte) '\0');
        buffer.flip();
        return buffer;
    }

    public ByteBuffer toTCPBuffer(ByteBuffer buffer) {
        // Do not use GZIP, as the headers will contain \0 bytes
        // graylog2-server uses \0 as a delimiter for TCP frames
        // see: https://github.com/Graylog2/graylog2-server/issues/127

        toJson(buffer, "_");

        buffer.put((byte) '\0');
        buffer.flip();
        return buffer;
    }

    protected ByteBuffer[] sliceDatagrams(ByteBuffer source, int datagrams, ByteBuffer target) {
        int messageLength = source.limit();

        byte[] msgId = generateMsgId();

        // Reuse length of datagrams array since this is supposed to be the correct number of datagrams
        ByteBuffer[] slices = new ByteBuffer[datagrams];
        for (int idx = 0; idx < datagrams; idx++) {

            int start = target.position();
            target.put(GELF_CHUNKED_ID).put(msgId).put((byte) idx).put((byte) datagrams);

            int from = idx * maximumMessageSize;
            int to = from + maximumMessageSize;
            if (to >= messageLength) {
                to = messageLength;
            }

            ByteBuffer duplicate = (ByteBuffer) source.duplicate().limit(to).position(from);
            target.put(duplicate);
            int end = target.position();

            slices[idx] = (ByteBuffer) target.duplicate().limit(end).position(start);
        }

        return slices;
    }

    byte[] generateMsgId() {
        // Considerations about generating the message ID: The GELF documentation suggests to
        // "[g]enerate [the id] from millisecond timestamp + hostname for example":
        // https://docs.graylog.org/en/3.1/pages/gelf.html#chunking
        //
        // However, relying on current time in milliseconds on the same system will result in a high
        // collision probability if lots of messages are generated quickly. Things will be even
        // worse if multiple servers send to the same log server. Adding the hostname is not
        // guaranteed to help, and if the hostname is the FQDN it is even unlikely to be unique at
        // all.
        //
        // The GELF module used by Logstash uses the first eight bytes of an MD5 hash of the current
        // time as floating point, a hyphen, and an eight byte random number:
        // https://github.com/logstash-plugins/logstash-output-gelf
        // https://github.com/graylog-labs/gelf-rb/blob/master/lib/gelf/notifier.rb#L239 It probably
        // doesn't have to be that clever:
        //
        // Using the timestamp plus a random number will mean we only have to worry about collision
        // of random numbers within the same milliseconds. How short can the timestamp be before it
        // will collide with old timestamps? Every second Graylog will evict expired messaged (5
        // seconds old) from the pool:
        // https://github.com/Graylog2/graylog2-server/blob/master/graylog2-server/src/main/java/org/graylog2/inputs/codecs/GelfChunkAggregator.java
        // Thus, we just need six seconds which will require two bytes. Then we can spend six bytes
        // on a random number.

        return ByteBuffer.allocate(8).putLong(getRandomLong())
                // Overwrite the last two bytes with the timestamp.
                .putShort(6, getCurrentTimeMillis()).array();
    }

    long getRandomLong() {
        return rand.nextLong();
    }

    short getCurrentTimeMillis() {
        return (short) System.currentTimeMillis();
    }

    private byte[] gzipMessage(byte[] message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            GZIPOutputStream stream = new GZIPOutputStream(bos);

            stream.write(message);
            stream.finish();
            Closer.close(stream);
            byte[] zipped = bos.toByteArray();
            Closer.close(bos);
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] lastFourAsciiBytes(String host) {
        final String shortHost = host.length() >= 4 ? host.substring(host.length() - 4) : host;
        return Charsets.ascii(shortHost);
    }

    private String getMatchingDynamicMdcFieldType(String fieldName) {
        String fieldType = null;
        for (Map.Entry<Pattern, String> entry : dynamicMdcFieldTypes.entrySet()) {
            if(entry.getKey().matcher(fieldName).matches()) {
                fieldType = entry.getValue();
                break;
            }
        }

        return fieldType;
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

    public BigDecimal getTimestampAsBigDecimal() {
        return new BigDecimal(javaTimestamp).divide(TIME_DIVISOR);
    }

    public String getTimestamp() {
        return getTimestampAsBigDecimal().toPlainString();
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

    public Map<String, String> getAdditionalFieldTypes() {
        return additionalFieldTypes;
    }

    public void setAdditionalFieldTypes(Map<String, String> additionalFieldTypes) {
        this.additionalFieldTypes.putAll(additionalFieldTypes);
    }

    public void setDynamicMdcFieldTypes(Map<Pattern, String> dynamicMdcFieldTypes) {
        this.dynamicMdcFieldTypes = dynamicMdcFieldTypes;
    }

    /**
     * Add multiple fields (key/value pairs)
     *
     * @param fields map of fields
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
     * @param key the key
     * @param value the value
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
        return isShortOrFullMessagesExists() && !isEmpty(version) && !isEmpty(host);
    }

    private boolean isShortOrFullMessagesExists() {
        return !isEmpty(shortMessage) || !isEmpty(fullMessage);
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
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
