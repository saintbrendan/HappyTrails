package com.cunnie.trails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by saint on 1/15/2017.
 */
public class Table {
    private String dbName;
    private String englishName;
    private String className;
    private String camelClassName;
    private Collection<Field> fields;

    public Table(String dbName, Collection<Field> fields) {
        this.dbName = dbName;
        this.fields = new ArrayList<>(fields);  // Field is immutable

        /// resolve duplicated code with Field.initializeField() to get Pascal and camelCase names
        String[] names = dbName.split("_");
        ArrayList<String> propercaseNames = new ArrayList<>();
        for (String name: names) {
            char[] namechars = name.toCharArray();
            namechars[0] = Character.toUpperCase(namechars[0]);
            propercaseNames.add(String.valueOf(namechars));
        }
        this.className = String.join("", propercaseNames);
        this.englishName = String.join(" ", propercaseNames);
        propercaseNames.set(0, names[0]);
        this.camelClassName = String.join("", propercaseNames);
    }

    public String getDbName() {
        return dbName;
    }

    public String getClassName() {
        return className;
    }

    public String getCamelClassName() {
        return camelClassName;
    }

    public String getEnglishName() {
        return englishName;
    }
    public CharSequence getEnglishNamePlural() {
        return englishName + 's';  /// TODO:  Use English Inflector https://github.com/atteo/evo-inflector/blob/master/src/main/java/org/atteo/evo/inflector/English.java
    }

    public Collection<Field> getFields()
    {
        return fields;
    }

    public String resolve(String sourceFileText) {
        return sourceFileText
                .replace("%%%TABLE_NAME%%%", this.getDbName())
                .replace("%%%TABLE_CLASS%%%", this.getClassName())
                .replace("%%%TABLE_CAMEL_CASE%%%", this.getCamelClassName());
    }

}
