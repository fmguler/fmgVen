/*
 *  fmgVen - A Convention over Configuration Java ORM Tool
 *  Copyright 2010 Fatih Mehmet Güler
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.fmguler.ven.sample;

import com.fmguler.ven.Ven;
import com.fmguler.ven.sample.domain.SomeDomainObject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import liquibase.FileSystemFileOpener;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

/**
 * Demonstrates sample usage of fmgVen.
 * @author Fatih Mehmet Güler
 */
public class Sample {
    public static void main(String[] args) {
        //build the sample database
        buildDatabase();

        //save an object
        testSave();
        //delete an object
        testDelete();

        //rollback the sample database to original state
        rollbackDatabase();
    }

    /**
     * Test save an object
     */
    public static void testSave() {
        Ven ven = getVen();

        //insert
        SomeDomainObject obj = new SomeDomainObject();
        obj.setName("name");
        obj.setDescription("desc");
        obj.setDate(new Date());
        ven.save(obj);
        System.out.println(obj);

        //update
        obj.setName("name update");
        ven.save(obj);
        System.out.println(obj);
    }

    /**
     * Test delete an object
     */
    public static void testDelete() {
        Ven ven = getVen();
        ven.delete(1, SomeDomainObject.class);
    }

    /**
     * Test get an object by primary key
     */
    public static void testGet() {
        Ven ven = getVen();
        SomeDomainObject obj = (SomeDomainObject)ven.get(1, SomeDomainObject.class);
        System.out.println(obj);
    }

    /**
     * Test list the collection of objects
     */
    public static void testList() {
        Ven ven = getVen();
        List objList = ven.list(SomeDomainObject.class);
        System.out.println(objList);
    }

    /**
     * Test list the collection of objects by some criteria
     */
    public static void testListByCriteria() {
        Ven ven = getVen();
        List objList = ven.list(SomeDomainObject.class/*, criteria */);
        System.out.println(objList);
    }

    //---------------------------------------------------------
    private static Ven getVen() {
        Ven ven = new Ven();
        ven.setDataSource(getDataSource());
        ven.addDomainPackage("com.fmguler.ven.sample.domain").addDomainPackage("another.package");
        return ven;
    }

    private static DataSource getDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUsername("postgres");
        ds.setPassword("qwerty");
        ds.setUrl("jdbc:postgresql://127.0.0.1:5432/ven-test");
        return ds;
    }

    private static void buildDatabase() {
        try {
            Locale currLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(getDataSource().getConnection());
            Liquibase liquibase = new Liquibase("etc/test-db/test-db-changelog.xml", new FileSystemFileOpener(), database);
            liquibase.update("");
            Locale.setDefault(currLocale);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (JDBCException ex) {
            ex.printStackTrace();
        } catch (LiquibaseException ex) {
            ex.printStackTrace();
        }
    }

    private static void rollbackDatabase() {
        try {
            Locale currLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(getDataSource().getConnection());
            Liquibase liquibase = new Liquibase("etc/test-db/test-db-changelog.xml", new FileSystemFileOpener(), database);
            liquibase.rollback("tag-single-table", "");
            Locale.setDefault(currLocale);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (JDBCException ex) {
            ex.printStackTrace();
        } catch (LiquibaseException ex) {
            ex.printStackTrace();
        }
    }
}
