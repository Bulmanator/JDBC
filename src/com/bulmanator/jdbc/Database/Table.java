package com.bulmanator.jdbc.Database;

import com.bulmanator.jdbc.Main;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

public class Table {

    private String name;
    private HashMap<String, Column> columns;
    private boolean hasForeign;
    private boolean multiplePrimary;

    public Table(String name, DatabaseMetaData metadata) throws SQLException {

        this.name = name;
        columns = new HashMap<>();
        hasForeign = false;
        multiplePrimary = false;

        // This is enough to get all of
        ResultSet columnData = metadata.getColumns(null, null, name, null);
        ResultSet primaryKeys = metadata.getPrimaryKeys(null, null, name);
        ResultSet foreignKeys = metadata.getImportedKeys(null, null, name);

        while (columnData.next()) {
            String columnName = columnData.getString("COLUMN_NAME");
            Column column = new Column(columnData);

            columns.put(columnName, column);
        }

        int primaryCount = 0;

        while (primaryKeys.next()) {
            String primaryKey = primaryKeys.getString("COLUMN_NAME");

            if(columns.containsKey(primaryKey)) {
                columns.get(primaryKey).setPrimary();
                primaryCount++;
            }
        }

        multiplePrimary = primaryCount > 1;

        while(foreignKeys.next()) {
            String key = foreignKeys.getString("FKCOLUMN_NAME");
            if(columns.containsKey(key)) {
                String foreignTable = foreignKeys.getString("PKTABLE_NAME");
                String foreignKey = foreignKeys.getString("PKCOLUMN_NAME");

                columns.get(key).setReference(foreignTable + "(" + foreignKey + ")");
                hasForeign = true;
            }
        }

        Collection<Column> columns = getColumns();
        for(Column column : columns) {

        }
    }

    public String getName() { return name; }

    public Column getColumn(String name) { return columns.get(name); }

    public boolean hasForeign() { return hasForeign; }

    public String getCreateQuery() {
        String query = "CREATE TABLE " + name + " (\n";

        Collection<Column> columns = this.columns.values();

       /* if(!multiplePrimary) {
            for(Column column : columns) {
                if(column.isPrimary()) {
                    query += "    " + column.getName() + " " + column.getType() + " PRIMARY KEY,\n";
                }
            }
        }*/

        for(Column column : columns) {
          //  if(column.isPrimary()) continue;

            query += "    " + column.getName() + " " + column.getType()
                    + (!column.isNullable() ? " NOT NULL" : "") + ",\n";
        }

     //   if(multiplePrimary) {
            Column[] array = new Column[columns.size()];
            array = columns.toArray(array);
            query += "    PRIMARY KEY (";
            for(int i = 0; i < columns.size(); i++) {
                Column column = array[i];

                if(column.isPrimary()) {
                    query += column.getName() + ", ";
                }
            }

            query = query.substring(0, query.lastIndexOf(", "));
            query += "),\n";
       // }

        for(Column column : columns) {
            if(column.hasReference()) {
                query += "    FOREIGN KEY (" + column.getName() + ") REFERENCES " + column.getReference() + ",\n";
            }
        }

        query = query.substring(0, query.lastIndexOf(","));

        query += "\n);";

        return query;
    }

    public Collection<Column> getColumns() { return columns.values(); }

    @Override
    public String toString() {
        String val = "";

        Collection<Column> columns = this.columns.values();
        for(Column column : columns) {
            val += column.toString() + "\n";
        }

        return val;
    }
}
