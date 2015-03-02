package org.arquillian.jruby.resources;

import org.arquillian.jruby.api.RubyScript;
import org.arquillian.jruby.embedded.JRubyConfiguration;
import org.arquillian.jruby.embedded.JRubyScriptExecution;
import org.arquillian.jruby.embedded.JRubyTemporaryDir;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractResourceProvider implements ResourceProvider {

    @Inject
    @ApplicationScoped
    protected Instance<ScopedResources> scopedResourcesInstance;

    @Inject
    @ApplicationScoped
    protected Instance<JRubyTemporaryDir> temporaryDirInstance;

    @Inject
    @ApplicationScoped
    protected Instance<JRubyConfiguration> configuration;

    @Inject
    protected Event<JRubyScriptExecution> rubyScriptExecutionEvent;

    private static final Logger LOG = Logger.getLogger(RubyResourceProvider.class.getName());


    protected ScriptingContainer getOrCreateTestMethodScopedScriptingContainer() throws MalformedURLException {
        ScriptingContainer scriptingContainer = scopedResourcesInstance.get().getTestScopedScriptingContainer();
        if (scriptingContainer == null) {
            scriptingContainer = createScriptingContainer();
            scopedResourcesInstance.get().setTestScopedScriptingContainer(scriptingContainer);
        }
        return scriptingContainer;
    }

    protected ScriptingContainer getTestMethodScopedScriptingContainer() throws MalformedURLException {
        return scopedResourcesInstance.get().getTestScopedScriptingContainer();
    }

    protected ScriptingContainer getOrCreateClassScopedScriptingContainer() throws MalformedURLException {
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


}
