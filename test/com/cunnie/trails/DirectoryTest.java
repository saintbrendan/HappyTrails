package com.cunnie.trails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by saint on 11/25/2016.
 */
public class DirectoryTest {
    final String WHOLE_CONTENTS = "This is the first line of the file.\nThis is the second line.";
    final String WHOLE_TEMPLATIZED_CONTENTS = "public interface %%%TABLE_CLASS%%%Service {\n" +
            "    Iterable<%%%TABLE_CLASS%%%> listAll%%%TABLE_CLASS%%%s();\n" +
            "    User save%%%TABLE_CLASS%%%(%%%TABLE_CLASS%%% %%%TABLE_CAMEL_CASE%%%);\n" +
            "}";
    final String WHOLE_USER_CONTENTS = "public interface UserService {\n" +
            "    Iterable<User> listAllUsers();\n" +
            "    User saveUser(User user);\n" +
            "}";
    final String WHOLE_VEHICLE_CONTENTS = "public interface VehicleService {\n" +
            "    Iterable<Vehicle> listAllVehicles();\n" +
            "    User saveVehicle(Vehicle vehicle);\n" +
            "}";
    final String USER_HTML_CONTENTS = "<!DOCTYPE html>\n" +
            "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
            "<body>\n" +
            "<div class=\"container\">\n" +
            "    <div th:if=\"${not #lists.isEmpty(%%%TABLE_CLASS%%%s)}\">\n" +
            "    </div>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";
    final String USER_CONTROLLER_PREFIX_CONTENTS = "package com.netarks.springframework.controllers;\n" +
            "\n" +
            "import com.netarks.springframework.services.%%%TABLE_CLASS%%%Service;\n" +
            "import com.netarks.springframework.domain.%%%TABLE_CLASS%%%;";
    final String USER_CONTROLLER_PREFIX_W_PACKAGE_CONTENTS = "package %%%PACKAGE%%%.controllers;\n" +
            "\n" +
            "import com.netarks.springframework.services.%%%TABLE_CLASS%%%Service;\n" +
            "import com.netarks.springframework.domain.%%%TABLE_CLASS%%%;";
    final String PREFIX_FILE_CONTENTS = "PREFIX file.  ";
    final String PER_FIELD1_FILE_CONTENTS = "PER_FIELD1 file.  ";
    final String PER_FIELD2_FILE_CONTENTS = "PER_FIELD2 file.  ";
    final String SUFFIX_FILE_CONTENTS = "SUFFIX file.  ";

    final String PROJECT_JAVA_DIR = "src/main/java/";
    final String JAVA_PACKAGE = "com.testdomain.testproject";
    final String SUBDIR = PROJECT_JAVA_DIR + JAVA_PACKAGE.replace(".", "/");

    List<Field> FIELDS = Arrays.asList(new Field("id", "int"), new Field("version", "int"));
    final List<Table> TABLES = Arrays.asList( new Table("user", FIELDS),
            new Table("vehicle", FIELDS),
            new Table("some_table", FIELDS));
    Path sourcePath;
    Path destinationPath;
    Path newpath;
    Directory dir;

    @Before
    public void setUp() throws Exception {
        sourcePath = Files.createTempDirectory("source");
        destinationPath = Files.createTempDirectory("destination");
        newpath = Files.createDirectories(sourcePath.resolve(SUBDIR));
        dir = new Directory (sourcePath);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void resolveToCopiesDirectory() throws IOException {
        dir.resolveTo(destinationPath, TABLES);

        DirectoryComparer visitor = new DirectoryComparer(sourcePath, destinationPath);
        Files.walkFileTree(sourcePath, visitor);
        System.out.println("visitor.isMatch(): " + visitor.isMatch());

        assertTrue("The directories should match.  But they don't.", visitor.isMatch());
    }

    @Test
    public void resolveToCopiesWholeFiles() throws IOException {
        writeWholeFile(newpath, "test.txt", WHOLE_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);

        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("test.txt");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents "+ WHOLE_CONTENTS +
                " but it has contents " + content, WHOLE_CONTENTS, content);
    }

    @Test
    public void resolveToCopiesWholeTemplatizedFiles() throws IOException {
        writeWholeFile(newpath, "TABLECLASSService.java", WHOLE_TEMPLATIZED_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);

        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("UserService.java");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents "+ WHOLE_USER_CONTENTS +
                " but it has contents " + content, WHOLE_USER_CONTENTS, content);
    }

    @Test
    public void resolveToCopiesTemplatizedFileWithTableClassNameInFile() throws IOException {
        writeWholeFile(newpath, "TABLENAMEs.html", USER_HTML_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);

        String expectedContent = USER_HTML_CONTENTS.replace("%%%TABLE_CLASS%%%", "User");
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("users.html");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents "+ WHOLE_USER_CONTENTS +
                " but it has contents " + content, expectedContent, content);
    }

    @Test
    public void resolveToCopiesTemplatizedControllerPrefixFileWithTableClassNameInFile() throws IOException {
        writeWholeFile(newpath, "TABLECLASSController.java", USER_CONTROLLER_PREFIX_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);

        String expectedContent = USER_CONTROLLER_PREFIX_CONTENTS.replace("%%%TABLE_CLASS%%%", "User");
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("UserController.java");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents: \n"+ WHOLE_USER_CONTENTS +
                " but it has contents: \n" + content, expectedContent, content);
    }

    @Test
    public void resolveToAggregatesSuffixPerField1PerField2SuffixFilesToOneDestinationFile() throws Exception {
        writeWholeFile(newpath, "TABLECLASS.java.PREFIX", PREFIX_FILE_CONTENTS);
        writeWholeFile(newpath, "TABLECLASS.java.PER_FIELD1", PER_FIELD1_FILE_CONTENTS);
        writeWholeFile(newpath, "TABLECLASS.java.PER_FIELD2", PER_FIELD2_FILE_CONTENTS);
        writeWholeFile(newpath, "TABLECLASS.java.SUFFIX", SUFFIX_FILE_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("Vehicle.java");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));

        String content = new String(Files.readAllBytes(expectedFile));
        String expectedContent = PREFIX_FILE_CONTENTS + PER_FIELD1_FILE_CONTENTS + PER_FIELD1_FILE_CONTENTS + PER_FIELD2_FILE_CONTENTS + PER_FIELD2_FILE_CONTENTS + SUFFIX_FILE_CONTENTS;
        assertEquals("File "+expectedFile + " should have contents: \n"+ expectedContent +
                " but it has contents: \n" + content, expectedContent, content);
    }

    private void writeWholeFile(Path path, String filename, String contents) throws IOException {
        Path filepath = path.resolve(filename);
        Files.write(filepath, contents.getBytes());
    }

    @Test
    public void resolveToResolvesPackageName() throws IOException {
        writeWholeFile(newpath, "TABLECLASSController.java", USER_CONTROLLER_PREFIX_W_PACKAGE_CONTENTS);

        dir.resolveTo(destinationPath, TABLES);

        String expectedContent = USER_CONTROLLER_PREFIX_W_PACKAGE_CONTENTS
                .replace("%%%PACKAGE%%%", JAVA_PACKAGE)
                .replace("%%%TABLE_CLASS%%%", "User");
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("UserController.java");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        System.out.println("expectedContent: " + expectedContent);
        System.out.println("content: " + content);
        assertEquals("File "+expectedFile + " should have contents: \n"+ WHOLE_USER_CONTENTS +
                " but it has contents: \n" + content, expectedContent, content);
    }

}
