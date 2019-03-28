package biz.paluch.logging.gelf.jul;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class TestHandler extends Handler {

    static List<String> messages = new ArrayList<String>();

    public TestHandler() {
        super();
        configure();
    }

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cn = manager.getProperty(getClass().getName() + ".formatter");
        Formatter f;
        if (cn != null) {
            try {
                f = (Formatter) Class.forName(cn).newInstance();
            } catch (Exception e) {
                f = new SimpleFormatter();
            }
        } else {
            f = new SimpleFormatter();
        }
        this.setFormatter(f);
    }

    @Override
    public void publish(LogRecord record) {
        messages.add(getFormatter().format(record));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public static String[] getLoggedLines() {
        return messages.toArray(new String[messages.size()]);
    }

    public static void clear() {
        messages.clear();
    }
}
