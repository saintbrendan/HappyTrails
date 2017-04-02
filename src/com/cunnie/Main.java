package com.cunnie;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;


import com.cunnie.trails.Directory;
import com.cunnie.trails.Table;
import com.cunnie.trails.TablesFactory;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;


public class Main {

    private static String USERSHOW =
            "            <div class=\"form-group\">\n" +
                    "                <label class=\"col-sm-2 control-label\">%%%ENGLISHNAME%%%:</label>\n" +
                    "                <div class=\"col-sm-10\">\n" +
                    "                    <p class=\"form-control-static\" th:text=\"${%%%TABLE%%%.%%%DBNAME%%%}\">url....</p>\n" +
                    "                </div>\n" +
                    "            </div>\n";
    private static String USERS_HEADER = "                <th>%%%ENGLISHNAME%%%</th>\n";
    private static String USERS_DATA = "                <td th:text=\"${user.id}\"><a href=\"/user/${user.%%%DBNAME%%%}\">Id</a></td>\n";
    private static String USER_TEXT_FORM =
            "            <div class=\"form-group\">\n"+
                    "                <label class=\"col-sm-2 control-label\">%%%ENGLISHNAME%%%:</label>\n"+
                    "                <div class=\"col-sm-10\">\n"+
                    "                    <input type=\"text\" class=\"form-control\" th:field=\"*{%%%DBNAME%%%}\"/>\n"+
                    "                </div>\n"+
                    "            </div>\n";
    public static void main(String[] args) throws SQLException, ClassNotFoundException, ParseException, IOException {
        // get options
        Options options = new Options();
        Option sourceOpt = new Option("s", "source", true, "Source of .PREFIX and .SUFFIX and .PER_FIELD files");
        Option destOpt = new Option("d", "dest", true, "destination of generated files");
        Option schemaOpt = new Option("c", "schema", true, "database schema to interrogate");
        Option tablesOpt = new Option("t", "tables", true, "comma separated tables to process");
        options.addOption(sourceOpt);
        options.addOption(destOpt);
        options.addOption(schemaOpt);
        options.addOption(tablesOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String source = cmd.getOptionValue("source");
        String dest = cmd.getOptionValue("dest");
        String schema = cmd.getOptionValue("schema");
        String tableNamesRequestedOption = cmd.getOptionValue("tables");
        String[] tableNamesRequested = null;
        if (tableNamesRequestedOption != null) {
            tableNamesRequested = tableNamesRequestedOption.split(",");
        }

        System.out.println("source: " + source);
        System.out.println("dest: " + dest);
        System.out.println("schema: " + schema);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + schema, "root", "root");
        Collection<Table> tables = new TablesFactory(conn).getTables();
        Collection<Table> tablesRequested = tables;
        if (tableNamesRequested != null) {
            tablesRequested = new ArrayList<>();
            for(String tableName: tableNamesRequested) {
                for (Table table: tables) {
                    if (tableName.equals(table.getDbName())) {
                        tablesRequested.add(table);
                    }
                }
            }
        }
        Path sourcePath = Paths.get(source);
        Path destPath = Paths.get(dest);
        Directory sourceDir = new Directory(sourcePath);
        sourceDir.resolveTo(destPath, tablesRequested);
    }
}
