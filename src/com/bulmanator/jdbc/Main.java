package com.bulmanator.jdbc;

import com.bulmanator.jdbc.Database.Column;
import com.bulmanator.jdbc.Database.Table;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    private String databaseName;

    public static void main(String[] args) {
        try {
            new Main(args[0]).run();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Main(String databaseName) { this.databaseName = databaseName; }

    private void run() throws SQLException {

        // Loads the SQLite Driver class
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        // Creates a connection to the database provided
        Connection database = DriverManager.getConnection("jdbc:sqlite:Databases/" + databaseName);
        if(database == null) return;

        // Gets the metadata for the entire database
        DatabaseMetaData metadata = database.getMetaData();

        // Gets the data for all of the tables, ignoring indexes
        ResultSet tableData = metadata.getTables(null, null, null, new String[] { "TABLE" });


        // Generates all of the table information and
        ArrayList<Table> tables = new ArrayList<>();
        while(tableData.next()) {
            String tableName = tableData.getString("TABLE_NAME");
            tables.add(new Table(tableName, metadata));
        }


        // Sort the tables to make sure they get created in the right order
        tables.sort((t1, t2) -> t1.hasForeign() ? t2.hasForeign() ? 0 : 1 : -1);
        for(int i = 0; i < tables.size(); i++) {
            Table one = tables.get(i);
            if(!one.hasForeign()) continue;
            for(int j = i + 1; j < tables.size(); j++) {
                Table two = tables.get(j);
                if(!two.hasForeign()) continue;

                if(one.getReferenceTables().contains(two.getName())) {
                    Collections.swap(tables, i, j);
                }
            }
        }

        // Print out all of the table create queries
        for(Table table : tables) {
            System.out.println(table.getCreateQuery());
        }

        System.out.println();

        // Get all of the insert statements and then print them out
        for (Table table : tables) {
            ArrayList<String> statements = table.getInsertSatements(database.createStatement());
            for(String statement : statements) {
                System.out.println(statement);
            }
            System.out.println();
        }

    }

    public static void printResultSet(ResultSet set) throws SQLException {
        int colCount = set.getMetaData().getColumnCount();

        int index = 1;
        while (set.next()) {
            System.out.println("------ Index [" + index + "] ------");
            for (int i = 1; i <= colCount; i++) {
                String name = set.getMetaData().getColumnName(i);
                String val = set.getString(i);

                System.out.println("Column[" + name + "]: " + val);
            }

            System.out.println("");
            index++;
        }
    }
}
