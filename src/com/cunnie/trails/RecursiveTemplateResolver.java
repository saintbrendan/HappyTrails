package com.cunnie.trails;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by saint on 11/27/2016.
 */
public class RecursiveTemplateResolver extends SimpleFileVisitor<Path> {
    private static final String PROJECT_JAVA_DIR = "src/main/java/";  // Assuming Java Spring default directories
    private Path sourcePath;
    private Path destinationPath;
    private Collection<Table> tables;
    private String javaPackage;
    HashMap<Path, HashMap<String, String[]>> filesFromPath = new HashMap<>();

    public RecursiveTemplateResolver(Path sourcePath, Path destinationPath, Collection<Table> tables) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.tables = tables;
    }

    /**
     * <code>FileVisitor</code> method.
     * Make create the destination directory if it doesn't already exist.
     * Get the java package string.
     * @param dir  The current directory being resolved.
     * @param attrs
     * @return super.preVisitDirectory(dir, attrs)
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
        filesFromPath.put(relativePath, new HashMap<>());

        javaPackage = getJavaPackage(sourcePath, PROJECT_JAVA_DIR, dir);

        return supersResult;
    }

    /**
     * Get the java package name (e.g. "com.storm.trident") from its path.
     * @param sourcePath  The project's absolute root directory.
     * @param projectJavaDir  The relative root java directory.  Usually PROJECT_DIR/src/main/java/.
     * @param dir  The directory we're resolving now.  E.g. PROJECT_DIR/src/main/java/com/storm/trident./
     * @return  The string of the java package.  E.g. "com.storm.trident".
     * @throws IOException
     */
    private String getJavaPackage(Path sourcePath, String projectJavaDir, Path dir) throws IOException {
        Path javaDir = sourcePath.resolve(projectJavaDir);  // What is the root java dir?
        Path javaPackagePath = javaDir.relativize(dir);  // What directory are we in relative to the root java dir?
        String javaPackage = javaPackagePath.toString().replace(File.separator, ".");
        return javaPackage;
    }

    @Override
    public  FileVisitResult postVisitDirectory(Path dir,  IOException exc) throws IOException {
        FileVisitResult superResult = super.postVisitDirectory(dir, exc);

        Path relativePath = sourcePath.relativize(dir);
        Path newDir = destinationPath.resolve(relativePath);
        HashMap<String, String[]> fileContentsByName = filesFromPath.get(relativePath);
        for (Map.Entry<String, String[]> entry: fileContentsByName.entrySet()) {
            String destinationFieldFilename = entry.getKey();
            String[] contents = entry.getValue();
            String content = String.join("", contents);
            Path newFile = newDir.resolve(destinationFieldFilename);
            Files.write(newFile, content.getBytes());
        }

        return  superResult;
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

        Path dir = file.getParent();
        Path relativePath = sourcePath.relativize(dir);
        System.out.println("file: " + file);
        System.out.println("dir: " + dir);
        System.out.println("relativePath: " + relativePath);

        HashMap<String, String[]> fileContentsByName = filesFromPath.get(relativePath);
        String destinationTableFilename = filename;
        Path parent = file.getParent();
        Path relativeParent = sourcePath.relativize(parent);
        Path newDir = destinationPath.resolve(relativeParent);
        contents = contents.replace("%%%PACKAGE%%%", this.javaPackage);
        if (filename.contains("TABLE")) {
            for (Table table: tables) {
                String tableTemplatizedContents = contents
                        .replace("%%%TABLE_NAME%%%", table.getDbName())
                        .replace("%%%TABLE_CLASS%%%", table.getClassName())
                        .replace("%%%TABLE_CAMEL_CASE%%%", table.getCamelClassName());
                destinationTableFilename = filename
                        .replace("TABLECLASS", table.getClassName())
                        .replace("TABLENAME", table.getDbName());
                System.out.println("destinationTableFilename: " + destinationTableFilename);
                // check for PREFIX, SUFFIX, PER_FIELD
                if (filename.contains(".PREFIX") || filename.contains(".SUFFIX") || filename.contains(".PER_FIELD")) {
                    String destinationFieldFilename = destinationTableFilename
                            .replace(".PREFIX", "")
                            .replace(".SUFFIX", "")
                            .replace(".PER_FIELD1", "")
                            .replace(".PER_FIELD2", "");
                    String[] fileParts = fileContentsByName.get(destinationFieldFilename);
                    if (fileParts == null) {
                        fileParts = new String[4];
                        Arrays.fill(fileParts, "");
                    }
                    if (filename.contains(".PER_FIELD")) {
                        for (Field field: table.getFields()) {
                            String fieldTemplatizedContents = tableTemplatizedContents.replace("%%%FIELD_NAME%%%", field.getDbName())
                                    .replace("%%%FIELD_DB_TYPE%%%", field.getDbType())
                                    .replace("%%%FIELD_JAVA_NAME%%%", field.getJavaName())
                                    .replace("%%%FIELD_JAVA_PASCAL_CASE%%%", field.getPascalCase())
                                    .replace("%%%FIELD_ENGLISH_NAME%%%", field.getEnglishName())
                                    .replace("%%%FIELD_PRE_DOMAIN%%%", field.getPredomain())
                                    .replace("%%%FIELD_JAVA_TYPE%%%", field.getJavaType())
                                    ;
                            if (filename.contains(".PER_FIELD1")) {
                                fileParts[1] += fieldTemplatizedContents;
                            } else { //.PER_FIELD2
                                fileParts[2] += fieldTemplatizedContents;
                            }
                        }
                    } else if (filename.contains(".PREFIX")) {
                        fileParts[0] = tableTemplatizedContents;
                    } else { //.SUFFIX
                        fileParts[3] = tableTemplatizedContents;
                    }
                    fileContentsByName.put(destinationFieldFilename, fileParts);
                } else {
                    Path newFile = newDir.resolve(destinationTableFilename);
                    Files.write(newFile, tableTemplatizedContents.getBytes());
                }
            }
        } else {
            Path newFile = newDir.resolve(destinationTableFilename);
            Files.write(newFile, contentsbytes);
        }

        filesFromPath.put(relativePath, fileContentsByName);
        return supersResult;
    }

    private String pascalCaseFromSnakeCase(String tablename) {
        // Remove _ and capitalize first letter of each word to PascalCase-ize
        // e.g.  first_name  -->  First Name
        String[] names = tablename.split("_");
        ArrayList<String> englishnames = new ArrayList<>();
        for (String name: names) {
            char[] namechars = name.toCharArray();
            namechars[0] = Character.toUpperCase(namechars[0]);
            englishnames.add(String.valueOf(namechars));
        }
        return String.join("", englishnames);
    }

    private String camelCaseFromPascalCase(String classname) {
        char[] namechars = classname.toCharArray();
        namechars[0] = Character.toLowerCase(namechars[0]);
        return String.valueOf(namechars);
    }

}
