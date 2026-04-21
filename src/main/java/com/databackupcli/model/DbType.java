package com.databackupcli.model;

public enum DbType {
    POSTGRES("PostgreSQL"),
    MYSQL("MySQL"),
    MONGODB("MongoDB");

    private final String displayName;

    DbType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DbType fromString(String value) {
        for (DbType t : values()) {
            if (t.name().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Unknown DB type: " + value);
    }
}
