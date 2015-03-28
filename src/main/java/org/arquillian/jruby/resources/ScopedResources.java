package org.arquillian.jruby.resources;

import org.jruby.embed.ScriptingContainer;

public class ScopedResources {

    private ScriptingContainer classScopedScriptingContainer;

    private ScriptingContainer testScopedScriptingContainer;

    private boolean createTestClassBasedScriptingContainer;
    private boolean createTestMethodBasedScriptingContainer;

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

    public void setCreateTestClassBasedScriptingContainer() {
        this.createTestClassBasedScriptingContainer = true;
    }

    public boolean isCreateTestClassBasedScriptingContainer() {
        return createTestClassBasedScriptingContainer;
    }

    public void setCreateTestMethodBasedScriptingContainer() {
        this.createTestMethodBasedScriptingContainer = true;
    }

    public boolean isCreateTestMethodBasedScriptingContainer() {
        return createTestMethodBasedScriptingContainer;
    }
}
