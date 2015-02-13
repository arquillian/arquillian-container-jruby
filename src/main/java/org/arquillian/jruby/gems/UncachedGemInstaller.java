package org.arquillian.jruby.gems;

import org.arquillian.jruby.util.FileUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UncachedGemInstaller implements GemInstaller {

    private static final Logger LOG = Logger.getLogger(UncachedGemInstaller.class.getName());

    private final Path targetArchiveDir;

    private final Path targetGemDir;

    public UncachedGemInstaller(Path targetGemDir, Path targetArchiveDir) throws DeploymentException {
        this.targetGemDir = targetGemDir;
        this.targetArchiveDir = targetArchiveDir;

        if (!targetGemDir.toFile().exists()) {
            if (!targetGemDir.toFile().mkdirs()) {
                throw new DeploymentException("Could not create gem install directory!");
            }
        }
        if (!targetArchiveDir.toFile().exists()) {
            if (!targetArchiveDir.toFile().mkdirs()) {
                throw new DeploymentException("Could not create archive install directory!");
            }
        }
    }

    @Override
    public void installGemsFromArchive(Archive<?> archive) throws DeploymentException {

        Map<ArchivePath, File> archiveFiles = unpackArchive(archive);

        Map<String, File> gemsToInstall = getGems(archiveFiles);

        LOG.fine("Invoke JRuby install");
        try {
            installGems(gemsToInstall);
        } catch (IOException | InterruptedException e) {
            throw new DeploymentException("Error during installation of gems", e);
        }
        LOG.fine("Finished JRuby installation");
    }

    Map<String, File> getGems(Map<ArchivePath, File> archiveFiles) {
        Map<String, File> ret = new HashMap<>();
        for (Map.Entry<ArchivePath, File> archiveFile: archiveFiles.entrySet()) {
            if (archiveFile.getKey().get().matches("/.+gem")) {
                ret.put(getGemFullNameFromArchivePath(archiveFile.getKey()), archiveFile.getValue());
            }
        }
        return ret;
    }

    Map<ArchivePath, File> unpackArchive(Archive<?> archive) throws DeploymentException {
        Map<ArchivePath, Node> gems = archive.getContent();
        LOG.fine("Installing " + gems.keySet());

        Map<ArchivePath, File> files = new HashMap<>();

        for (Map.Entry<ArchivePath, Node> gemEntry: gems.entrySet()) {
            LOG.fine("Unpack " + gemEntry.getKey());

            File gemFile = FileUtils.unpackGemFromArchive(gemEntry.getKey(), gemEntry.getValue(), targetArchiveDir);

            files.put(gemEntry.getKey(), gemFile);
        }
        return files;
    }


    private void installGems(Map<String, File> gemsToInstall) throws IOException, InterruptedException, DeploymentException {
        if (gemsToInstall == null || gemsToInstall.isEmpty()) {
            return;
        }
        // This is deliberately taken from the JRuby gradle plugin
        // https://github.com/jruby-gradle/jruby-gradle-plugin
        final String javaHome = System.getProperty("java.home");
        final String java = javaHome + File.separator + "bin" + File.separator + "java";

        ProcessBuilder pb = new ProcessBuilder(
                java,
                "-classpath", System.getProperty("java.class.path"),
                "org.jruby.Main",
                "-S", "gem", "install"
        );
        for (Map.Entry<String, File> gemToInstall: gemsToInstall.entrySet()) {
            pb.command().add(gemToInstall.getValue().getAbsolutePath());
        }
        pb.command().add("--ignore-dependencies");
        pb.command().add("--install-dir=" + targetGemDir.toAbsolutePath().toString());
        pb.command().add("-N");
        pb.command().add("--platform=java");

        Map<String, String> envEntries = new HashMap<>();
        envEntries.put("JBUNDLE_SKIP", "true");
        envEntries.put("JARS_SKIP", "true");
        if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            envEntries.put("TMP", System.getenv("TMP"));
            envEntries.put("TEMP", System.getenv("TEMP"));
        }
        pb.environment().putAll(envEntries);

        Process process = pb.start();

        int ret = process.waitFor();
        if (ret != 0) {
            throw new DeploymentException("Installation failed " + ret);
        }

    }

    private String getGemFullNameFromArchivePath(ArchivePath key) {
        return key.get().substring(1, key.get().length() - 4);
    }

    @Override
    public void deleteInstallationDirs() throws IOException {
        FileUtils.deleteDir(targetGemDir);
        FileUtils.deleteDir(targetArchiveDir);
    }

}
