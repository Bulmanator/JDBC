package com.bulmanator.jdbc;

import com.bulmanator.jdbc.Database.Column;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class Main {


    public static void main(String[] args) {
        try {
            new Main().run();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void run() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection database = DriverManager.getConnection("jdbc:sqlite:Content/University.db");
        if(database == null) return;

        String spaces = "     ";

        // The Metadata of the entire database
        DatabaseMetaData dbMeta = database.getMetaData();

        ResultSet tables = dbMeta.getTables(null, null, "%", null);
        int tblIndex = 0;
        while(tables.next()) {
            String tblName = tables.getString("TABLE_NAME");

          //  System.out.println("[ " + tblName + " ]");


            ResultSet columns = dbMeta.getColumns(null, null, tblName, "%");
            ResultSet primaryKeys = dbMeta.getPrimaryKeys(database.getCatalog(), null, tblName);
            ResultSet foreignKeys = dbMeta.getImportedKeys(database.getCatalog(), null, tblName);

           // System.out.println(" -- [ Columns ]");

            ArrayList<Column> cols = new ArrayList<>();

            while (columns.next()) {
                String name = columns.getString("COLUMN_NAME");
                String type = columns.getString("TYPE_NAME");

                cols.add(new Column(columns));

              //  System.out.println(" - " + name + " " + type);
            }

           // System.out.println("\n -- [ Primary Keys ]");
            while (primaryKeys.next()) {
                String name = primaryKeys.getString("COLUMN_NAME");

                for(Column column : cols) {
                    if(name.equals(column.getName())) {
                        column.setPrimary(true);
                    }
                }

              //  System.out.println(" - " + name);
            }

           // System.out.println("\n -- [ Foreign Keys ]");

            while (foreignKeys.next()) {
                String referenceKey = foreignKeys.getString("PKCOLUMN_NAME");
                String referenceTable = foreignKeys.getString("PKTABLE_NAME");

                String foreignKey = foreignKeys.getString("FKCOLUMN_NAME");

                for(Column column : cols) {
                    if(foreignKey.equals(column.getName())) {
                        column.setReference("FOREIGN KEY (" + foreignKey + ") REFERENCES "
                                + referenceTable + "(" + referenceKey + ")");
                    }
                }

            //    System.out.println("FOREIGN KEY(" + foreignKey + ") REFERENCES " + referenceTable + "(" + referenceKey + ")");
            }
            tblIndex++;

            System.out.println("");

            String query = "CREATE TABLE " + tblName + "(\n";
            for(Column column : cols) {
                query += (spaces + column.toString()) + "\n";
            }
            query += ");";


            System.out.println("\n\bQuery: " + query + "\n");
        }

    }

   /* private void run() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:Content/University.db");
        if(connection == null) return;

        DatabaseMetaData metaData = connection.getMetaData();

        String catalog = null;
        String schema = null;
        String tableNamePattern = null;
        String[] types = null;

        ResultSet set = metaData.getTables(null, null, null, null);

        int count = 1;

        ArrayList<String> tableNames = new ArrayList<>();

        while (set.next()) {
            String name = set.getString("TABLE_NAME");
            System.out.println("Table [" + count + "]: " + name);

            tableNames.add(name);

            count++;
        }
        System.out.println("");

        ArrayList<Column> columns = new ArrayList<>();




        for(String name : tableNames) {
            ResultSet table = metaData.getColumns(null, null, name, "%");
            ResultSet primary = metaData.getPrimaryKeys(null, null, name);
            ResultSet foreign = metaData.getExportedKeys(connection.getCatalog(), null, name);

        //    printResultSet(foreign);
            count = 0;
            while (table.next()) {
                ResultSetMetaData md = table.getMetaData();

                ArrayList<String> references = new ArrayList<>();
                while (foreign.next()) {
                    String fkTable = foreign.getString("FKTABLE_NAME");
                    System.out.println("FKTABLE: " + fkTable + " TABLE: " + name);
                    if(fkTable.equals(name)) {
                        System.out.println("Matching!");
                        String ref = "FOREIGN KEY (" + foreign.getString("FKCOLUMN_NAME") + ") REFERENCES " + fkTable + "(" + foreign.getString("PKCOLUMN_NAME") + ")";
                        references.add(ref);
                    }
                }


                boolean added = false;
                for(int i = 1; i <= colCount; i++) {
                    String colName = md.getColumnName(i);
                    System.out.println("Column[" + colName + "]: " + table.getString(i));
                }

               while (primary.next()) {
                   if(primary.getString("COLUMN_NAME").equals(table.getString("COLUMN_NAME"))) {
                       if(references.size() > 0) {
                           columns.add(new Column(table, true, references.get(0)));
                       }
                       else {
                           columns.add(new Column(table, true));
                       }
                       added = true;
                   }
               }

                if(!added) {
                    if(references.size() > 0) {
                        columns.add(new Column(table, false, references.get(0)));
                    }
                    else {
                        columns.add(new Column(table, false));
                    }
               }

                System.out.print("Column Name: " + table.getString("COLUMN_NAME") + " Type: " + table.getString("TYPE_NAME"));
                if(table.getString("IS_NULLABLE").toLowerCase().equals("yes")) {
                    System.out.println(" NOT NULL");
                }
                else {
                    System.out.println("");
                }

              //  String column = table.getString("COLUMN_NAME");
              //  int type = table.getInt("type");

              //  System.out.println("Column [" + count + "]: " + column);
             //   count++;
                count++;
            }
            System.out.println("Count: " + count);

            String query = "CREATE TABLE " + name + "(";
            for(int i = 0; i < columns.size() - 1; i++) {
                query += columns.get(i).toString() + ", ";
            }

            query += columns.get(columns.size() - 1).toString();

            query += ");";

            System.out.println("Query: " + query);
            columns.clear();

            System.out.println("");
        }

    }*/


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

    private void printResultSet(ResultSet set) throws SQLException {
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
