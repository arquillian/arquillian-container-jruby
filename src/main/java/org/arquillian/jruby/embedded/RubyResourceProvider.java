package org.arquillian.jruby.embedded;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class RubyResourceProvider implements ResourceProvider {


    @Inject
    private Instance<Ruby> rubyInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.equals(Ruby.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        System.out.println(" Test: " + rubyInstance.get());
        System.out.println("LOOKUP " + Arrays.asList(qualifiers) + " " + resource);
        return rubyInstance.get();
    }
}
