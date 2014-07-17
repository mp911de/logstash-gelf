package biz.paluch.logging.gelf.intern;

import java.nio.charset.Charset;

/**
 * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
 * @since 17.07.14 10:45
 */
class Charsets {
    public final static Charset ASCII = Charset.forName("ASCII");
    public final static Charset UTF8 = Charset.forName("UTF-8");

    public final static byte[] ascii(String input) {
        return input.getBytes(ASCII);
    }

    public final static byte[] utf8(String input) {
        return input.getBytes(UTF8);
    }

}
