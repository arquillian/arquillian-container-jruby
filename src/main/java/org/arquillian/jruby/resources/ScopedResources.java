package org.arquillian.jruby.resources;

import org.arquillian.jruby.util.AnnotationUtils;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

import java.lang.reflect.Method;

public class ScopedResources {

    private ScriptingContainer classScopedScriptingContainer;

    private ScriptingContainer testScopedScriptingContainer;

    private Method testMethod;

    private boolean scriptsExecutedOnTest;

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

    public void setScriptsExecutedOnTest(boolean scriptsExecutedOnTest) {
        this.scriptsExecutedOnTest = scriptsExecutedOnTest;
    }

    public boolean isScriptsExecutedOnTest() {
        return scriptsExecutedOnTest;
    }

    public void setTestMethod(Method testMethod) {
        this.testMethod = testMethod;
    }

    public Method getTestMethod() {
        return testMethod;
    }

    public boolean isTestMethodUsingParameterInjectedRubyResource() {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == Ruby.class || testMethod.getParameterTypes()[i] == ScriptingContainer.class) {
                if (AnnotationUtils.filterAnnotation(testMethod.getParameterAnnotations()[i], ArquillianResource.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
