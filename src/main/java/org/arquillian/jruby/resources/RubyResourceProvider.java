package org.arquillian.jruby.resources;

import org.arquillian.jruby.api.RubyResource;
import org.arquillian.jruby.api.RubyScript;
import org.arquillian.jruby.embedded.JRubyConfiguration;
import org.arquillian.jruby.embedded.JRubyTemporaryDir;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class RubyResourceProvider implements TestEnricher {

    @Inject
    @ApplicationScoped
    protected Instance<ScopedResources> scopedResourcesInstance;

    @Inject
    @ApplicationScoped
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    @Inject
    @ApplicationScoped
    private Instance<JRubyConfiguration> configuration;

    private static final Logger LOG = Logger.getLogger(RubyResourceProvider.class.getName());

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
        ScriptingContainer scriptingContainer = null;
        for(int i = 0; i < parameterTypes.length; i++) {

            RubyResource resource = getResourceAnnotation(method.getParameterAnnotations()[i]);
            if (resource != null) {
                try {
                    if (parameterTypes[i] == Ruby.class) {
                        scriptingContainer = getOrCreateTestMethodScopedScriptingContainer();
                        values[i] = scriptingContainer.getProvider().getRuntime();
                    } else if (parameterTypes[i] == ScriptingContainer.class) {
                        scriptingContainer = getOrCreateTestMethodScopedScriptingContainer();
                        values[i] = scriptingContainer;
                    } else {
                        throw new RuntimeException("Unsupported RubyResource field type " + parameterTypes[i]);
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (scriptingContainer != null) {
                try {
                    handleRubyScriptAnnotation(scriptingContainer, method.getDeclaringClass().getAnnotation(RubyScript.class));
                    handleRubyScriptAnnotation(scriptingContainer, method.getAnnotation(RubyScript.class));
                } catch (IOException e) {
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

        URL jrubyURL = findJruby();

        ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        List<URL> urls = new ArrayList<URL>();
        urls.add(temporaryDirInstance.get().getTempGemDir().toUri().toURL());
        urls.add(temporaryDirInstance.get().getTempArchiveDir().toUri().toURL());

        URLClassLoader urlClassLoader;
        if (configuration.get().isIsolatedClassloader()) {
            urls.add(jrubyURL);
            urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
            LOG.fine("Using isolated classloader with " + Arrays.asList(urlClassLoader.getURLs()));
        } else {
            urlClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
            LOG.fine("Using non-isolated classloader with " + Arrays.asList(urlClassLoader.getURLs()));
        }

        scriptingContainer.setClassLoader(urlClassLoader);

        List<String> loadPath = new ArrayList<>();
        loadPath.add(temporaryDirInstance.get().getTempGemDir().toAbsolutePath().toString());
        loadPath.add(temporaryDirInstance.get().getTempArchiveDir().toAbsolutePath().toString());

        scriptingContainer.setLoadPaths(loadPath);

        return scriptingContainer;
    }

    private URL findJruby() {
        CodeSource cs = Ruby.class.getProtectionDomain().getCodeSource();
        return cs.getLocation();
    }

    public void handleRubyScriptAnnotation(ScriptingContainer scriptingContainer, RubyScript rubyScriptAnnotation) throws IOException {
        if (rubyScriptAnnotation != null) {
            String[] scripts = rubyScriptAnnotation.value();
            if (scripts != null) {
                for (String script : scripts) {
                    applyScript(scriptingContainer, script);
                }
            }
        }
    }

    private void applyScript(ScriptingContainer scriptingContainer, String script) throws IOException {
        try (FileReader scriptReader = new FileReader(temporaryDirInstance.get().getTempArchiveDir().resolve(script).toAbsolutePath().toFile())) {
            scriptingContainer.runScriptlet(
                    scriptReader,
                    script);
        }
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
