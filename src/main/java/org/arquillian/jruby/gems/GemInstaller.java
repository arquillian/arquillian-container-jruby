package org.arquillian.jruby.gems;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;

import java.io.IOException;

public interface GemInstaller {
    void installGemsFromArchive(Archive archive) throws DeploymentException;

    void deleteInstallationDir() throws IOException;
}
