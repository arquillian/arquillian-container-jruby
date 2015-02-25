package org.arquillian.jruby.embedded;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

public class JRubyConfiguration implements ContainerConfiguration {

    private String gemDir;

    private boolean isolatedClassloader = true;

    @Override
    public void validate() throws ConfigurationException {
    }

    public String getGemDir() {
        return gemDir;
    }

    public void setGemDir(String gemDir) {
        this.gemDir = gemDir;
    }

    public boolean isIsolatedClassloader() {
        return isolatedClassloader;
    }

    public void setIsolatedClassloader(boolean isolatedClassloader) {
        this.isolatedClassloader = isolatedClassloader;
    }
}
