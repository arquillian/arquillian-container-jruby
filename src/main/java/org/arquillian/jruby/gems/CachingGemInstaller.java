package org.arquillian.jruby.gems;

import org.arquillian.jruby.util.FileUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CachingGemInstaller implements GemInstaller {

    private final Path cacheDir;

    private final UncachedGemInstaller uncachedGemInstaller;
    private final Path targetDir;

    public CachingGemInstaller(Path cacheDir, Path targetDir) throws DeploymentException {
        this.cacheDir = cacheDir;
        this.targetDir = targetDir;
        this.uncachedGemInstaller = new UncachedGemInstaller(targetDir);
    }

    Path getCachedGemDirectory(Map<String, File> gemFiles) throws NoSuchAlgorithmException {
        String digest = computeDigest(gemFiles);

        Path cacheEntry = Paths.get(new File(cacheDir.toFile(), digest).toURI());

        if (cacheEntry.toFile().exists()) {
            return cacheEntry;
        } else {
            return null;
        }
    }

    public Path addToCache(Map<String, File> gemFiles, Path originalTestTargetDir) throws IOException, NoSuchAlgorithmException {

        String digest = computeDigest(gemFiles);

        Path cacheEntry = Paths.get(new File(cacheDir.toFile(), digest).toURI());

        FileUtils.copyDir(originalTestTargetDir, cacheEntry);

        return cacheEntry;
    }

    private String computeDigest(Map<String, File> gemFiles) throws NoSuchAlgorithmException {
        List<String> gemNames = new ArrayList<>(gemFiles.keySet());
        Collections.sort(gemNames);

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        for (String gemName: gemNames) {
            messageDigest.update(gemName.getBytes());
        }
        return new BigInteger(messageDigest.digest()).abs().toString(16);
    }

    @Override
    public void installGemsFromArchive(Archive archive) throws DeploymentException {
        Map<String, File> gemsToInstall = uncachedGemInstaller.unpackGemsFromArchive(archive);

        try {
            Path cacheHit = getCachedGemDirectory(gemsToInstall);
            if (cacheHit != null) {
                FileUtils.copyDir(cacheHit, targetDir);
                return;
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new DeploymentException("Unexpected exception while retrieving gem dir from cache", e);
        }

        uncachedGemInstaller.installGemsFromArchive(archive);

        try {
            addToCache(gemsToInstall, targetDir);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DeploymentException("Unexpected exception while copying gem dir to cache", e);
        }

    }

    @Override
    public void deleteInstallationDir() throws IOException {
        uncachedGemInstaller.deleteInstallationDir();
    }
}
