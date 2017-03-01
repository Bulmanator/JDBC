package com.bulmanator.jdbc.Database;

import com.bulmanator.jdbc.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Column {

    private String name;
    private String type;
    private boolean nullable;
    private boolean primary;
    private boolean hasReference;
    private String reference;

    public Column(ResultSet set) throws SQLException {

        name = set.getString("COLUMN_NAME");
        type = set.getString("TYPE_NAME");
        nullable = set.getString("NULLABLE").equals("1");
        primary = false;
        reference = "";
        hasReference = false;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public boolean isPrimary() { return primary; }

    public boolean isNullable() { return nullable; }

    public boolean hasReference() { return hasReference; }

    public String getReference() { return reference; }

    public void setPrimary() { this.primary = true; }

    public void setReference(String reference) {
        this.reference = reference;
        hasReference = true;
    }

    @Override
    public String toString() {
        String val = "";

        val += name + "";
        val += " " + type;
        val += (nullable ? "" : " NOT NULL");
        val += (primary ? " PRIMARY KEY" : "");
        val += (!reference.equals("") ?  " " + reference : "");

        return val;
    }

}
