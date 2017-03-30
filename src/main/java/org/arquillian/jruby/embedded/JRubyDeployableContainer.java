package org.arquillian.jruby.embedded;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.arquillian.jruby.gems.CachingGemInstaller;
import org.arquillian.jruby.gems.GemInstaller;
import org.arquillian.jruby.gems.UncachedGemInstaller;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class JRubyDeployableContainer implements DeployableContainer<JRubyConfiguration> {

    private static final Logger LOG = Logger.getLogger(JRubyDeployableContainer.class.getName());

    private JRubyConfiguration containerConfig;

    private GemInstaller installer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<JRubyTemporaryDir> temporaryDirInstanceProducer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<JRubyConfiguration> containerConfiguration;

    @Override
    public Class<JRubyConfiguration> getConfigurationClass() {
        return JRubyConfiguration.class;
    }

    @Override
    public void setup(JRubyConfiguration configuration) {
        this.containerConfig = configuration;
        containerConfiguration.set(configuration);
    }

    @Override
    public void start() throws LifecycleException {
        try {
            Path tempGemDir =
                Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")),
                    "arquillianJRubyGemDir");
            Path tempArchiveDir =
                Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")),
                    "arquillianJRubyArchiveDir");
            temporaryDirInstanceProducer.set(new JRubyTemporaryDir(tempGemDir, tempArchiveDir));
            LOG.fine("Unpacking archive in " + tempArchiveDir);
            LOG.fine("Installing gems in " + tempGemDir);
        } catch (IOException e) {
            throw new LifecycleException("Could not create temporary directory!", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Descriptors not supported by JRuby");
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Descriptors not supported by JRuby");
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {

        try {
            installer.deleteInstallationDirs();
        } catch (IOException e) {
            throw new DeploymentException("Could not delete temporary directory!", e);
        }
        installer = null;
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        if (installer != null) {
            throw new IllegalStateException("Only one deployment at a time supported.");
        }
        Path tempGemDir = temporaryDirInstanceProducer.get().getTempGemDir();
        Path tempArchiveDir = temporaryDirInstanceProducer.get().getTempArchiveDir();
        installer = containerConfig.getGemDir() != null ?
            new CachingGemInstaller(Paths.get(containerConfig.getGemDir()), tempGemDir, tempArchiveDir) :
            new UncachedGemInstaller(tempGemDir, tempArchiveDir);

        long start = System.currentTimeMillis();
        installer.installGemsFromArchive(archive);
        LOG.fine("Installation took " + (System.currentTimeMillis() - start));

        return new ProtocolMetaData();
    }
}
