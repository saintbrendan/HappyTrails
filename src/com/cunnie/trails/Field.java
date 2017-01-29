package com.cunnie.trails;

import java.security.InvalidParameterException;
import java.util.*;

public class Field {

    private static final List<String> NON_EDITABLE_FIELDS = Arrays.asList("id", "version",
            "create_time", "create_user",
            "update_time", "update_user");
    private static String USER_NUMERIC_FORM =
            "            <div class=\"form-group\">\n"+
            "                <label class=\"col-sm-2 control-label\">%s:</label>\n"+
            "                <div class=\"col-sm-10\">\n"+
            "                    <input type=\"text\" class=\"form-control\" th:onkeypress=\"'return event.charCode &gt;= 48 &amp;&amp; event.charCode &lt;= 57'\"  th:field=\"*{%s}\"/>\n"+
            "                </div>\n"+
            "            </div>\n";
    private static String USER_TEXT_FORM =
            "            <div class=\"form-group\">\n"+
            "                <label class=\"col-sm-2 control-label\">%s:</label>\n"+
            "                <div class=\"col-sm-10\">\n"+
            "                    <input type=\"text\" class=\"form-control\" th:field=\"*{%s}\"/>\n"+
            "                </div>\n"+
            "            </div>\n";
    private static String USER_DATE_FORM =
            "            <div class=\"form-group\">\n" +
            "                <label class=\"col-sm-2 control-label\">%s:</label>\n" +
            "                <div class=\"col-sm-10\">\n" +
            "                    <input type=\"date\" class=\"form-control\" th:field=\"*{%s}\"/>\n" +
            "                </div>\n" +
            "            </div>\n";
    private static String USER_BOOLEAN_FORM =
            "            <div class=\"form-group\">\n" +
            "                <label class=\"col-sm-2 control-label\">%s:</label>\n" +
            "                <div class=\"col-sm-10\">\n" +
            "                    <input  type='checkbox'  th:field=\"*{%s}\" />\n" +
            "                </div>\n" +
            "            </div>\n";


    //  Number field needs      th:onkeypress="'return event.charCode &gt;= 48 &amp;&amp; event.charCode &lt;= 57'"
    private String dbName;
    private String dbType;
    private String javaName;
    private String javaType;
    private String pascalCase;
    private String englishName;
    private Integer characterMaximumLength = null;
    private Integer numericPrecision = null;
    private Integer numericScale = null;
    private String predomain = ""; // Used in the domains/class.java files for id and version fields.

    // datatype lookup
    private static final Map<String, String> javaTypes;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("int", "int");
        map.put("varchar", "String");
        map.put("bit", "boolean");
        map.put("binary", "byte[]");
        map.put("blob", "byte[]");
        map.put("longblob", "byte[]");
        map.put("date", "java.sql.Date");
        map.put("decimal", "java.math.BigDecimal");
        map.put("numeric", "java.math.BigDecimal");
        map.put("timestamp", "java.sql.Timestamp");
        javaTypes = Collections.unmodifiableMap(map);
    }

    /// TODO: Update tests to not use this constructor.  And then remove this constructor.
    public Field(String dbName, String dbType) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.javaName = dbName;

        initializeField();
    }

    public Field(String dbName, String dbType, int characterMaximumLength, int numericPrecision, int numericScale) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.javaName = dbName;
        this.characterMaximumLength = characterMaximumLength;
        this.numericPrecision = numericPrecision;
        this.numericScale = numericScale;

        initializeField();
    }

    private void initializeField() {
        // Replace _ and capitalize first letter of each word to Englishize
        // e.g.  first_name  -->  First Name
        String[] names = dbName.split("_");
        ArrayList<String> propercaseNames = new ArrayList<>();
        for (String name: names) {
            char[] namechars = name.toCharArray();
            namechars[0] = Character.toUpperCase(namechars[0]);
            propercaseNames.add(String.valueOf(namechars));
        }
        englishName = String.join(" ", propercaseNames);
        pascalCase = String.join("", propercaseNames);

        propercaseNames.set(0, names[0]);
        javaName = String.join("", propercaseNames);
        javaType = javaTypes.get(dbType.toLowerCase());
        if (javaType == null) {
            /// TODO: log but don't throw exception here
            throw new InvalidParameterException(dbName + "'s dbType " + dbType + " does not have a matching java type!");
        }

        if ("id".equals(dbName)) {
            predomain = "@Id\n" +
                    "    @GeneratedValue(strategy = GenerationType.AUTO)";
        } else if ("version".equals(dbName)) {
            predomain = "@Version";
        }
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbType() {
        return dbType;
    }

    public String getJavaName() {
        return javaName;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getPascalCase() {
        return pascalCase;
    }

    public String getPredomain() {
        return predomain;
    }

    /// TODO:  delete all this
    public String toFormHtml() {
        if (NON_EDITABLE_FIELDS.contains(getDbName())){
            return "            <!-- " + getDbName() + " is not directly editable by the UI -->\n";
        }
        // TODO: think about refactoring this to separate classes.
        switch (dbType) {
            case "date":
                return String.format(USER_DATE_FORM, getEnglishName(), getDbName());
            case "bit":
                return String.format(USER_BOOLEAN_FORM, getEnglishName(), getDbName());
            case "numeric":
            case "decimal":
                return String.format(USER_NUMERIC_FORM, getEnglishName(), getDbName());
            default:
                return String.format(USER_TEXT_FORM, getEnglishName(), getDbName());
        }
    }

    public String resolve(String sourceFileContents) {
        return sourceFileContents.replace("%%%FIELD_NAME%%%", this.getDbName())
                .replace("%%%FIELD_DB_TYPE%%%", this.getDbType())
                .replace("%%%FIELD_JAVA_NAME%%%", this.getJavaName())
                .replace("%%%FIELD_JAVA_TYPE%%%", this.getJavaType())
                .replace("%%%FIELD_JAVA_PASCAL_CASE%%%", this.getPascalCase())
                .replace("%%%FIELD_ENGLISH_NAME%%%", this.getEnglishName());
    }

}
