package org.arquillian.jruby.embedded;

import org.arquillian.jruby.api.RubyResource;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jruby.Ruby;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class JRubyUncachedTest {

    @RubyResource
    private Ruby rubyInstance;

    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = "asciidoctor", managed = false)
    public static JavaArchive deploy() throws Exception {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsResource(Maven.configureResolver().withRemoteRepo("rubygems", "http://rubygems-proxy.torquebox.org/releases", "default")
                        .resolve("rubygems:asciidoctor:gem:1.5.2").withoutTransitivity().asSingleFile());
    }

    @Test
    public void shouldCleanGemDirAfterUndeploy() throws Exception {
        File gemFile;

        deployer.deploy("asciidoctor");
        try {
            assertThat(
                    (Boolean) rubyInstance.evalScriptlet("require 'asciidoctor'").toJava(Boolean.class),
                    is(Boolean.TRUE));

            URL url = rubyInstance.getJRubyClassLoader().getResource("asciidoctor-1.5.2.gem");
            assertThat("Gem not available via classloader!", url, notNullValue());
            gemFile = new File(url.toURI());
        } finally {
            deployer.undeploy("asciidoctor");
        }
        System.out.println(gemFile);
        assertThat("Gem not deleted after undeployment", gemFile.exists(), is(false));
    }

    @Test
    public void shouldInjectRubyInstance() throws Exception {

        assertThat("Ruby instance not injected!", rubyInstance, notNullValue());

    }

}
