package org.arquillian.jruby.embedded;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.io.File;

public class JRubyConfiguration implements ContainerConfiguration {

    private String gemDir;

    @Override
    public void validate() throws ConfigurationException {
    }

    public String getGemDir() {
        return gemDir;
    }

    public void setGemDir(String gemDir) {
        this.gemDir = gemDir;
    }
}
