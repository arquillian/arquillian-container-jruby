package org.arquillian.jruby.gems;

import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JRubyGemCacheTest {

    public static final String HELLO_WORLD = "Hello World";
    private static File asciidoctorGem;

    @BeforeClass
    public static void resolveAsciidoctorGem() {
        asciidoctorGem = Maven.configureResolver()
            .withRemoteRepo("rubygems", "http://rubygems-proxy.torquebox.org/releases", "default")
            .resolve("rubygems:asciidoctor:gem:1.5.2")
            .withoutTransitivity()
            .asSingleFile();
    }

    @Test
    public void shouldReturnNullOnCacheMiss() throws Exception {

        // Given
        File cachedir = new File("build/emptytestgemcache");
        cachedir.mkdirs();
        Path targetGemDir = Paths.get("build", "shouldReturnNullOnCacheMissGemDir");
        Path targetArchiveDir = Paths.get("build", "shouldReturnNullOnCacheMissArchiveDir");
        CachingGemInstaller gemCache =
            new CachingGemInstaller(Paths.get(cachedir.toURI()), targetGemDir, targetArchiveDir);

        Map<String, File> gemFiles = Collections.singletonMap(
            asciidoctorGem.getName(),
            asciidoctorGem);

        // When
        Path cachedGemsDirectory = gemCache.getCachedGemDirectory(gemFiles);

        // Then
        assertNull(cachedGemsDirectory);
    }

    @Test
    public void shouldReturnCachedDirOnCacheHit() throws Exception {

        // Given
        File cachedir = new File("build/testgemcache");
        cachedir.mkdirs();
        Path targetGemDir = Paths.get("build", "shouldReturnCachedDirOnCacheHitGemDir");
        Path targetArchiveDir = Paths.get("build", "shouldReturnCachedDirOnCacheHitArchiveDir");
        CachingGemInstaller gemCache =
            new CachingGemInstaller(Paths.get(cachedir.toURI()), targetGemDir, targetArchiveDir);

        File originalTestTargetDir = new File("build/originalTestCachehitdir");
        originalTestTargetDir.mkdirs();

        FileUtils.writeStringToFile(new File(originalTestTargetDir, "test.txt"), HELLO_WORLD);

        Map<String, File> gemFiles = Collections.singletonMap(
            asciidoctorGem.getName(),
            asciidoctorGem);

        // When
        @SuppressWarnings("unused")
        Path cachedGemsDirectory = gemCache.addToCache(gemFiles, Paths.get(originalTestTargetDir.toURI()));
        Path cacheHit = gemCache.getCachedGemDirectory(gemFiles);

        // Then
        assertNotNull(cacheHit);

        assertThat(FileUtils.readFileToString(new File(cacheHit.toFile(), "test.txt")), is(HELLO_WORLD));
    }
}
