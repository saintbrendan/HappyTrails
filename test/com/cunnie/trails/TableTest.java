package com.cunnie.trails;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by saint on 1/15/2017.
 */
public class TableTest {
    @Test
    public void confirmDbNameIsCorrect() throws Exception {
        Table table = new Table("name_of_table", new ArrayList<>());
        assertEquals("name_of_table", table.getDbName());
    }

    @Test
    public void getClassName() throws Exception {
        Table table = new Table("name_of_table", new ArrayList<>());
        assertEquals("NameOfTable", table.getClassName());
    }

    @Test
    public void getCamelClassName() throws Exception {
        Table table = new Table("name_of_table", new ArrayList<>());
        assertEquals("nameOfTable", table.getCamelClassName());
    }

    @Test
    public void getFields() throws Exception {
        Table table = new Table("name_of_table", Arrays.asList(new Field("field_name", "int")));
        assertEquals("name_of_table", table.getDbName());
    }

    @Test
    public void resolve() throws Exception {
        Table table = new Table("name_of_table", new ArrayList<>());
        String resolvedText = table.resolve("%%%TABLE_NAME%%% " +
                "%%%TABLE_CLASS%%% " +
                "%%%TABLE_CAMEL_CASE%%%");
        assertEquals("name_of_table NameOfTable nameOfTable", resolvedText);
    }

}