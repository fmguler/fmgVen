/*
 *  fmgVen - A Convention over Configuration Java ORM Tool
 *  Copyright 2011 Fatih Mehmet Güler
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
package com.fmguler.ven;

import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Database utilities for testing
 * 
 * @author Fatih Mehmet Güler
 */
public class LiquibaseUtil {
    /**
     * @return DataSource for the test database
     */
    public static DataSource getDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUsername("postgres");
        ds.setPassword("qwerty");
        ds.setUrl("jdbc:postgresql://127.0.0.1:5432/ven-test");
        return ds;
    }

    /**
     * Build the test database
     */
    public static void buildDatabase() {
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

    /**
     * Undo all changes in the test database
     */
    public static void rollbackDatabase(String tag) {
        try {
            Locale currLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(getDataSource().getConnection());
            Liquibase liquibase = new Liquibase("etc/test-db/test-db-changelog.xml", new FileSystemFileOpener(), database);
            liquibase.rollback(tag, "");
            Locale.setDefault(currLocale);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (JDBCException ex) {
            ex.printStackTrace();
        } catch (LiquibaseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Quick test
     */
    public static void main(String[] args) {
        //buildDatabase();
        //rollbackDatabase("tag-no-data");
        rollbackDatabase("tag-init");

    }
}
