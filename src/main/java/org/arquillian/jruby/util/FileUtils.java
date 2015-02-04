package org.arquillian.jruby.util;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class FileUtils {

    private FileUtils() {
    }

    public static void deleteDir(Path targetDir) throws IOException {
        Files.walkFileTree(targetDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    public static void copyDir(final Path fromPath, final Path toPath) throws IOException {

        toPath.toFile().mkdirs();

        Files.walkFileTree(
                fromPath,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetPath = toPath.resolve(fromPath.relativize(dir));
                        if (!Files.exists(targetPath)) {
                            Files.createDirectory(targetPath);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, toPath.resolve(fromPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    public static File unpackGemFromArchive(ArchivePath archivePath, Node gemEntry, Path targetDir) throws DeploymentException {
        File gemFile = new File(targetDir.toFile(), archivePath.get());
        if (gemFile.exists()) {
            // Was unpacked before.
            return gemFile;
        }
        if (!gemFile.getParentFile().exists()) {
            if (!gemFile.getParentFile().mkdirs()) {
                throw new DeploymentException("Error while unpacking file " + archivePath + " from archive. Could not create directory " + gemFile.getParentFile());
            }
        }
        Asset asset = gemEntry.getAsset();
        if (asset != null) {
            try (InputStream gemIn = gemEntry.getAsset().openStream();
                 FileOutputStream gemFileOut = new FileOutputStream(gemFile);) {
                // TODO: Use commons-io for this? Or is there already sth in Arquillian/ShrinkWrap?

                byte[] buf = new byte[65535];
                int bytesRead = gemIn.read(buf);
                while (bytesRead > 0) {
                    gemFileOut.write(buf, 0, bytesRead);
                    bytesRead = gemIn.read(buf);
                }
                return gemFile;
            } catch (IOException e) {
                throw new DeploymentException("Error while unpacking file " + archivePath + " from archive", e);
            }
        }
        return gemFile;
    }

}