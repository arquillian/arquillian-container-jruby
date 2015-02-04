package foo.test;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.RequestScoped;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class BasicTest {

    @ArquillianResource
    private Ruby rubyInstance;

    @ArquillianResource
    private ScriptingContainer scriptingContainer;

    @Deployment
    public static GenericArchive deploy() throws Exception {
        File asciidoctorGem = Maven.configureResolver()
                .withRemoteRepo("rubygems", "http://rubygems-proxy.torquebox.org/releases", "default")
                .resolve("rubygems:asciidoctor:gem:1.5.2")
                .withTransitivity().asSingleFile();

        return ShrinkWrap.create(GenericArchive.class)
                .add(new FileAsset(asciidoctorGem), asciidoctorGem.getName());
    }

    @Test
    public void shouldRenderAsciidocDocument() throws Exception {
        IRubyObject result = rubyInstance.evalScriptlet(
                "require 'asciidoctor'\n" +
                "Asciidoctor.convert '*This* is Asciidoctor.'");
        assertThat(
                (String)JavaEmbedUtils.rubyToJava(rubyInstance, result, String.class),
                containsString("<strong>This</strong> is Asciidoctor."));
    }
}
