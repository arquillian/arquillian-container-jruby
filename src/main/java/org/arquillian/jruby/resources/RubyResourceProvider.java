package org.arquillian.jruby.resources;

import org.arquillian.jruby.api.RubyResource;
import org.arquillian.jruby.embedded.JRubyTemporaryDir;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class RubyResourceProvider implements TestEnricher {

    @Inject
    protected Instance<ScopedResources> scopedResourcesInstance;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    @Override
    public void enrich(Object testCase) {

        for(Field field : SecurityActions.getFieldsWithAnnotation(testCase.getClass(), RubyResource.class)) {

            Object value = null;
            try {
                Annotation[] qualifiers = filterAnnotations(field.getAnnotations());
                // null value will throw exception in lookup
                if (field.getType() == Ruby.class) {
                    value = getOrCreateClassScopedScriptingContainer().getProvider().getRuntime();
                } else if (field.getType() == ScriptingContainer.class) {
                    value = getOrCreateClassScopedScriptingContainer();
                } else {
                    throw new RuntimeException("Unsupported RubyResource field type " + field.getType());
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not lookup value for field " + field, e);
            }
            try {
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }
                field.set(testCase, value);
            } catch (Exception e) {
                throw new RuntimeException("Could not set value on field " + field + " using " + value);
            }
        }
    }

    @Override
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++) {

            RubyResource resource = getResourceAnnotation(method.getParameterAnnotations()[i]);
            if (resource != null) {
                try {
                    if (parameterTypes[i] == Ruby.class) {
                        values[i] = getOrCreateTestMethodScopedScriptingContainer().getProvider().getRuntime();
                    } else if (parameterTypes[i] == ScriptingContainer.class) {
                        values[i] = getOrCreateTestMethodScopedScriptingContainer();
                    } else {
                        throw new RuntimeException("Unsupported RubyResource field type " + parameterTypes[i]);
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return values;
    }

    public ScriptingContainer getOrCreateTestMethodScopedScriptingContainer() throws MalformedURLException {
        ScriptingContainer scriptingContainer = scopedResourcesInstance.get().getTestScopedScriptingContainer();
        if (scriptingContainer == null) {
            scriptingContainer = createScriptingContainer();
            scopedResourcesInstance.get().setTestScopedScriptingContainer(scriptingContainer);
        }
        return scriptingContainer;
    }

    public ScriptingContainer getOrCreateClassScopedScriptingContainer() throws MalformedURLException {
        ScriptingContainer scriptingContainer = scopedResourcesInstance.get().getClassScopedScriptingContainer();
        if (scriptingContainer == null) {
            scriptingContainer = createScriptingContainer();
            scopedResourcesInstance.get().setClassScopedScriptingContainer(scriptingContainer);
        }
        return scriptingContainer;
    }

    private ScriptingContainer createScriptingContainer() throws MalformedURLException {
        ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        scriptingContainer.setClassLoader(
                new URLClassLoader(
                        new URL[]{
                                temporaryDirInstance.get().getTempGemDir().toUri().toURL(),
                                temporaryDirInstance.get().getTempArchiveDir().toUri().toURL()}));

        return scriptingContainer;
    }

    protected RubyResource getResourceAnnotation(Annotation[] annotations) {
        for(Annotation annotation : annotations) {
            if(annotation.annotationType() == RubyResource.class) {
                return (RubyResource)annotation;
            }
        }
        return null;
    }

    protected Annotation[] filterAnnotations(Annotation[] annotations) {
        if(annotations == null) {
            return new Annotation[0];
        }
        List<Annotation> filtered = new ArrayList<Annotation>();
        for(Annotation annotation : annotations) {
            if(annotation.annotationType() != RubyResource.class) {
                filtered.add(annotation);
            }
        }
        return filtered.toArray(new Annotation[0]);
    }

}
