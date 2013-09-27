package biz.paluch.logging.gelf;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 18:29
 */
public interface MdcLogEvent extends LogEvent {
    Object getMDC(String item);
}
