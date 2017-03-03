package com.bulmanator.jdbc.Database;

import com.bulmanator.jdbc.Main;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Table {

    private String name;
    private HashMap<String, Column> columns;
    private boolean hasForeign;
    private ArrayList<String> referenceTables;

    /**
     * Generates all of the columns stored within the table from the given name
     * @param name The name of the table to create
     * @param metadata The metadata of the database the table is contained within
     * @throws SQLException Thrown if any SQL exceptions
     */
    public Table(String name, DatabaseMetaData metadata) throws SQLException {

        this.name = name;
        columns = new HashMap<>();
        referenceTables = new ArrayList<>();
        hasForeign = false;

        ResultSet columnData = metadata.getColumns(null, null, name, null);
        ResultSet primaryKeys = metadata.getPrimaryKeys(null, null, name);
        ResultSet foreignKeys = metadata.getImportedKeys(null, null, name);

        while (columnData.next()) {
            String columnName = columnData.getString("COLUMN_NAME");
            Column column = new Column(columnData);

            columns.put(columnName, column);
        }

        while (primaryKeys.next()) {
            String primaryKey = primaryKeys.getString("COLUMN_NAME");

            if(columns.containsKey(primaryKey)) {
                columns.get(primaryKey).setPrimary();
            }
        }

        while(foreignKeys.next()) {
            String key = foreignKeys.getString("FKCOLUMN_NAME");
            if(columns.containsKey(key)) {
                String foreignTable = foreignKeys.getString("PKTABLE_NAME");
                String foreignKey = foreignKeys.getString("PKCOLUMN_NAME");
                columns.get(key).setReference(foreignTable + "(" + foreignKey + ")");
                referenceTables.add(foreignTable);
                hasForeign = true;
            }
        }
    }

    public String getName() { return name; }

    public Column getColumn(String name) { return columns.get(name); }

    public boolean hasForeign() { return hasForeign; }

    public String getCreateQuery() {
        String query = "CREATE TABLE " + name + " (\n";

        Collection<Column> columns = this.columns.values();

        for(Column column : columns) {
            query += "    " + column.getName() + " " + column.getType()
                    + (!column.isNullable() ? " NOT NULL" : "") + ",\n";
        }

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

    public ArrayList<String> getInsertSatements(Statement statement) throws SQLException {
        ResultSet data = statement.executeQuery("SELECT * FROM " + name);

//        Main.printResultSet(data);

        Collection<Column> columns = getColumns();
        ArrayList<String> statements = new ArrayList<>();

        while (data.next()) {
            String insert = "INSERT INTO " + name + " VALUES (";
            for (Column column : columns) {
                if (column.getType().toLowerCase().contains("varchar")) {
                    String value = data.getString(column.getName());

                    if(value.contains("'")) {
                        String[] split = value.split("'");
                        value = split[0];
                        for(int i = 1; i < split.length; i++) {
                            value += "''" + split[i];
                        }

                    }

                    insert += ("'" + value + "', ");
                } else if (column.getType().toLowerCase().contains("int")) {
                    int value = data.getInt(column.getName());
                    insert += (value) + ", ";
                }
                else {
                    System.out.println("Other Value!");
                }
            }

            insert = insert.substring(0, insert.lastIndexOf(","));
            insert += ");";

            statements.add(insert);
        }

        return statements;
    }

    public ArrayList<String> getReferenceTables() { return referenceTables; }

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
