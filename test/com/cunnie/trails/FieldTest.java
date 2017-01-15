package com.cunnie.trails;

import com.cunnie.trails.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by saint on 10/8/2016.
 */
public class FieldTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetDbName() throws Exception {
        Field field = new Field("email", "varchar");
        assertEquals("email", field.getDbName());
    }

    @Test
    public void testGetDbType() throws Exception {
        Field field = new Field("email", "varchar");
        assertEquals("varchar", field.getDbType());
    }

    @Test
    public void testSimpleJavaName() throws Exception {
        Field field = new Field("email", "varchar");
        assertEquals("email", field.getJavaName());
    }

    @Test
    public void testGetDbTypeInt() throws Exception {
        Field field = new Field("email", "int");
        assertEquals("int", field.getJavaType());
    }

    @Test
    public void testDbTypeVarchar() throws Exception {
        Field field = new Field("email", "varchar");
        assertEquals("String", field.getJavaType());
    }

    @Test
    public void testDbTypeDate() throws Exception {
        Field field = new Field("birthdate", "date");
        assertEquals("java.sql.Date", field.getJavaType());
    }

    @Test
    public void testDbTypeTimestamp() throws Exception {
        Field field = new Field("create_time", "timestamp");
        assertEquals("java.sql.Timestamp", field.getJavaType());
    }

    @Test
    public void testDbTypeDecimal() throws Exception {
        Field field = new Field("supplemental_liability", "decimal");
        assertEquals("java.math.BigDecimal", field.getJavaType());
    }

    @Test
    public void testDbTypeBit() throws Exception {
        Field field = new Field("optional_coverage", "bit");
        assertEquals("boolean", field.getJavaType());
    }

    @Test
    public void testSingleWordEnglishName() throws Exception {
        Field field = new Field("email", "int");
        assertEquals("Email", field.getEnglishName());
    }

    @Test
    public void testMultiWordEnglishName() throws Exception {
        Field field = new Field("first_name", "varchar");
        assertEquals("First Name", field.getEnglishName());
    }

    @Test
    public void testResolveResolvesAllWildcards() throws Exception {
        Field field = new Field("first_name", "varchar");
        String resolvedText = field.resolve("%%%FIELD_NAME%%% " +
                "%%%FIELD_DB_TYPE%%% " +
                "%%%FIELD_JAVA_NAME%%% " +
                "%%%FIELD_JAVA_TYPE%%% " +
                "%%%FIELD_ENGLISH_NAME%%%");
        assertEquals("first_name varchar firstName String First Name", resolvedText);
    }

}