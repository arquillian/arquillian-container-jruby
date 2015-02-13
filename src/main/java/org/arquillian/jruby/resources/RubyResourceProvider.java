package org.arquillian.jruby.resources;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;

public class RubyResourceProvider extends AbstractJRubyResourceProvider implements ResourceProvider {

    @Override
    public boolean canProvide(Class<?> type) {
        return type.equals(Ruby.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        try {
            if (qualifiers == null) {
                return getOrCreateTestMethodScopedScriptingContainer().getProvider().getRuntime();
            }
            for (Annotation qualifier : qualifiers) {
                if (qualifier.annotationType() == ApplicationScoped.class) {
                    return getOrCreateClassScopedScriptingContainer().getProvider().getRuntime();
                }
            }
            return getOrCreateTestMethodScopedScriptingContainer().getProvider().getRuntime();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
