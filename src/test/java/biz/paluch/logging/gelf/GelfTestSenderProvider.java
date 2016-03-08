package biz.paluch.logging.gelf;

import java.io.IOException;

import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderProvider;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class GelfTestSenderProvider implements GelfSenderProvider {

    @Override
    public boolean supports(String host) {
        return host.startsWith("test:");
    }

    @Override
    public GelfSender create(GelfSenderConfiguration configuration) throws IOException {
        return new GelfTestSender();
    }

}
