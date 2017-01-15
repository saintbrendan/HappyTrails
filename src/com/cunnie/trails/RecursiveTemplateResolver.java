package com.cunnie.trails;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by saint on 11/27/2016.
 */
public class RecursiveTemplateResolver extends SimpleFileVisitor<Path> {
    private Path sourcePath;
    private Path destinationPath;
    private Collection<String> tablenames;

    public RecursiveTemplateResolver(Path sourcePath, Path destinationPath, Collection<String> tablenames) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.tablenames = tablenames;
    }

    /**
     * <code>FileVisitor</code> method.
     * Make create the destination directory if it doesn't already exist.
     * @param dir
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        FileVisitResult supersResult = super.preVisitDirectory(dir, attrs);

        Path relativePath = sourcePath.relativize(dir);
        Path newDir = destinationPath.resolve(relativePath);
        if (Files.notExists(newDir)) {
            Files.createDirectory(newDir);
        }

        return supersResult;
    }

    /**
     * <code>FileVisitor</code> method.
     * Resolve a single template file to one or more destination files.
     * @param file
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        FileVisitResult supersResult = super.visitFile(file, attrs);
        String filename = file.getFileName().toString();
        byte[] contentsbytes = Files.readAllBytes(file);
        String contents = new String(contentsbytes);

        /// check for PREFIX, SUFFIX, PER_FIELD
            String destinationFilename = filename;
            Path parent = file.getParent();
            Path relativeParent = sourcePath.relativize(parent);
            Path newDir = destinationPath.resolve(relativeParent);
            if (filename.contains("TABLE")) {
                for (String tablename: tablenames) {
                    String classname = pascalCaseFromSnakeCase(tablename);
                    String camelcasename = camelCaseFromPascalCase(classname);
                    String templatizedContents = contents
                            .replace("%%%TABLE_CLASS%%%", classname)
                            .replace("%%%TABLE_CAMEL_CASE%%%", camelcasename);
                    destinationFilename = filename
                            .replace("TABLECLASS", classname)
                            .replace("TABLENAME", tablename);
                    System.out.println("destinationFilename: " + destinationFilename);
                    Path newFile = newDir.resolve(destinationFilename);
                    Files.write(newFile, templatizedContents.getBytes());
                }
            } else {
                Path newFile = newDir.resolve(destinationFilename);
                Files.write(newFile, contentsbytes);
            }

        return supersResult;
    }

    private String pascalCaseFromSnakeCase(String tablename) {
        // Replace _ and capitalize first letter of each word to Englishize
        // e.g.  first_name  -->  First Name
        String[] names = tablename.split("_");
        ArrayList<String> englishnames = new ArrayList<>();
        for (String name: names) {
            char[] namechars = name.toCharArray();
            namechars[0] = Character.toUpperCase(namechars[0]);
            englishnames.add(String.valueOf(namechars));
        }
        return String.join(" ", englishnames);
    }

    private String camelCaseFromPascalCase(String classname) {
        char[] namechars = classname.toCharArray();
        namechars[0] = Character.toLowerCase(namechars[0]);
        return String.valueOf(namechars);
    }

}
