package org.arquillian.jruby.gems;

import org.arquillian.jruby.util.FileUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UncachedGemInstaller implements GemInstaller {

    private static final Logger LOG = Logger.getLogger(UncachedGemInstaller.class.getName());

    private Path targetDir;

    public UncachedGemInstaller(Path targetDir) throws DeploymentException {
        this.targetDir = targetDir;

        if (!targetDir.toFile().exists()) {
            if (!targetDir.toFile().mkdirs()) {
                throw new DeploymentException("Could not create gem install directory!");
            }
        }
    }

    @Override
    public void installGemsFromArchive(Archive archive) throws DeploymentException {

        Map<String, File> gemsToInstall = unpackGemsFromArchive(archive);

        LOG.fine("Invoke JRuby install");
        try {
            installGems(gemsToInstall);
        } catch (IOException | InterruptedException e) {
            throw new DeploymentException("Error during installation of gems", e);
        }
        LOG.fine("Finished JRuby installation");
    }

    Map<String,File> unpackGemsFromArchive(Archive archive) throws DeploymentException {
        Map<ArchivePath, Node> gems = archive.getContent(Filters.include("/.*.gem"));
        LOG.fine("Installing " + gems.keySet());

        Map<String, File> gemsToInstall = new HashMap<>();

        for (Map.Entry<ArchivePath, Node> gemEntry: gems.entrySet()) {
            LOG.fine("Unpack " + gemEntry.getKey());

            String gemName = getGemFullNameFromArchivePath(gemEntry.getKey());

            File gemFile = FileUtils.unpackGemFromArchive(gemEntry.getKey(), gemEntry.getValue(), targetDir);

            gemsToInstall.put(gemName, gemFile);
        }
        return gemsToInstall;
    }


    private void installGems(Map<String, File> gemsToInstall) throws IOException, InterruptedException, DeploymentException {
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
        pb.command().add("--install-dir=" + targetDir.toAbsolutePath().toString());
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
    public void deleteInstallationDir() throws IOException {
        FileUtils.deleteDir(targetDir);
    }

}
