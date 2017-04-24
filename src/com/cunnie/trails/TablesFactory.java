package com.cunnie.trails;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TablesFactory {
    List<Table> tables = new ArrayList<>();

    public TablesFactory(Connection connection) throws SQLException {
        String catalog = connection.getCatalog();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(catalog, catalog, "%", null);
        while (rs.next()) {
            String tableType = rs.getString("TABLE_TYPE");
            List<Field> fields = new ArrayList<>();
            String tableName = rs.getString("TABLE_NAME");
            ResultSet rsColumns = metaData.getColumns(catalog, catalog, tableName, null);
            while(rsColumns.next()) {
                Field field = new Field(rsColumns.getString("COLUMN_NAME"),
                        rsColumns.getString("TYPE_NAME"),
                        rsColumns.getInt("COLUMN_SIZE"),
                        rsColumns.getInt("COLUMN_SIZE"),
                        rsColumns.getInt("DECIMAL_DIGITS")
                        );
                fields.add(field);
            }
            tables.add(new Table(tableName, tableType, fields));
        }
    }

    public Collection<Table> getTables() {
        return tables;
    }
}
