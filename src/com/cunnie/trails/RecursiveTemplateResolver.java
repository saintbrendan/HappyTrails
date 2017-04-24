package com.cunnie.trails;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;

/**
 * Created by saint on 11/27/2016.
 */
public class RecursiveTemplateResolver extends SimpleFileVisitor<Path> {
    private static final String PROJECT_JAVA_DIR = "src/main/java/";  // Assuming Java Spring default directories
    private Path sourcePath;
    private Path destinationPath;
    private Collection<Table> tables;
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
            System.out.println("About to write out to newFile: "+newFile);
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
        if ("NetarksWebApplication.java".equals(filename)) {
            int x = 5;
        }
        byte[] contentsbytes = Files.readAllBytes(file);
        String contents = new String(contentsbytes);

        Path dir = file.getParent();
        Path relativePath = sourcePath.relativize(dir);
        String javaPackage = getJavaPackage(sourcePath, PROJECT_JAVA_DIR, dir);
        String javaPackageParent = javaPackage.substring(0, javaPackage.lastIndexOf('.'));
        System.out.println("file: " + file);
        System.out.println("dir: " + dir);
        System.out.println("relativePath: " + relativePath);
        System.out.println("javaPackage: " + javaPackage);
        System.out.println("javaPackageParent: " + javaPackageParent);

        HashMap<String, String[]> fileContentsByName = filesFromPath.get(relativePath);
        String destinationTableFilename = filename;
        Path parent = file.getParent();
        Path relativeParent = sourcePath.relativize(parent);
        Path newDir = destinationPath.resolve(relativeParent);
        contents = contents.replace("%%%PACKAGE%%%", javaPackage);
        contents = contents.replace("%%%PACKAGE_PARENT%%%", javaPackageParent);
        if (filename.contains("TABLE")) {
            for (Table table: tables) {
                String tableTemplatizedContents;
                if (table.isEditable()) {
                    tableTemplatizedContents = contents.replace("%%%%IF_EDITABLE%%%%", "")
                            .replace("%%%%END_IF_EDITABLE%%%%", "");
                } else {
                    String regex = "%%%%IF_EDITABLE%%%%.*?%%%%END_IF_EDITABLE%%%%";
                    // remove all text between IF_EDITABLE and END_IF_EDITABLE tags
                    tableTemplatizedContents = Pattern.compile(regex, Pattern.DOTALL).matcher(contents).replaceAll("");
                }
                tableTemplatizedContents = tableTemplatizedContents
                        .replace("%%%TABLE_NAME%%%", table.getDbName())
                        .replace("%%%TABLE_CLASS%%%", table.getClassName())
                        .replace("%%%TABLE_CAMEL_CASE%%%", table.getCamelClassName())
                        .replace("%%%TABLE_ENGLISH_NAME%%%", table.getEnglishName())
                        .replace("%%%TABLE_ENGLISH_NAME_PLURAL%%%", table.getEnglishNamePlural());
                destinationTableFilename = filename
                        .replace("TABLECLASS", table.getClassName())
                        .replace("TABLENAME", table.getDbName());
                System.out.println("destinationTableFilename: " + destinationTableFilename);
                // check for PREFIX, PER_FIELD, MIDDLE, SUFFIX
                if (filename.contains(".PREFIX") || filename.contains(".PER_FIELD") || filename.contains(".MIDDLE") || filename.contains(".SUFFIX")) {
                    String destinationFieldFilename = destinationTableFilename
                            .replace(".PREFIX", "")
                            .replace(".PER_FIELD1", "")
                            .replace(".MIDDLE", "")
                            .replace(".PER_FIELD2", "")
                            .replace(".SUFFIX", "")
                            ;
                    String[] fileParts = fileContentsByName.get(destinationFieldFilename);
                    if (fileParts == null) {
                        fileParts = new String[5];
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
                                    .replace("%%%FIELD_FORM_HTML%%%", field.toFormHtml())
                                    .replace("%%%FIELD_SHOW_HTML%%%", field.toShowHtml())
                                    .replace("%%%TABLE_CAMEL_CASE%%%", table.getCamelClassName())
                                    ;
                            if (filename.contains(".PER_FIELD1")) {
                                fileParts[1] += fieldTemplatizedContents;
                            } else { //.PER_FIELD2
                                fileParts[3] += fieldTemplatizedContents;
                            }
                        }
                    } else if (filename.contains(".PREFIX")) {
                        fileParts[0] = tableTemplatizedContents;
                    } else if (filename.contains(".MIDDLE")) {
                        fileParts[2] = tableTemplatizedContents;
                    } else { //.SUFFIX
                        fileParts[4] = tableTemplatizedContents;
                    }
                    fileContentsByName.put(destinationFieldFilename, fileParts);
                } else {
                    Path newFile = newDir.resolve(destinationTableFilename);
                    Files.write(newFile, tableTemplatizedContents.getBytes());
                }
            }
        } else {
            /// Find a cleaner way to do this
            Path newFile = newDir.resolve(destinationTableFilename);
            if (filename.contains("header.html")) {
                String template = "                        <li><a href=\"#\" th:href=\"@{/products}\">Products</a></li>";
                contents = contents.replace(template, "xxx");
                for (Table table: tables) {
                    String replacement = "                        <li><a href=\"#\" th:href=\"@{/"
                            + table.getDbName() + "s}\">"
                            + table.getEnglishNamePlural()
                            + "</a></li>\nxxx";
                            ;
                    contents = contents.replace("xxx",
                            replacement);
                }
                contents = contents.replace("xxx","");
            }
            Files.write(newFile, contents.getBytes());
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
