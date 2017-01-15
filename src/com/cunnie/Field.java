package com.cunnie;

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
    private String englishName;

    // datatype lookup
    private static final Map<String, String> javaTypes;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("int", "int");
        map.put("varchar", "String");
        map.put("bit", "boolean");
        map.put("date", "java.sql.Date");
        map.put("decimal", "java.math.BigDecimal");
        map.put("numeric", "java.math.BigDecimal");
        map.put("timestamp", "java.sql.Timestamp");
        javaTypes = Collections.unmodifiableMap(map);
    }

    public Field(String dbName, String dbType) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.javaName = dbName;

        // Replace _ and capitalize first letter of each word to Englishize
        // e.g.  first_name  -->  First Name
        String[] names = dbName.split("_");
        ArrayList<String> englishnames = new ArrayList<>();
        for (String name: names) {
            char[] namechars = name.toCharArray();
            namechars[0] = Character.toUpperCase(namechars[0]);
            englishnames.add(String.valueOf(namechars));
        }
        englishName = String.join(" ", englishnames);

        javaType = javaTypes.get(dbType);
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
}
