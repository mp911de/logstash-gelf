package biz.paluch.logging.gelf.intern;

import biz.paluch.logging.gelf.GelfMessageAssembler;
import biz.paluch.logging.gelf.intern.sender.DefaultGelfSenderProvider;
import biz.paluch.logging.gelf.intern.sender.RedisGelfSenderProvider;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 26.09.13 15:12
 */
public final class GelfSenderFactory {

    
    
    public static GelfSender createSender(final GelfMessageAssembler gelfMessageAssembler, final ErrorReporter errorReporter) {
        if (gelfMessageAssembler.getHost() == null) {
            errorReporter.reportError("Graylog2 hostname is empty!", null);
        } else {
            try {
                GelfSenderConfiguration senderConfiguration = new GelfSenderConfiguration() {

                    @Override
                    public int getPort() {
                        return gelfMessageAssembler.getPort();
                    }

                    @Override
                    public String getHost() {
                        return gelfMessageAssembler.getHost();
                    }

                    @Override
                    public ErrorReporter getErrorReport() {
                        return errorReporter;
                    }
                };

                for (GelfSenderProvider provider : SenderProviderHolder.getSenderProvider()) {
                    if (provider.supports(senderConfiguration.getHost())) {
                        return provider.create(senderConfiguration);
                    }
                }
                senderConfiguration.getErrorReport().reportError("No sender found for host " + senderConfiguration.getHost(), null);
                return null;
            } catch (UnknownHostException e) {
                errorReporter.reportError("Unknown Graylog2 hostname:" + gelfMessageAssembler.getHost(), e);
            } catch (SocketException e) {
                errorReporter.reportError("Socket exception: " + e.getMessage(), e);
            } catch (IOException e) {
                errorReporter.reportError("IO exception: " + e.getMessage(), e);
            }
        }

        return null;
    }
    
    public static void addGelfSenderProvider(GelfSenderProvider provider) {
       SenderProviderHolder.addSenderProvider(provider);
    }

    // For thread safe lazy intialization of provider list
    private static class SenderProviderHolder {
        private static ServiceLoader<GelfSenderProvider> gelfSenderProvider = ServiceLoader.load(GelfSenderProvider.class);
        private static List<GelfSenderProvider> providerList = new ArrayList<GelfSenderProvider>();
        
        static {
            Iterator<GelfSenderProvider> iter = gelfSenderProvider.iterator();
            while (iter.hasNext()) {
                providerList.add(iter.next());
            }
            providerList.add(new RedisGelfSenderProvider());
            providerList.add(new DefaultGelfSenderProvider());
        }

        static List<GelfSenderProvider> getSenderProvider() {
            return providerList;
        }
        
        static void addSenderProvider(GelfSenderProvider provider) {
            synchronized (providerList) {
                if(!providerList.contains(provider)) {
                    providerList.add(0, provider); // To take precedence over built-in providers
                }
            }
        }
    }

}
