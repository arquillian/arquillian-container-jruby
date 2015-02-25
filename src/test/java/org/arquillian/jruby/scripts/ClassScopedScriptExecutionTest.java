package org.arquillian.jruby.scripts;


import org.arquillian.jruby.api.RubyResource;
import org.arquillian.jruby.api.RubyScript;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ClassScopedScriptExecutionTest {

    @Deployment
    public static GenericArchive deploy() throws Exception {
        return ShrinkWrap.create(GenericArchive.class)
                .add(new StringAsset("@a=1\n"), "testscript1.rb")
                .add(new StringAsset("@a=@a+1\n"), "testscript2.rb");
    }

    // In contrast to TestScopedScriptExecutionTest this ScriptingContainer is
    // Test class scoped, so that the value of @a is not lost between
    // the two test methods
    @RubyResource
    private Ruby ruby;

    @Test
    @InSequence(1)
    @RubyScript("testscript1.rb")
    public void firstTest() {
        assertEquals(1L, JavaEmbedUtils.rubyToJava(ruby.evalScriptlet("@a")));
    }

    @Test
    @InSequence(2)
    @RubyScript("testscript2.rb")
    public void secondTest() {
        assertEquals(2L, JavaEmbedUtils.rubyToJava(ruby.evalScriptlet("@a")));
    }

    @Test
    @InSequence(3)
    public void shouldFindTestResourceWithNonIsolatedClassLoader() {
        assertThat(ruby.evalScriptlet("require 'test.rb'"), is((IRubyObject) RubyBoolean.newBoolean(ruby, true)));
    }
}
