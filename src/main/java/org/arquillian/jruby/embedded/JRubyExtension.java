package org.arquillian.jruby.embedded;

import org.arquillian.jruby.resources.RubyResourceProvider;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

public class JRubyExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, JRubyDeployableContainer.class);
        builder.service(TestEnricher.class, RubyResourceProvider.class);
        builder.observer(JRubyTestObserver.class);
    }

}
