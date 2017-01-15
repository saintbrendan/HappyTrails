package com.cunnie.trails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Directory copies templates from <code>source</code> and copies the resolved contents to <code>destination</code>.
 * The files' extension is PREFIX, PER_RECORD, SUFFIX.
 * The tables name might also have the text TABLENAME or CLASSNAME.  This is resolved to the name of the class that corresponds to
 * that table.  (This means that no destination filename can ever contain the text TABLENAME or CLASSNAME.)
 * If a file does not contain the text TABLENAME it its name, it will be resolved once.
 * If it does not contain TABLE_NAME but does contain the suffix PER_RECORD, <code>resolveTo()</code> throws
 * an exception.
 */
public class Directory {
    private Path source;

    public Directory(Path source) {
        this.source = source;
    }

    public void resolveTo(Path destination, Collection<Table> tables) throws IOException {
        RecursiveTemplateResolver resolver = new RecursiveTemplateResolver(source, destination, tables);
        Files.walkFileTree(source, resolver);
    }
}
