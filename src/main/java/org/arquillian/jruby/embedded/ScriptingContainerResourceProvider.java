package org.arquillian.jruby.embedded;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Annotation;

public class ScriptingContainerResourceProvider implements ResourceProvider {

    @Inject
    private Instance<ScriptingContainer> scriptingContainerInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.equals(ScriptingContainer.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return scriptingContainerInstance.get();
    }
}
