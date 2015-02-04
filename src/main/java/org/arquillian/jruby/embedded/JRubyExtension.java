package org.arquillian.jruby.embedded;

import org.arquillian.jruby.resources.RubyResourceProvider;
import org.arquillian.jruby.resources.ScriptingContainerResourceProvider;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class JRubyExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, JRubyDeployableContainer.class);
        builder.service(ResourceProvider.class, RubyResourceProvider.class);
        builder.service(ResourceProvider.class, ScriptingContainerResourceProvider.class);

        builder.observer(JRubyTestObserver.class);
    }

}
