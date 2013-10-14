package biz.paluch.logging.gelf.logback;

import biz.paluch.logging.gelf.GelfUtil;
import biz.paluch.logging.gelf.LogEvent;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.log4j.MdcGelfMessageAssembler;


/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-08
 */
public class MdcLogbackGelfMessageAssembler extends MdcGelfMessageAssembler {

    @Override
    public GelfMessage createGelfMessage(LogEvent logEvent) {
        GelfMessage gelfMessage = super.createGelfMessage(logEvent);
        if (isMdcProfiling()) {
            GelfUtil.addMdcProfiling(gelfMessage);
        }

        return gelfMessage;
    }
}
