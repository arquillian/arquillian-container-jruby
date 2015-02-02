package org.arquillian.jruby.embedded;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Annotation;

public class RubyResourceProvider implements ResourceProvider {

    @Override
    public boolean canProvide(Class<?> type) {
        return type.equals(Ruby.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return JRubyDeployableContainer.rubyInstance;
    }
}
