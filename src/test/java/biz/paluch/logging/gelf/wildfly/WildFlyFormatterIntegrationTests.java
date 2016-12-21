package biz.paluch.logging.gelf.wildfly;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
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

/**
 * Test for the Formatter of the logstash-gelf module in WildFly.
 *
 * @author Mark Paluch
 * @since 11.08.14 08:36
 */
@RunWith(Arquillian.class)
@ServerSetup({ WildFlyFormatterIntegrationTests.LoggerSetup.class })
public class WildFlyFormatterIntegrationTests {

    static class LoggerSetup implements ServerSetupTask {

        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {

            CommandContext commandContext = CommandContextFactory.getInstance().newCommandContext();
            commandContext.bindClient(managementClient.getControllerClient());
            commandContext
                    .handle("/subsystem=logging/custom-formatter=JsonFormatter/:add(module=biz.paluch.logging,class=biz.paluch.logging.gelf.wildfly.WildFlyJsonFormatter,properties={ \\\n"
                            + "\t\t   version=\"1.0\", \\\n"
                            + "\t\t   facility=\"java-test\", \\\n"
                            + "\t\t   extractStackTrace=true, \\\n"
                            + "\t\t   filterStackTrace=true, \\\n"
                            + "\t\t   mdcProfiling=true, \\\n"
                            + "\t\t   additionalFields=\"fieldName1=fieldValue1,fieldName2=fieldValue2\", \\\n"
                            + "\t\t   mdcFields=\"mdcField1,mdcField2\"})");

            commandContext
                    .handle("/subsystem=logging/file-handler=JsonLog/:add(file={\"relative-to\"=>\"jboss.server.log.dir\" ,path=server.json}, \\\n"
                            + "            level=ALL,named-formatter=JsonFormatter)");

            commandContext
                    .handle("/subsystem=logging/root-logger=ROOT/:write-attribute(name=handlers,value=[\"FILE\",\"CONSOLE\",\"JsonLog\"])");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            CommandContext commandContext = CommandContextFactory.getInstance().newCommandContext();
            commandContext.bindClient(managementClient.getControllerClient());
            commandContext
                    .handle("/subsystem=logging/root-logger=ROOT/:write-attribute(name=handlers,value=[\"FILE\",\"CONSOLE\"])");
            commandContext.handle("/subsystem=logging/file-handler=JsonLog/:remove");
            commandContext.handle("/subsystem=logging/custom-formatter=JsonFormatter/:remove");
        }
    }

    @Deployment
    public static Archive<?> createTestArchive() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml").resolve("commons-io:commons-io:2.2").withoutTransitivity()
                .asFile();
        return ShrinkWrap.create(WebArchive.class, "logstash-gelf.war").addAsLibraries(files);
    }

    @Test
    public void testJsonLogging() throws Exception {

        LogManager.getLogger(getClass()).info("some message");

        File logDir = new File(System.getProperty("jboss.server.log.dir"));
        File logFile = new File(logDir, "server.json");
        assertTrue(logFile.exists());

        String fileContents = FileUtils.readFileToString(logFile);

        assertTrue(fileContents.contains("\"full_message\":\"some message\""));
    }
}
