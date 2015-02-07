package org.arquillian.jruby.resources;

import org.arquillian.jruby.embedded.JRubyTemporaryDir;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public abstract class AbstractJRubyResourceProvider {

    @Inject
    protected Instance<ScopedResources> scopedResourcesInstance;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

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

}
