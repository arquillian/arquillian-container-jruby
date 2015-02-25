package org.arquillian.jruby.classloaderisolation;

import org.arquillian.jruby.api.RubyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jruby.Ruby;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Arquillian.class)
public class JRubyIsolatedClassloaderTest {

    @RubyResource
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
