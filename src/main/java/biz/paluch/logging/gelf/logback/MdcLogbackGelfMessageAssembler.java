package biz.paluch.logging.gelf.logback;

import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.MdcLogEvent;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.log4j.MdcGelfMessageAssembler;
import org.slf4j.MDC;


/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class MdcLogbackGelfMessageAssembler extends MdcGelfMessageAssembler {

    @Override
    public GelfMessage createGelfMessage(MdcLogEvent logEvent) {
        GelfMessage gelfMessage = super.createGelfMessage(logEvent);
        if (isMdcProfiling()) {
            GelfUtil.addMdcProfiling(gelfMessage);
        }

        for (String mdcField : getMdcFields()) {
            Object value = MDC.get(mdcField);
            if (value != null && !value.toString().equals("")) {
                gelfMessage.addField(mdcField, value.toString());
            }
        }

        return gelfMessage;
    }
}
