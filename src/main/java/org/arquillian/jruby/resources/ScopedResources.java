package org.arquillian.jruby.resources;

import org.jruby.embed.ScriptingContainer;

public class ScopedResources {

    private ScriptingContainer classScopedScriptingContainer;

    private boolean classScopedScriptingContainerRequested;

    private ScriptingContainer testScopedScriptingContainer;

    private boolean testScopedScriptingContainerRequested;

    public void setClassScopedScriptingContainer(ScriptingContainer classScopedScriptingContainer) {
        this.classScopedScriptingContainer = classScopedScriptingContainer;
    }

    public ScriptingContainer getClassScopedScriptingContainer() {
        return classScopedScriptingContainer;
    }

    public void setTestScopedScriptingContainer(ScriptingContainer testScopedScriptingContainer) {
        this.testScopedScriptingContainer = testScopedScriptingContainer;
    }

    public ScriptingContainer getTestScopedScriptingContainer() {
        return testScopedScriptingContainer;
    }

    public void setTestScopedScriptingContainerRequested(boolean testScopedScriptingContainerRequested) {
        this.testScopedScriptingContainerRequested = testScopedScriptingContainerRequested;
    }

    public boolean isTestScopedScriptingContainerRequested() {
        return testScopedScriptingContainerRequested;
    }

    public void setClassScopedScriptingContainerRequested(boolean classScopedScriptingContainerRequested) {
        this.classScopedScriptingContainerRequested = classScopedScriptingContainerRequested;
    }

    public boolean isClassScopedScriptingContainerRequested() {
        return classScopedScriptingContainerRequested;
    }
}
