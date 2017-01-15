package com.cunnie.trails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    final String SUBDIR = "newpath";

    final ArrayList<Table> NULL_TABLES = new ArrayList<>(0);
    final List<Table> TABLES = Arrays.asList( new Table("user", new ArrayList<Field>()),
            new Table("vehicle", new ArrayList<Field>()),
            new Table("some_table", new ArrayList<Field>()));
    Path sourcePath;
    Path destinationPath;
    Path newpath;

    @Before
    public void setUp() throws Exception {
        sourcePath = Files.createTempDirectory("source");
        destinationPath = Files.createTempDirectory("destination");
        newpath = Files.createDirectories(sourcePath.resolve(SUBDIR));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void resolveToCopiesDirectory() throws IOException {
        Directory dir = new Directory (sourcePath);
        dir.resolveTo(destinationPath, NULL_TABLES);

        DirectoryComparer visitor = new DirectoryComparer(sourcePath, destinationPath);
        Files.walkFileTree(sourcePath, visitor);
        System.out.println("visitor.isMatch(): " + visitor.isMatch());

        assertTrue("The directories should match.  But they don't.", visitor.isMatch());
    }

    @Test
    public void resolveToCopiesWholeFiles() throws IOException {
        writeWholeFileToPath(newpath);

        Directory dir = new Directory (sourcePath);
        dir.resolveTo(destinationPath, NULL_TABLES);

        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("test.txt");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents "+ WHOLE_CONTENTS +
                " but it has contents " + content, WHOLE_CONTENTS, content);
    }

    @Test
    public void resolveToCopiesWholeTemplatizedFiles() throws IOException {
        writeWholeTemplatizedFileToPath();

        Directory dir = new Directory (sourcePath);
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
        writeUserHtmlContentsFileToPath();

        Directory dir = new Directory (sourcePath);
        dir.resolveTo(destinationPath, TABLES);

        String expected_content = USER_HTML_CONTENTS.replace("%%%TABLE_CLASS%%%", "User");
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("users.html");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents "+ WHOLE_USER_CONTENTS +
                " but it has contents " + content, expected_content, content);
    }

    @Test
    public void resolveToCopiesTemplatizedControllerPrefixFileWithTableClassNameInFile() throws IOException {
        writeUserControllerContentsFileToPath();

        Directory dir = new Directory (sourcePath);
        dir.resolveTo(destinationPath, TABLES);

        String expected_content = USER_CONTROLLER_PREFIX_CONTENTS.replace("%%%TABLE_CLASS%%%", "User");
        Path expectedDir = destinationPath.resolve(SUBDIR);
        Path expectedFile = expectedDir.resolve("UserController.java.PREFIX");
        assertTrue("File ["+expectedFile+"] should exist.  But it doesn't.", Files.exists(expectedFile));
        String content = new String(Files.readAllBytes(expectedFile));

        assertEquals("File "+expectedFile + " should have contents: \n"+ WHOLE_USER_CONTENTS +
                " but it has contents: \n" + content, expected_content, content);
    }

    /**
     * Write a whole file to directory newpath
     * @param newpath  the directory to write the WHOLE file to.
     */
    private void writeWholeFileToPath(Path newpath) throws IOException {
        Path filepath = newpath.resolve("test.txt");
        Files.write(filepath, WHOLE_CONTENTS.getBytes());
    }

    private void writeWholeTemplatizedFileToPath() throws IOException {
        Path filepath = newpath.resolve("TABLECLASSService.java");
        Files.write(filepath, WHOLE_TEMPLATIZED_CONTENTS.getBytes());
    }

    private void writeUserHtmlContentsFileToPath() throws IOException {
        Path filepath = newpath.resolve("TABLENAMEs.html");
        Files.write(filepath, USER_HTML_CONTENTS.getBytes());
    }

    private void writeUserControllerContentsFileToPath() throws IOException {
        Path filepath = newpath.resolve("TABLECLASSController.java.PREFIX");
        Files.write(filepath, USER_CONTROLLER_PREFIX_CONTENTS.getBytes());
    }

}
