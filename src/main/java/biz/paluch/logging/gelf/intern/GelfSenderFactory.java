package biz.paluch.logging.gelf.intern;

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

    /**
     * Create a GelfSender based on the configuration.
     * 
     * @param hostAndPortProvider
     * @return GelfSender.
     */
    public static GelfSender createSender(final HostAndPortProvider hostAndPortProvider, final ErrorReporter errorReporter) {
        GelfSenderConfiguration senderConfiguration = new GelfSenderConfiguration() {

            @Override
            public int getPort() {
                return hostAndPortProvider.getPort();
            }

            @Override
            public String getHost() {
                return hostAndPortProvider.getHost();
            }

            @Override
            public ErrorReporter getErrorReporter() {
                return errorReporter;
            }
        };

        return createSender(senderConfiguration);
    }

    /**
     * Create a GelfSender based on the configuration.
     * 
     * @param senderConfiguration
     * @return GelfSender.
     */
    public static GelfSender createSender(GelfSenderConfiguration senderConfiguration) {
        ErrorReporter errorReporter = senderConfiguration.getErrorReporter();
        if (senderConfiguration.getHost() == null) {
            senderConfiguration.getErrorReporter().reportError("GELF server hostname is empty!", null);
        } else {
            try {

                for (GelfSenderProvider provider : SenderProviderHolder.getSenderProvider()) {
                    if (provider.supports(senderConfiguration.getHost())) {
                        return provider.create(senderConfiguration);
                    }
                }
                senderConfiguration.getErrorReporter().reportError("No sender found for host " + senderConfiguration.getHost(),
                        null);
                return null;
            } catch (UnknownHostException e) {
                errorReporter.reportError("Unknown GELF server hostname:" + senderConfiguration.getHost(), e);
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

    public static void removeGelfSenderProvider(GelfSenderProvider provider) {
        SenderProviderHolder.removeSenderProvider(provider);
    }

    public static void removeAllAddedSenderProviders() {
        SenderProviderHolder.removeAllAddedSenderProviders();
    }

    // For thread safe lazy intialization of provider list
    private static class SenderProviderHolder {
        private static ServiceLoader<GelfSenderProvider> gelfSenderProvider = ServiceLoader.load(GelfSenderProvider.class);
        private static List<GelfSenderProvider> providerList = new ArrayList<GelfSenderProvider>();
        private static List<GelfSenderProvider> addedProviders = new ArrayList<GelfSenderProvider>();

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
                addedProviders.add(provider);
                if (!providerList.contains(provider)) {
                    providerList.add(0, provider); // To take precedence over built-in providers
                }
            }
        }

        static void removeAllAddedSenderProviders() {
            synchronized (providerList) {
                providerList.removeAll(addedProviders);
                addedProviders.clear();
            }
        }

        static void removeSenderProvider(GelfSenderProvider provider) {
            synchronized (providerList) {
                addedProviders.remove(provider);
                providerList.remove(provider);
            }
        }
    }

}
