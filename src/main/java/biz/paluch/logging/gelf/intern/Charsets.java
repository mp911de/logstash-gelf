package biz.paluch.logging.gelf.intern;

import java.nio.charset.Charset;

/**
 * @author Mark Paluch
 * @since 17.07.14 10:45
 */
class Charsets {

    public static final Charset ASCII = Charset.forName("ASCII");
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final byte[] ascii(String input) {
        return input.getBytes(ASCII);
    }

    public static final byte[] utf8(String input) {
        return input.getBytes(UTF8);
    }

}
