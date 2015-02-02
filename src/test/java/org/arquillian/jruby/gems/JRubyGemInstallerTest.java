package org.arquillian.jruby.gems;

import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class JRubyGemInstallerTest {

    private static File asciidoctorGem;

    @BeforeClass
    public static void resolveAsciidoctorGem() throws Exception {
        asciidoctorGem = Maven.configureResolver().withRemoteRepo("rubygems", "http://rubygems-proxy.torquebox.org/releases", "default")
                .resolve("rubygems:asciidoctor:gem:1.5.2").withTransitivity().asFile()[0];
    }

    @Test
    public void shouldInstallGem() throws Exception {

        // Given
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addAsResource(asciidoctorGem);

        Path targetDir = Paths.get("build", "shouldInstallGem");

        // When
        new UncachedGemInstaller(targetDir).installGemsFromArchive(jar);

        // Then
        assertTrue(
                "Gem not unpacked from archive!",
                targetDir.resolve("asciidoctor-1.5.2.gem").toFile().exists());
        assertTrue(
                "Gem not installed",
                targetDir.resolve("gems")
                        .resolve("asciidoctor-1.5.2")
                        .resolve("lib")
                        .resolve("asciidoctor.rb").toFile().exists());
    }

    @Test
    public void shouldDeleteGemTargetDir() throws Exception {

        // Given
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addAsResource(asciidoctorGem);

        Path targetDir = Paths.get("build", "shouldDeleteGemTargetDir");

        File testFile = new File(targetDir.toFile(), "testFile");
        FileUtils.writeStringToFile(testFile, "Hello World");
        assertTrue(testFile.exists());

        // When
        new UncachedGemInstaller(targetDir).deleteInstallationDir();

        // Then
        assertFalse(testFile.exists());
        assertFalse(targetDir.toFile().exists());
    }


}
