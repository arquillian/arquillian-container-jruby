package org.arquillian.jruby.classloaderisolation;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JRubyIsolatedClassloaderTest {

    @ArquillianResource
    private ScriptingContainer scriptingContainer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotFindTestResourceWithIsolatedClassloader() {
        thrown.expect(EvalFailedException.class);
        thrown.expectMessage("LoadError");
        scriptingContainer.runScriptlet("require 'test.rb'");
    }
}
