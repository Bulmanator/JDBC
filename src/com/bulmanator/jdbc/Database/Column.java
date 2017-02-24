package com.bulmanator.jdbc.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Column {

    private String name;
    private String type;
    private boolean nullable;
    private boolean primary;
    private String reference;

    public Column(ResultSet set) throws SQLException {
        name = set.getString("COLUMN_NAME");
        type = set.getString("TYPE_NAME");
        nullable = set.getString("IS_NULLABLE").toLowerCase().equals("yes");
        primary = false;
        reference = "";
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public boolean isPrimary() { return primary; }

    public String getReference() { return reference; }

    public void setPrimary(boolean primary) { this.primary = primary; }

    public void setReference(String reference) { this.reference = reference; }

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
