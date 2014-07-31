package biz.paluch.logging.gelf.standalone;

import biz.paluch.logging.gelf.GelfMessageBuilder;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 31.07.14 08:47
 */
public class DatenpumpeImpl implements Datenpumpe {

    private GelfSenderConfiguration gelfSenderConfiguration;
    private GelfSender gelfSender = null;

    public DatenpumpeImpl(GelfSenderConfiguration gelfSenderConfiguration) {
        this.gelfSenderConfiguration = gelfSenderConfiguration;
    }

    @Override
    public void submit(Map<String, Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data map must not be null");
        }
        Map<String, String> fields = new HashMap<String, String>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            fields.put(entry.getKey(), entry.getValue().toString());
        }

        GelfMessage gelfMessage = GelfMessageBuilder.newInstance().withFields(fields).build();
        submit(gelfMessage);
    }

    @Override
    public void submit(GelfMessage gelfMessage) {
        if (gelfMessage == null) {
            throw new IllegalArgumentException("GelfMessage must not be null");
        }

        if (gelfSender == null) {
            gelfSender = GelfSenderFactory.createSender(gelfSenderConfiguration);
        }

        gelfSender.sendMessage(gelfMessage);

    }

    @Override
    public void submit(Object javaBean) {
        if (javaBean == null) {
            throw new IllegalArgumentException("Passed object must not be null");
        }

        Map<String, Object> fields = BeanPropertyExtraction.extractProperties(javaBean);

        submit(fields);
    }
}
