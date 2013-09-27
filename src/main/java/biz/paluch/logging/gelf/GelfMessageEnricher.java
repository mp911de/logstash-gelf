package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 18:22
 */
public interface GelfMessageEnricher {
    void enrichMessage(GelfMessage gelfMessage, LogEvent logEvent);
}
