package org.arquillian.jruby.embedded;

import java.nio.file.Path;

public class JRubyTemporaryDir {

    private Path tempDir;


    public JRubyTemporaryDir(Path tempDir) {
        this.tempDir = tempDir;
    }

    public Path getTempDir() {
        return tempDir;
    }
}
