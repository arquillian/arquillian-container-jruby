package org.arquillian.jruby.embedded;

import java.io.FileReader;
import java.io.IOException;

import org.arquillian.jruby.api.RubyScript;
import org.arquillian.jruby.resources.ScopedResources;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jruby.embed.ScriptingContainer;

public class JRubyTestObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<ScopedResources> scopedResourcesInstanceProducer;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    public void beforeClass(@Observes BeforeClass beforeClass) throws IOException {
        scopedResourcesInstanceProducer.set(new ScopedResources());
    }

    // Precedence is -10 so that we are invoked after ResourceProviders are called
    // Apply scripts on the requested scripting containers
    public void beforeTestInvokeScript(@Observes(precedence = -10) Before beforeEvent) throws IOException {
        ScriptingContainer scriptingContainer = scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer() != null
                ? scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer()
                : scopedResourcesInstanceProducer.get().getClassScopedScriptingContainer();

        if (scriptingContainer != null) {
            handleRubyScriptAnnotation(scriptingContainer, beforeEvent.getTestClass().getAnnotation(RubyScript.class));
            handleRubyScriptAnnotation(scriptingContainer, beforeEvent.getTestMethod().getAnnotation(RubyScript.class));
        }
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

    public void afterTestClearJRubyInstance(@Observes After afterEvent) {
        scopedResourcesInstanceProducer.get().setTestScopedScriptingContainer(null);
    }

}
