package org.arquillian.jruby.resources;

import org.arquillian.jruby.embedded.JRubyScriptExecution;
import org.arquillian.jruby.util.AnnotationUtils;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.util.Arrays;

public class ScriptingContainerResourceProvider extends AbstractResourceProvider {

    @Override
    public boolean canProvide(Class<?> aClass) {
        return aClass == ScriptingContainer.class;
    }

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        ScriptingContainer scriptingContainer;

        try {
            if (AnnotationUtils.filterAnnotation(annotations, MethodInjection.class) != null) {
                scriptingContainer = getOrCreateTestMethodScopedScriptingContainer();
                rubyScriptExecutionEvent.fire(new JRubyScriptExecution());
            } else if (AnnotationUtils.filterAnnotation(annotations, ClassInjection.class) != null) {
                scriptingContainer = getOrCreateClassScopedScriptingContainer();
            } else {
                throw new IllegalArgumentException("Don't know how to resolve Ruby instance with qualifiers " + Arrays.asList(annotations));
            }
            return scriptingContainer;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
