package biz.paluch.logging.gelf.intern;

import java.util.Map;

/**
 * Writer to write an object in JSON representation. Strings are UTF-8 encoded with a fast-path implementation. UTF-8 characters
 * are escaped with using control sequences {@code \t, \b, ...} or UTF-8 escape sequences {@code \u0aD5}.
 * 
 * @author Mark Paluch
 */
class JsonWriter {

    private static final byte[] NULL = "null".getBytes();
    private static final byte[] TRUE = "true".getBytes();
    private static final byte[] FALSE = "false".getBytes();

    private static final byte[] B = "\\b".getBytes();
    private static final byte[] T = "\\t".getBytes();
    private static final byte[] N = "\\n".getBytes();
    private static final byte[] F = "\\f".getBytes();
    private static final byte[] R = "\\r".getBytes();
    private static final byte[] QUOT = "\\\"".getBytes();
    private static final byte[] BSLASH = "\\\\".getBytes();
    private static final byte[] Q_AND_C = "\":".getBytes();
    private static final byte[] NaN = "NaN".getBytes();
    private static final byte[] Infinite = "Infinite".getBytes();

    private static final byte WRITE_UTF_UNKNOWN = (byte) '?';

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static final byte[] HEX_BYTES;
    
    static {
        int len = HEX_CHARS.length;
        HEX_BYTES = new byte[len];
        for (int i = 0; i < len; ++i) {
            HEX_BYTES[i] = (byte) HEX_CHARS[i];
        }
    }

    /**
     * Start JSON object.
     * 
     * @param out
     */
    public static void writeObjectStart(OutputAccessor out) {
        out.write('{');
    }

    /**
     * End JSON object.
     * 
     * @param out
     */
    public static void writeObjectEnd(OutputAccessor out) {
        out.write('}');
    }

    /**
     * Write key-value separator
     * 
     * @param out
     */
    public static void writeKeyValueSeparator(OutputAccessor out) {
        out.write(',');
    }

    /**
     * Utility method to write a {@link Map} into a JSON representation.
     * 
     * @param out
     * @param map
     */
    public static void toJSONString(OutputAccessor out, Map<String, ? extends Object> map) {

        writeObjectStart(out);
        boolean first = true;

        for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {

            if (first) {
                first = false;
            } else {
                writeKeyValueSeparator(out);
            }

            writeMapEntry(out, entry.getKey(), entry.getValue());
        }

        writeObjectEnd(out);

    }

    /**
     * Write a map entry.
     * 
     * @param out
     * @param key
     * @param value
     */
    public static void writeMapEntry(OutputAccessor out, String key, Object value) {

        out.write((byte) '\"');

        if (key == null)
            out.write(NULL);
        else {
            writeUtf8(out, key);
        }

        out.write(Q_AND_C);

        toJSONString(out, value);
    }

    /**
     * Convert an object to JSON text. Encode the value as UTF-8 text, or "null" if value is null or it's an NaN or an INF
     * number.
     */
    private static void toJSONString(OutputAccessor out, Object value) {

        if (value == null) {
            out.write(NULL);
            return;
        }

        if (value instanceof String) {
            out.write((byte) '\"');
            writeUtf8(out, (String) value);
            out.write((byte) '\"');
            return;
        }

        if (value instanceof Double) {
            Double d = (Double) value;
            if (d.isNaN()) {
                out.write((byte) '\"');
                out.write(NaN);
                out.write((byte) '\"');
            } else if (d.isInfinite()) {
                out.write((byte) '\"');
                if (d == Double.NEGATIVE_INFINITY) {
                    out.write('-');

                }
                out.write(Infinite);
                out.write((byte) '\"');
            } else {
                writeAscii(out, value.toString());

            }
            return;
        }

        if (value instanceof Float) {
            Float f = (Float) value;
            if (f.isNaN()) {
                out.write((byte) '\"');
                out.write(NaN);
                out.write((byte) '\"');
            } else if (f.isInfinite()) {
                out.write((byte) '\"');
                if (f == Float.NEGATIVE_INFINITY) {
                    out.write('-');

                }
                out.write(Infinite);
                out.write((byte) '\"');
            } else {
                writeAscii(out, value.toString());

            }
            return;
        }

        if (value instanceof Number) {
            writeAscii(out, value.toString());
        }

        if (value instanceof Boolean) {

            if ((Boolean) value) {
                out.write(TRUE);
            } else {
                out.write(FALSE);
            }
        }
    }

    private static void writeUtf8(OutputAccessor out, String string) {
        writeUtf8(out, string, string.length());
    }

    /**
     * Fast-Path ASCII implementation.
     */
    private static void writeAscii(OutputAccessor out, CharSequence seq) {

        // We can use the _set methods as these not need to do any index checks and reference checks.
        // This is possible as we called ensureWritable(...) before.
        for (int i = 0; i < seq.length(); i++) {
            out.write((byte) seq.charAt(i));
        }
    }

    /**
     * Fast-Path UTF-8 implementation.
     */
    private static void writeUtf8(OutputAccessor out, CharSequence seq, int len) {

        // We can use the _set methods as these not need to do any index checks and reference checks.
        // This is possible as we called ensureWritable(...) before.
        for (int i = 0; i < len; i++) {
            char c = seq.charAt(i);

            switch (c) {
                case '\b':
                    out.write(B);
                    continue;
                case '\t':
                    out.write(T);
                    continue;
                case '\n':
                    out.write(N);
                    continue;
                case '\f':
                    out.write(F);
                    continue;
                case '\r':
                    out.write(R);
                    continue;
                case '\"':
                    out.write(QUOT);
                    continue;
                case '\\':
                    out.write(BSLASH);
                    continue;
            }

            if (c < 0x20) {
                escape(out, c);
            } else if (c < 0x80) {
                out.write((byte) c);
            } else if (c < 0x800) {
                out.write((byte) (0xc0 | (c >> 6)));
                out.write((byte) (0x80 | (c & 0x3f)));
            } else if (isSurrogate(c)) {
                if (!Character.isHighSurrogate(c)) {
                    out.write(WRITE_UTF_UNKNOWN);
                    continue;
                }

                final char c2;
                try {
                    // Surrogate Pair consumes 2 characters. Optimistically try to get the next character to avoid
                    // duplicate bounds checking with charAt. If an IndexOutOfBoundsException is thrown we will
                    // re-throw a more informative exception describing the problem.
                    c2 = seq.charAt(++i);
                } catch (IndexOutOfBoundsException e) {
                    out.write(WRITE_UTF_UNKNOWN);
                    break;
                }
                if (!Character.isLowSurrogate(c2)) {
                    out.write(WRITE_UTF_UNKNOWN);
                    out.write((byte) (Character.isHighSurrogate(c2) ? WRITE_UTF_UNKNOWN : c2));
                    continue;
                }
                int codePoint = Character.toCodePoint(c, c2);
                // See http://www.unicode.org/versions/Unicode7.0.0/ch03.pdf#G2630.
                escape(out, c);
                escape(out, codePoint);
            } else {
                out.write((byte) (0xe0 | (c >> 12)));
                out.write((byte) (0x80 | ((c >> 6) & 0x3f)));
                out.write((byte) (0x80 | (c & 0x3f)));
            }
        }
    }

    /**
     * Write a UTF escape sequence using either an one or two-byte seqience.
     * 
     * @param out the output
     * @param charToEscape the character to escape.
     */
    private static void escape(OutputAccessor out, int charToEscape) {

        out.write('\\');
        out.write('u');
        if (charToEscape > 0xFF) {
            int hi = (charToEscape >> 8) & 0xFF;
            out.write(HEX_BYTES[hi >> 4]);
            out.write(HEX_BYTES[hi & 0xF]);
            charToEscape &= 0xFF;
        } else {
            out.write((byte) '0');
            out.write((byte) '0');
        }
        // We know it's a control char, so only the last 2 chars are non-0
        out.write(HEX_BYTES[charToEscape >> 4]);
        out.write(HEX_BYTES[charToEscape & 0xF]);
    }

    /**
     * Determine if {@code c} lies within the range of values defined for
     * <a href="http://unicode.org/glossary/#surrogate_code_point">Surrogate Code Point</a>.
     * 
     * @param c the character to check.
     * @return {@code true} if {@code c} lies within the range of values defined for
     *         <a href="http://unicode.org/glossary/#surrogate_code_point">Surrogate Code Point</a>. {@code false} otherwise.
     */
    private static boolean isSurrogate(char c) {
        return c >= '\uD800' && c <= '\uDFFF';
    }

}
