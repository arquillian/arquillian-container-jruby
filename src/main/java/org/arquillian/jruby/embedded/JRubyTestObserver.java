package org.arquillian.jruby.embedded;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jruby.Ruby;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class JRubyTestObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<ScriptingContainer> scriptingContainerInstanceProducer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<Ruby> rubyInstanceProducer;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;
    
    public void beforeTest(@Observes Before beforeEvent) throws IOException {
        System.out.println("BEFORETEST");
        ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        Path tempDir = temporaryDirInstance.get().getTempDir();

        if (scriptingContainerInstanceProducer.get() == null) {

            scriptingContainer.setClassLoader(new URLClassLoader(new URL[]{tempDir.toUri().toURL()}));

            scriptingContainerInstanceProducer.set(scriptingContainer);
            System.out.println("Observer instance: "+ scriptingContainer.getProvider().getRuntime());
            rubyInstanceProducer.set(scriptingContainer.getProvider().getRuntime());
        }

    }

}
