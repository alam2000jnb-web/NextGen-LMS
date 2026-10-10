package com.lms.model;

import java.io.Serializable;

public class SystemSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String value;
    private String description;
    private String category; // "GENERAL", "ENROLLMENT", "SECURITY", "NOTIFICATION"

    public SystemSetting() {}

    public SystemSetting(String key, String value, String description, String category) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.category = category;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return key + " = " + value;
    }
}
