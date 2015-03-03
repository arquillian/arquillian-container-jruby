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
                .resolve("rubygems:asciidoctor:gem:1.5.2").withoutTransitivity().asSingleFile();
    }

    @Test
    public void shouldInstallGem() throws Exception {

        // Given
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addAsResource(asciidoctorGem);

        Path targetGemDir = Paths.get("build", "shouldInstallGemGemDir");
        Path targetArchiveDir = Paths.get("build", "shouldInstallGemArchiveDir");

        // When
        new UncachedGemInstaller(targetGemDir, targetArchiveDir).installGemsFromArchive(jar);

        // Then
        assertTrue(
                "Gem not unpacked from archive!",
                targetArchiveDir.resolve("asciidoctor-1.5.2.gem").toFile().exists());
        assertTrue(
                "Gem not installed",
                targetGemDir.resolve("gems")
                        .resolve("asciidoctor-1.5.2")
                        .resolve("lib")
                        .resolve("asciidoctor.rb").toFile().exists());
    }

    @Test
    public void shouldDeleteGemTargetDir() throws Exception {

        // Given
        Path targetGemDir = Paths.get("build", "shouldDeleteGemTargetDirGemDir");
        Path targetArchiveDir = Paths.get("build", "shouldDeleteGemTargetDirArchiveDir");

        File testFile = new File(targetGemDir.toFile(), "testFile");
        FileUtils.writeStringToFile(testFile, "Hello World");
        assertTrue(testFile.exists());

        // When
        new UncachedGemInstaller(targetGemDir, targetArchiveDir).deleteInstallationDirs();

        // Then
        assertFalse(testFile.exists());
        assertFalse(targetGemDir.toFile().exists());
        assertFalse(targetArchiveDir.toFile().exists());
    }


}
