/*
 * Copyright 2015 Robert Panzer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.jruby.embedded;

import org.arquillian.jruby.gems.CachingGemInstaller;
import org.arquillian.jruby.gems.GemInstaller;
import org.arquillian.jruby.gems.UncachedGemInstaller;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

public class JRubyDeployableContainer implements DeployableContainer {

    private static final Logger LOG = Logger.getLogger(JRubyDeployableContainer.class.getName());

    private JRubyConfiguration containerConfig;

    private GemInstaller installer;

    //static Path tempDir;

    @Inject
    @SuiteScoped
    private InstanceProducer<ScriptingContainer> scriptingContainerInstanceProducer;

    @Inject
    @SuiteScoped
    private InstanceProducer<Ruby> rubyInstanceProducer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<JRubyTemporaryDir> temporaryDirInstanceProducer;

    @Override
    public Class<JRubyConfiguration> getConfigurationClass() {
        return JRubyConfiguration.class;
    }

    @Override
    public void setup(ContainerConfiguration configuration) {
        this.containerConfig = (JRubyConfiguration) configuration;
    }

    @Override
    public void start() throws LifecycleException {
        ScriptingContainer scriptingContainer = new ScriptingContainer();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")), "arquillianJRubyCache");
            temporaryDirInstanceProducer.set(new JRubyTemporaryDir(tempDir));
            System.out.println(tempDir.toAbsolutePath().toString());
            //scriptingContainer.setLoadPaths(Collections.singletonList(tempDir.toAbsolutePath().toString()));
            //scriptingContainer.setClassLoader(new URLClassLoader(new URL[]{tempDir.toUri().toURL()}));
        } catch (IOException e) {
            throw new LifecycleException("Could not create temporary directory!", e);
        }
        /*
        scriptingContainerInstanceProducer.set(scriptingContainer);
        rubyInstanceProducer.set(scriptingContainer.getProvider().getRuntime());
        */
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
    public void undeploy(Archive archive) throws DeploymentException {

        try {
            installer.deleteInstallationDir();
        } catch (IOException e) {
            throw new DeploymentException("Could not delete temporary directory!", e);
        }
        installer = null;
    }

    @Override
    public ProtocolMetaData deploy(Archive archive) throws DeploymentException {
        if (!Validate.isArchiveOfType(JavaArchive.class, archive)) {
            throw new IllegalArgumentException("Only jars supported by JRuby container.");
        }

        if (installer != null) {
            throw new IllegalStateException("Only one deployment at a time supported.");
        }
        Path tempDir = temporaryDirInstanceProducer.get().getTempDir();

        installer = containerConfig.getGemDir() != null ?
                new CachingGemInstaller(Paths.get(containerConfig.getGemDir()), tempDir) :
                new UncachedGemInstaller(tempDir);

        long start = System.currentTimeMillis();
        installer.installGemsFromArchive(archive);
        LOG.fine("Installation took " + (System.currentTimeMillis() - start));

        return new ProtocolMetaData();
    }

}
