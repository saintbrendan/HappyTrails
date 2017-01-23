package com.cunnie;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.cunnie.trails.Directory;
import com.cunnie.trails.Field;
import com.cunnie.trails.Table;
import com.cunnie.trails.TablesFactory;
import javafx.scene.control.Tab;
import org.apache.commons.cli.*;


public class Main {
    private static String sqlSchema = "select column_name, data_type, character_maximum_length length, " +
            "    numeric_precision 'precision', " +
            "    numeric_scale scale, " +
            "    column_type  " +
            "from information_schema.columns " +
            "where table_schema = 'netarks' " +
            "    and table_name = ?;";

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
        options.addOption(sourceOpt);
        options.addOption(destOpt);
        options.addOption(schemaOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String source = cmd.getOptionValue("source");
        String dest = cmd.getOptionValue("dest");
        String schema = cmd.getOptionValue("schema");

        System.out.println("source: " + source);
        System.out.println("dest: " + dest);
        System.out.println("schema: " + schema);

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + schema, "root", "root");
        Collection<Table> tables = new TablesFactory(conn).getTables();
        Path sourcePath = Paths.get(source);
        Path destPath = Paths.get(dest);
        Directory sourceDir = new Directory(sourcePath);
        sourceDir.resolveTo(destPath, tables);

    }

    public static void makeFields() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/information_schema", "root", "root");
        PreparedStatement prep = conn.prepareStatement(sqlSchema);
        prep.setString(1, "user");
        ResultSet rs = prep.executeQuery();
        ArrayList<Field> fields = new ArrayList<>();
        while (rs.next()) {
            if (!"version".equals(rs.getString("column_name"))) {
                fields.add(new Field(rs.getString("column_name"), rs.getString("data_type")));
            }
        }
        String UserShowHtml = getString(USERSHOW, "user", fields);
        System.out.println("<!--  usershow.html  -->\n"+UserShowHtml);
        String UsersHeaderHtml = getString(USERS_HEADER, "user", fields);
        String UsersDataHtml = getString(USERS_DATA, "user", fields);
        System.out.println("\n\n\n<!--  users.html  -->\n"+UsersHeaderHtml);
        System.out.println("\n"+UsersDataHtml);

        // userform.html
        System.out.println("\n\n\n<!--  userform.html  -->\n"+getString(USER_TEXT_FORM, "user", fields));
    }

    public static String getString(String base, String tablename, List<Field> fields) {
        StringBuffer sb = new StringBuffer();
        for (Field field: fields) {
            sb.append(field.toFormHtml());
        }
        return sb.toString();
    }
}
