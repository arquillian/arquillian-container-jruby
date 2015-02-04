package org.arquillian.jruby.embedded;

import java.nio.file.Path;

public class JRubyTemporaryDir {

    private final Path tempArchiveDir;

    private Path tempGemDir;


    public JRubyTemporaryDir(Path tempGemDir, Path tempArchiveDir) {
        this.tempGemDir = tempGemDir;
        this.tempArchiveDir = tempArchiveDir;
    }

    /**
     * @return The directory where the gems are installed in.
     */
    public Path getTempGemDir() {
        return tempGemDir;
    }

    /**
     * @return The directory where the archive is unpacked
     */
    public Path getTempArchiveDir() {
        return tempArchiveDir;
    }
}
