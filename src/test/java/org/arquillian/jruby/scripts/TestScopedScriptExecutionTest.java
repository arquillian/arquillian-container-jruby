package org.arquillian.jruby.scripts;

import org.arquillian.jruby.api.RubyScript;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jruby.embed.ScriptingContainer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class TestScopedScriptExecutionTest {

    @Deployment
    public static GenericArchive deploy() throws Exception {
        return ShrinkWrap.create(GenericArchive.class)
            .add(new StringAsset("@a = @a ? @a + 1 : 1\n"), "testscript.rb");
    }

    @Test
    @InSequence(1)
    @RubyScript("testscript.rb")
    public void firstTest(@ArquillianResource ScriptingContainer scriptingContainer) {
        assertEquals(1L, scriptingContainer.runScriptlet("@a"));
    }

    @Test
    @InSequence(1)
    @RubyScript("testscript.rb")
    public void secondTest(@ArquillianResource ScriptingContainer scriptingContainer) {
        assertEquals(1L, scriptingContainer.runScriptlet("@a"));
    }
}
