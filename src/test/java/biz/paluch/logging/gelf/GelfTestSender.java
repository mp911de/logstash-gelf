package biz.paluch.logging.gelf;

import java.util.ArrayList;
import java.util.List;

import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 27.09.13 07:45
 */
public class GelfTestSender implements GelfSender {
    private static List<GelfMessage> messages = new ArrayList<GelfMessage>();

    @Override
    public boolean sendMessage(GelfMessage message) {
        synchronized (messages) {
            messages.add(message);
        }
        return true;
    }

    @Override
    public void close() {

    }

    public static List<GelfMessage> getMessages() {
        return messages;
    }
}
