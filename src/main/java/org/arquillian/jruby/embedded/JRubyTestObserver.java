package org.arquillian.jruby.embedded;

import org.arquillian.jruby.api.RubyScript;
import org.arquillian.jruby.resources.ScopedResources;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jruby.embed.ScriptingContainer;

import java.io.FileReader;
import java.io.IOException;

public class JRubyTestObserver {

    @Inject
    @ApplicationScoped
    private InstanceProducer<ScopedResources> scopedResourcesInstanceProducer;

    @Inject
    private Instance<JRubyTemporaryDir> temporaryDirInstance;

    @Inject
    private Event<JRubyScriptExecution> rubyScriptExecutionEvent;

    public void beforeClass(@Observes BeforeClass beforeClass) throws IOException {
        scopedResourcesInstanceProducer.set(new ScopedResources());
    }

    // Precedence is -10 so that we are invoked after the ResourceProvider that enriches the test class
    // is called.
    // Apply scripts on the requested scripting containers
    public void beforeTestMethod(@Observes(precedence = -10) Before before) throws IOException {
        scopedResourcesInstanceProducer.get().setTestMethod(before.getTestMethod());

        if (!scopedResourcesInstanceProducer.get().isTestMethodUsingParameterInjectedRubyResource()) {
            rubyScriptExecutionEvent.fire(new JRubyScriptExecution());
        }
    }

    // Event is either thrown by JRubyTestObserver#beforeTestMethod if scripts should be executed on
    // class scoped Ruby instance or by ResourceProvider#lookup if scripts should be executed
    // on test method scoped Ruby instance.
    public void executeScript(@Observes JRubyScriptExecution scriptExecution) throws IOException {
        if (!scopedResourcesInstanceProducer.get().isScriptsExecutedOnTest()) {
            scopedResourcesInstanceProducer.get().setScriptsExecutedOnTest(true);
            ScriptingContainer scriptingContainer = scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer();

            if (scriptingContainer == null) {
                scriptingContainer = scopedResourcesInstanceProducer.get().getClassScopedScriptingContainer();
            }

            if (scriptingContainer != null) {
                handleRubyScriptAnnotation(scriptingContainer, scopedResourcesInstanceProducer.get().getTestMethod().getAnnotation(RubyScript.class));
                handleRubyScriptAnnotation(scriptingContainer, scopedResourcesInstanceProducer.get().getTestMethod().getDeclaringClass().getAnnotation(RubyScript.class));
            }
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
        ScriptingContainer testScopedScriptingContainer = scopedResourcesInstanceProducer.get().getTestScopedScriptingContainer();
        if (testScopedScriptingContainer != null) {
            testScopedScriptingContainer.terminate();
        }
        scopedResourcesInstanceProducer.get().setTestScopedScriptingContainer(null);
        scopedResourcesInstanceProducer.get().setScriptsExecutedOnTest(false);
    }

    public void afterTestClassCleanJRubyInstance(@Observes AfterClass afterClassEvent) {
        ScriptingContainer classScopedScriptingContainer = scopedResourcesInstanceProducer.get().getClassScopedScriptingContainer();
        if (classScopedScriptingContainer != null) {
            classScopedScriptingContainer.terminate();
        }
        scopedResourcesInstanceProducer.get().setClassScopedScriptingContainer(null);
    }


}
