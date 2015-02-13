package org.arquillian.jruby.scripts;


import static org.junit.Assert.assertEquals;

import org.arquillian.jruby.api.RubyScript;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassScopedScriptExecutionTest {

    @Deployment
    public static GenericArchive deploy() throws Exception {
        return ShrinkWrap.create(GenericArchive.class)
                .add(new StringAsset("puts \">>>#{@a}\"\n@a = @a ? @a + 1 : 1\n"), "testscript.rb");
    }

    // In contrast to TestScopedScriptExecutionTest this ScriptingContainer is
    // Test class scoped, so that the value of @a is not lost between
    // the two test methods
    @ArquillianResource
    @ApplicationScoped
    private Ruby ruby;

    @Test
    @InSequence(1)
    @RubyScript("testscript.rb")
    public void firstTest() {
        assertEquals(1L, JavaEmbedUtils.rubyToJava(ruby.evalScriptlet("@a")));
    }

    @Test
    @InSequence(2)
    @RubyScript("testscript.rb")
    public void secondTest() {
        assertEquals(2L, JavaEmbedUtils.rubyToJava(ruby.evalScriptlet("@a")));
    }

}
