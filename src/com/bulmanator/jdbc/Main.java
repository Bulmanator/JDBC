package com.bulmanator.jdbc;

import com.bulmanator.jdbc.Database.Column;
import com.bulmanator.jdbc.Database.Table;
import javafx.scene.control.Tab;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Main {


    public static void main(String[] args) {
        try {
            new Main().run(args);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void run(String[] args) throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection database = DriverManager.getConnection("jdbc:sqlite:Content/" + args[0]);
        if(database == null) return;

        // The Metadata of the entire database
        DatabaseMetaData metadata = database.getMetaData();

        ResultSet tableData = metadata.getTables(null, null, null, new String[] { "TABLE" });

        ArrayList<Table> tables = new ArrayList<>();
        int tableIndex = 0;

        while(tableData.next()) {
            String tableName = tableData.getString("TABLE_NAME");
            tables.add(new Table(tableName, metadata));

     //       System.out.println(tables.get(tableIndex).getCreateQuery() + "\n");
            tableIndex++;
        }

        tables.sort(new Comparator<Table>() {
            @Override
            public int compare(Table t1, Table t2) {
                return t1.hasForeign() ? t2.hasForeign() ? 0 : 1 : -1;
            }
        });

        for(Table table : tables) {
            System.out.println(table.getCreateQuery() + "\n");
        }

        Statement statement = database.createStatement();

        for(Table table : tables) {
            ResultSet data = statement.executeQuery("SELECT * FROM " + table.getName());

            Collection<Column> columns = table.getColumns();
            System.out.println("Table: " + table.getName());
            while (data.next()) {
                String insert = "INSERT INTO " + table.getName() + " VALUES (";
                for(Column column : columns) {
                    if(column.getType().toLowerCase().contains("varchar")) {
                        String value = data.getString(column.getName());
                        insert += ("'" + value + "', ");
                    }
                    else if(column.getType().toLowerCase().contains("int")) {
                        int value = data.getInt(column.getName());
                        insert += (value) + ", ";
                    }
                }

                insert = insert.substring(0, insert.lastIndexOf(","));
                insert += ");";
                System.out.println(insert);
            }
            System.out.println();
        }

    }

   private void dumpResultSet(ResultSet set, String label, String path) {
       try {

           FileWriter writer = new FileWriter(path, false);
           PrintWriter printer = new PrintWriter(writer);

           printer.println(label + " {");
           int colCount = set.getMetaData().getColumnCount();
           int index = 1;

           String space = "    ";

           while (set.next()) {
               printer.println(space + "----- Index [" + index + "] -----");
               for(int i = 1; i <= colCount; i++) {
                   String name = set.getMetaData().getColumnName(i);
                   String val = set.getString(i);

                   printer.println(space + "Column[ " + name + " ]: " + val);
               }

               printer.println();
               index++;
           }

           printer.println("}");

           printer.close();
       }
       catch (IOException | SQLException ex) {
           ex.printStackTrace();
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
