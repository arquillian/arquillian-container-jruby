package org.arquillian.jruby.embedded;

import org.arquillian.jruby.api.RubyScript;
import org.arquillian.jruby.resources.RubyResourceProvider;
import org.arquillian.jruby.resources.ScopedResources;
import org.arquillian.jruby.util.AnnotationUtils;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.FileReader;
import java.io.IOException;
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

public class JRubyTestObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<ScopedResources> scopedResourcesInstanceProducer;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    @Inject
    @ApplicationScoped
    protected Instance<JRubyConfiguration> configuration;

    private static final Logger LOG = Logger.getLogger(RubyResourceProvider.class.getName());

    public void createScopedResources(@Observes(precedence = 1000) BeforeClass beforeClass) {
        scopedResourcesInstanceProducer.set(new ScopedResources());
    }

    public void checkCreateClassBasedScriptingContainer(@Observes(precedence = 100) BeforeClass beforeClass) {

        if (isTestClassRequiringScriptingContainer(beforeClass.getTestClass().getJavaClass())) {
            scopedResourcesInstanceProducer.get().setCreateTestClassBasedScriptingContainer();
        }
    }

    public void beforeClass(@Observes BeforeClass beforeClass) throws IOException {

        if (scopedResourcesInstanceProducer.get().isCreateTestClassBasedScriptingContainer()) {
            scopedResourcesInstanceProducer.get().setClassScopedScriptingContainer(createScriptingContainer());
        }
    }

    public void checkCreateTestMethodBasedScriptingContainer(@Observes(precedence = 100) Before before) {

        if (isTestMethodUsingParameterInjectedRubyResource(before.getTestMethod())) {
            scopedResourcesInstanceProducer.get().setCreateTestMethodBasedScriptingContainer();
        }
    }

    // Precedence is 10 so that we are invoked before the ResourceProvider that enriches the test class
    // is called.
    // Apply scripts on the requested scripting containers
    public void beforeTestMethodCreateScriptingContainer(@Observes(precedence = 10) Before before) throws IOException {

        if (scopedResourcesInstanceProducer.get().isCreateTestMethodBasedScriptingContainer()) {
            scopedResourcesInstanceProducer.get().setTestScopedScriptingContainer(createScriptingContainer());
        }
    }

    // Event is either thrown by JRubyTestObserver#beforeTestMethod if scripts should be executed on
    // class scoped Ruby instance or by ResourceProvider#lookup if scripts should be executed
    // on test method scoped Ruby instance.
    // Precedence stays at default so that it is called after beforeTestMethodCreateScriptingContainer
    public void handleRubyScripts(@Observes Before before) throws IOException {
        ScriptingContainer scriptingContainer = scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer();

        if (scriptingContainer == null) {
            scriptingContainer = scopedResourcesInstanceProducer.get().getClassScopedScriptingContainer();
        }

        if (scriptingContainer != null) {
            handleRubyScriptAnnotation(scriptingContainer, before.getTestClass().getAnnotation(RubyScript.class));
            handleRubyScriptAnnotation(scriptingContainer, before.getTestMethod().getAnnotation(RubyScript.class));
        }
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

    private boolean isTestClassRequiringScriptingContainer(Class<?> testClass) {
        for (Field f : SecurityActions.getFieldsWithAnnotation(testClass, ArquillianResource.class)) {
            if (f.getType() == Ruby.class || f.getType() == ScriptingContainer.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isTestMethodUsingParameterInjectedRubyResource(Method testMethod) {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == Ruby.class
                || testMethod.getParameterTypes()[i] == ScriptingContainer.class) {
                if (AnnotationUtils.filterAnnotation(testMethod.getParameterAnnotations()[i], ArquillianResource.class)
                    != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void handleRubyScriptAnnotation(ScriptingContainer scriptingContainer, RubyScript rubyScriptAnnotation)
        throws IOException {
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
        try (FileReader scriptReader = new FileReader(
            temporaryDirInstance.get().getTempArchiveDir().resolve(script).toAbsolutePath().toFile())) {
            scriptingContainer.runScriptlet(
                scriptReader,
                script);
        }
    }

    public void afterTestClearJRubyInstance(@Observes After afterEvent) {
        ScriptingContainer testScopedScriptingContainer =
            scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer();
        if (testScopedScriptingContainer != null) {
            testScopedScriptingContainer.terminate();
        }
        scopedResourcesInstanceProducer.get().setTestScopedScriptingContainer(null);
    }

    public void afterTestClassCleanJRubyInstance(@Observes AfterClass afterClassEvent) {
        ScriptingContainer classScopedScriptingContainer =
            scopedResourcesInstanceProducer.get().getClassScopedScriptingContainer();
        if (classScopedScriptingContainer != null) {
            classScopedScriptingContainer.terminate();
        }
        scopedResourcesInstanceProducer.get().setClassScopedScriptingContainer(null);
    }
}
