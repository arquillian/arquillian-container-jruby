package foo.test;

import org.arquillian.jruby.api.RubyScript;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class BasicTest {

    @Deployment
    public static GenericArchive deploy() throws Exception {
        File asciidoctorGem = Maven.configureResolver()
                .withRemoteRepo("rubygems", "http://rubygems-proxy.torquebox.org/releases", "default")
                .resolve("rubygems:asciidoctor:gem:1.5.2")
                .withoutTransitivity().asSingleFile();

        return ShrinkWrap.create(GenericArchive.class)
                .add(new FileAsset(asciidoctorGem), asciidoctorGem.getName())
                .add(new StringAsset("require 'asciidoctor'\n"), "requireasciidoctor.rb");
    }

    private static Ruby lastRubyInstance;

    @Test
    public void shouldExecuteSimpleRubyStatement(@ArquillianResource Ruby rubyInstance) throws Exception {

        assertNotSame(lastRubyInstance, rubyInstance);
        lastRubyInstance = rubyInstance;

        IRubyObject result = rubyInstance.evalScriptlet(
                "java.lang.System.getProperties.map {|k,v| %(#{k}=#{v.inspect}) } * '\\n'");
        assertThat(
                (String)JavaEmbedUtils.rubyToJava(rubyInstance, result, String.class),
                containsString("java.io.tmpdir="));
    }

    @Test
    @RubyScript("requireasciidoctor.rb")
    public void shouldApplyScriptBeforeTest(@ArquillianResource Ruby rubyInstance) throws Exception {
        assertNotSame(lastRubyInstance, rubyInstance);
        lastRubyInstance = rubyInstance;

        IRubyObject result = rubyInstance.evalScriptlet(
                "Asciidoctor.convert '*This* is Asciidoctor.'");
        assertThat(
                (String)JavaEmbedUtils.rubyToJava(rubyInstance, result, String.class),
                containsString("<strong>This</strong> is Asciidoctor."));

    }
}
