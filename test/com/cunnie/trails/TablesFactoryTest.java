package com.cunnie.trails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public class TablesFactoryTest {
    Connection connTest = null;
    @Before
    public void setUp() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connSchema = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/information_schema", "root", "root");
        connTest = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/netarkstest", "root", "root");
        String table1Sql = "CREATE TABLE table1 ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`initials` VARCHAR(2),"
                + "`agent_date` DATE,"
                + "PRIMARY KEY (`id`)"
                + ");";
        String table2Sql = "CREATE TABLE table2 ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`count` int DEFAULT 0,"
                + "`name` varchar(250) DEFAULT NULL,"
                + "PRIMARY KEY (`id`)"
                + ");";
        Statement statement = connTest.createStatement();
        statement.executeUpdate(table1Sql);
        statement.close();
        statement = connTest.createStatement();
        statement.executeUpdate(table2Sql);
        statement.close();
    }

    @After
    public void tearDown() throws Exception {
        Statement statement = connTest.createStatement();
        statement.executeUpdate("DROP TABLE table1");
        statement.close();
        statement = connTest.createStatement();
        statement.executeUpdate("DROP TABLE table2");
        statement.close();
    }

    @Test
    public void getTablesReturnsTheRightNumberAndNamesOfTables() throws Exception {
        List<String> tablesDbNames = Arrays.asList("table1", "table2");
        Collection<Table> tables = new TablesFactory(connTest).getTables();
        assertNotNull(tables);
        assertEquals(2, tables.size());
        Iterator<Table> iterator = tables.iterator();
        Table first = iterator.next();
        Table second = iterator.next();
        assertNotEquals(first, second);
        assertNotEquals(first.getDbName(), second.getDbName());
        assertTrue(tablesDbNames.contains(first.getDbName()));
        assertTrue(tablesDbNames.contains(second.getDbName()));
    }

    @Test
    public void getTablesReturnsTheRightNumberAndNamesOfFields() throws Exception {
        Collection<Table> tables = new TablesFactory(connTest).getTables();
        for (Table table: tables) {
            if (table.getDbName().equals("table1")) {
                Collection<Field> fields = table.getFields();
                List<String> fieldDbNames = new ArrayList<>();
                for (Field field: fields) {
                    fieldDbNames.add(field.getDbName());
                }
                Collections.sort(fieldDbNames);
                assertArrayEquals("The fields of table1 should be agent_date, id, and initials.  But they are " +
                        fieldDbNames, new String[]{"agent_date", "id", "initials"}, fieldDbNames.toArray());
            }
        }

    }
}