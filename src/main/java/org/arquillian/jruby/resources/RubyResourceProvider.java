package org.arquillian.jruby.resources;

import org.arquillian.jruby.util.AnnotationUtils;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class RubyResourceProvider implements ResourceProvider {

    @Inject
    @ApplicationScoped
    protected Instance<ScopedResources> scopedResourcesInstance;

    @Override
    public boolean canProvide(Class<?> aClass) {
        return aClass == Ruby.class;
    }

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {

        if (AnnotationUtils.filterAnnotation(annotations, ResourceProvider.MethodInjection.class) != null) {
            return scopedResourcesInstance.get().getTestScopedScriptingContainer().getProvider().getRuntime();
        } else if (AnnotationUtils.filterAnnotation(annotations, ResourceProvider.ClassInjection.class) != null) {
            return scopedResourcesInstance.get().getClassScopedScriptingContainer().getProvider().getRuntime();
        } else {
            throw new IllegalArgumentException(
                "Don't know how to resolve Ruby instance with qualifiers " + Arrays.asList(annotations));
        }
    }
}
