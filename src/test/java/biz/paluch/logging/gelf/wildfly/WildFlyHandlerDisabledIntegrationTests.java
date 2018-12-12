package biz.paluch.logging.gelf.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.log4j.LogManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import biz.paluch.logging.gelf.netty.GelfInboundHandler;
import biz.paluch.logging.gelf.netty.NettyLocalServer;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Test for the GelfLogHandler of the logstash-gelf module in WildFly.
 *
 * @author Mark Paluch
 * @since 11.08.14 08:36
 */
@RunWith(Arquillian.class)
@ServerSetup({ WildFlyHandlerDisabledIntegrationTests.LoggerSetup.class })
public class WildFlyHandlerDisabledIntegrationTests {

    static class LoggerSetup implements ServerSetupTask {

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {

            CommandContext commandContext = CommandContextFactory.getInstance().newCommandContext();
            commandContext.bindClient(managementClient.getControllerClient());
            commandContext
                    .handle("/subsystem=logging/custom-handler=GelfLogger/:add(module=biz.paluch.logging,class=biz.paluch.logging.gelf.wildfly.WildFlyGelfLogHandler,enabled=false,properties={ \\\n"
                            + "           host=\"udp:localhost\", \\\n"
                            + "           port=\"19392\", \\\n"
                            + "           version=\"1.0\", \\\n"
                            + "           facility=\"java-test\", \\\n"
                            + "           extractStackTrace=true, \\\n"
                            + "           filterStackTrace=true, \\\n"
                            + "           mdcProfiling=true, \\\n"
                            + "           timestampPattern=\"yyyy-MM-dd HH:mm:ss,SSS\", \\\n"
                            + "           maximumMessageSize=8192, \\\n"
                            + "           additionalFields=\"fieldName1=fieldValue1,fieldName2=fieldValue2\", \\\n"
                            + "           mdcFields=\"mdcField1,mdcField2\" \\\n"
                            + "           dynamicMdcFields=\"mdc.*,(mdc|MDC)fields\" \\\n"
                            + "           includeFullMdc=true \\\n"
                            + "    })");

            commandContext.handle(
                    "/subsystem=logging/root-logger=ROOT/:write-attribute(name=handlers,value=[\"FILE\",\"CONSOLE\",\"GelfLogger\"])");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            CommandContext commandContext = CommandContextFactory.getInstance().newCommandContext();
            commandContext.bindClient(managementClient.getControllerClient());
            commandContext
                    .handle("/subsystem=logging/root-logger=ROOT/:write-attribute(name=handlers,value=[\"FILE\",\"CONSOLE\"])");
            commandContext.handle("/subsystem=logging/custom-handler=GelfLogger/:remove()");
        }
    }

    @Deployment
    public static Archive<?> createTestArchive() {

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").resolve("io.netty:netty-all", "org.assertj:assertj-core")
                .withoutTransitivity().asFile();
        return ShrinkWrap.create(WebArchive.class, "logstash-gelf.war").addAsLibraries(files).addClasses(NettyLocalServer.class,
                GelfInboundHandler.class);
    }

    @Test
    public void testGelfSubmissionToEmbeddedNettyGelfServer() throws Exception {
        NettyLocalServer nettyLocalServer = new NettyLocalServer(NioDatagramChannel.class);
        nettyLocalServer.run();
        String logMessage = "some log event, console/file only, gelf disabled";

        int iterations = 0;
        while (nettyLocalServer.getJsonValues().isEmpty() && iterations < 10) {
            LogManager.getLogger(getClass()).info(logMessage);
            Thread.sleep(100);
            iterations++;
        }

        assertThat(nettyLocalServer.getJsonValues()).isEmpty();

        boolean foundSomeLogEvent = false;

        for (Object o : nettyLocalServer.getJsonValues()) {
            if (o.toString().contains(logMessage)) {
                foundSomeLogEvent = true;
            }
        }
        assertThat(foundSomeLogEvent).isFalse();
        nettyLocalServer.close();
    }
}
