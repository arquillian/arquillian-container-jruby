package org.arquillian.jruby.scripts;


import org.arquillian.jruby.api.RubyScript;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Arquillian.class)
public class TestScopedScriptExecutionTest {

    @Deployment
    public static GenericArchive deploy() throws Exception {
        return ShrinkWrap.create(GenericArchive.class)
                .add(new StringAsset("@a = @a ? @a + 1 : 1\n"), "testscript.rb");
    }

    // In contrast to TestScopedScriptExecutionTest this ScriptingContainer is
    // Test method scoped, so that the value of @a is lost between
    // the two test methods
    @ArquillianResource
    private ScriptingContainer scriptingContainer;

    @Test
    @InSequence(1)
    @RubyScript("testscript.rb")
    public void firstTest() {
        assertEquals(1L, scriptingContainer.runScriptlet("@a"));
    }

    @Test
    @InSequence(1)
    @RubyScript("testscript.rb")
    public void secondTest() {
        assertEquals(1L, scriptingContainer.runScriptlet("@a"));
    }

}
