package org.arquillian.jruby.resources;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;

import java.lang.annotation.Annotation;

public class RubyResourceProvider implements ResourceProvider {


    @Inject
    private Instance<ScopedResources> scopedResourcesInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.equals(Ruby.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        if (qualifiers == null) {
            return scopedResourcesInstance.get().getTestScopedScriptingContainer().getProvider().getRuntime();
        }
        for (Annotation qualifier: qualifiers) {
            if (qualifier.annotationType() == ApplicationScoped.class) {
                return scopedResourcesInstance.get().getClassScopedScriptingContainer().getProvider().getRuntime();
            }
        }
        return scopedResourcesInstance.get().getTestScopedScriptingContainer().getProvider().getRuntime();
    }
}
