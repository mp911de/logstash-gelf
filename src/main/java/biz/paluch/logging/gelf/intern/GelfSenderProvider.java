package biz.paluch.logging.gelf.intern;

import java.io.IOException;


/**
 * 
 * (c) https://github.com/Batigoal/logstash-gelf.git
 *
 */
public interface GelfSenderProvider {

	public boolean supports(String host);
	public GelfSender create(GelfSenderConfiguration configuration) throws IOException;
}
