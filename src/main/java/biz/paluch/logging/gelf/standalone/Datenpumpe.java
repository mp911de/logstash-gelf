package biz.paluch.logging.gelf.standalone;

import biz.paluch.logging.gelf.intern.GelfMessage;

import java.util.Map;

/**
 * Datenpumpe allows to submit arbitrary values (flat data set) using Gelf.
 * 
 * @author Mark Paluch
 * @since 31.07.14 08:43
 */
public interface Datenpumpe {
    /**
     * Submit a map of key-value pairs using Gelf.
     * 
     * @param data map containing the data, must not be {@literal null}
     */
    void submit(Map<String, Object> data);

    /**
     * Submit a GelfMessage.
     * 
     * @param gelfMessage the message, must not be {@literal null}
     */
    void submit(GelfMessage gelfMessage);

    /**
     * Submit a Java bean. All accessible fields will be used in a property manner to submit the data.
     *
     * @param javaBean the java bean, must not be {@literal null}
     */
    void submit(Object javaBean);
}
