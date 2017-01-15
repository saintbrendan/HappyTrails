package com.cunnie.trails;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TablesFactory {
    private static String sqlcolumns = "select column_name, data_type, character_maximum_length, \n" +
            "    numeric_precision, \n" +
            "    numeric_scale \n" +
            "from information_schema.columns \n" +
            "where table_schema = 'netarkstest' \n" +
            "    and table_name = ?;";
    private static String sqltables = "select table_name " +
            "from information_schema.tables " +
            "where table_schema = 'netarkstest'; ";
    private Connection connection;
    List<Table> tables = new ArrayList<>();


    public TablesFactory(Connection connection) throws SQLException {
        this.connection = connection;
        System.out.println("connection.getCatalog(): "+connection.getCatalog());///
        /// TODO: refactor as prepared statement passing schema name from connection.getCatalog()
        Statement statement = connection.createStatement();
        /// refactor using DatabaseMetaData.getTables()
        ResultSet rs = statement.executeQuery(sqltables);
        while (rs.next()) {
            List<Field> fields = new ArrayList<>();
            String tableName = rs.getString("table_name");
            /// refactor using DatabaseMetaData.getColumns()
            PreparedStatement columnsStmt = connection.prepareStatement(sqlcolumns);
            columnsStmt.setString(1, tableName);
            System.out.println("columnStmt: "+columnsStmt);
            System.out.println("columnStmt.getParameterMetaData(): "+columnsStmt.getParameterMetaData());
            ResultSet rsColumns = columnsStmt.executeQuery();
            while(rsColumns.next()) {
                Field field = new Field(rsColumns.getString("column_name"),
                        rsColumns.getString("data_type"),
                        rsColumns.getInt("character_maximum_length"),
                        rsColumns.getInt("numeric_precision"),
                        rsColumns.getInt("numeric_scale")
                        );
                fields.add(field);
            }
            tables.add(new Table(tableName, fields));
        }
    }

    public Collection<Table> getTables() {
        return tables;
    }
}
