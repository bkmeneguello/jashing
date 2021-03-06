package com.github.avarabyeu.jashing.integration.vcs;

import com.github.avarabyeu.jashing.core.IncorrectConfigurationException;
import com.github.avarabyeu.jashing.utils.ResourceUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * VCS Configuration module
 *
 * @author Andrei Varabyeu
 */
public abstract class AbstractVcsModule extends AbstractModule {

    public static final String VCS_CONFIG_JSON = "vcs-config.json";

    @Override
    protected void configure() {
        Multibinder<VCSClient> multibinder = Multibinder.newSetBinder(binder(), VCSClient.class);
        getClients().stream().forEach(client -> multibinder.addBinding()
                .toProvider(() -> client));
    }

    /**
     * Creates composite VCS clients from another client in context
     *
     * @param clients Client delegates from context
     * @return Composite client
     */
    @Provides
    @Singleton
    public VCSClient vcsClient(Set<VCSClient> clients) {
        return new CompositeVCSClient(clients);
    }

    /**
     * Loads VCS configuration
     *
     * @param gson GSON for deserialization
     * @return Loaded configuration
     */
    @Provides
    public VCSConfiguration loadConfiguration(Gson gson) {
        try {
            URL resource = ResourceUtils.getResourceAsURL(VCS_CONFIG_JSON);
            if (null == resource) {
                throw new IncorrectConfigurationException("Unable to find VCS configuration");
            }
            return gson.fromJson(Resources.asCharSource(resource, Charsets.UTF_8).openStream(),
                    VCSConfiguration.class);
        } catch (IOException e) {
            throw new IncorrectConfigurationException("Unable to read VCS configuration", e);
        }
    }

    /**
     * @return Set of VCS clients
     */
    protected abstract List<VCSClient> getClients();
}
