package org.arquillian.jruby.embedded;

import org.arquillian.jruby.resources.ScopedResources;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class JRubyTestObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<ScopedResources> scopedResourcesInstanceProducer;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    public void beforeClass(@Observes BeforeClass beforeClass) throws IOException {
        ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        scriptingContainer.setClassLoader(
                new URLClassLoader(
                        new URL[]{
                                temporaryDirInstance.get().getTempGemDir().toUri().toURL(),
                                temporaryDirInstance.get().getTempArchiveDir().toUri().toURL()}));

        ScopedResources scopedResources = new ScopedResources();
        scopedResources.setClassScopedScriptingContainer(scriptingContainer);
        scopedResourcesInstanceProducer.set(scopedResources);
        System.out.println("Observer instance: "+ scriptingContainer.getProvider().getRuntime());
    }

    // Precedence is 10 so that we are invoked before ResourceProviders are called
    public void beforeTest(@Observes(precedence = 10) Before beforeEvent) throws IOException {
        ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        Path tempDir = temporaryDirInstance.get().getTempGemDir();

        scriptingContainer.setClassLoader(
                new URLClassLoader(
                        new URL[]{
                                temporaryDirInstance.get().getTempGemDir().toUri().toURL(),
                                temporaryDirInstance.get().getTempArchiveDir().toUri().toURL()}));

        scopedResourcesInstanceProducer.get().setTestScopedScriptingContainer(scriptingContainer);
    }

}
