package com.cunnie.trails;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * A class to compare two directories.
 * Not thread safe.
 */
class DirectoryComparer extends SimpleFileVisitor<Path> {
    private Path expected;
    private Path generated;
    private boolean match = true;
    private ArrayList<String> errors = new ArrayList<>();

    public DirectoryComparer(Path expected, Path generated) {
        this.expected = expected;
        this.generated = generated;
    }
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        FileVisitResult supersResult = super.preVisitDirectory(dir, attrs);

        Path relative = expected.relativize(dir);
        Path other = generated.resolve(relative);
        if (Files.exists(other) && Files.isDirectory(other)) {
        } else {
            System.out.println(dir.toString() + " does not match " + other);
            errors.add(dir.toString() + " does not match " + other);
            match = false;
        }
        return supersResult;
    }

    public boolean isMatch() {
        return match;
    }
}
