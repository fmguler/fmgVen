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
package com.fmguler.ven.support;

import com.fmguler.ven.util.Convert;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Convert domain objects to Liquibase changeset xml
 * @author Fatih Mehmet Güler
 */
public class LiquibaseConverter {
    private Set domainPackages;
    private Map dbClasses;
    private List unhandledForeignKeyConstraints;
    private Set processedTables;
    private String author;
    private String schema = "public";
    private int changeSetId = 0;
    private static final int COLUMN_TYPE_VARCHAR_1000 = 0;
    private static final int COLUMN_TYPE_TEXT = 1;
    private static final int COLUMN_TYPE_DATE = 2;
    private static final int COLUMN_TYPE_BOOLEAN = 3;
    private static final int COLUMN_TYPE_INT = 4;
    private static final int COLUMN_TYPE_DOUBLE = 5;

    public LiquibaseConverter() {
        domainPackages = new HashSet();
        //the predefined database classes;
        dbClasses = new HashMap();
        dbClasses.put(Integer.class, new Integer(COLUMN_TYPE_INT));
        dbClasses.put(String.class, new Integer(COLUMN_TYPE_VARCHAR_1000));
        dbClasses.put(Date.class, new Integer(COLUMN_TYPE_DATE));
        dbClasses.put(Double.class, new Integer(COLUMN_TYPE_DOUBLE));
        dbClasses.put(Boolean.class, new Integer(COLUMN_TYPE_BOOLEAN));
        //implementation specific
        unhandledForeignKeyConstraints = new LinkedList();
        processedTables = new HashSet();
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL)resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList classes = new ArrayList();
        Iterator it = dirs.iterator();
        while (it.hasNext()) {
            File directory = (File)it.next();
            classes.addAll(findClasses(directory, packageName));
        }

        return (Class[])classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                assert file.getName().indexOf(".") != -1;
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                }
        }
        return classes;
    }

    /**
     * Add the domain packages that will be considered persistent
     * @param domainPackage the domain package
     */
    public void addDomainPackage(String domainPackage) {
        domainPackages.add(domainPackage);
    }

    /**
     * Set the author field in the changesets
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Set the changeSetId start (default 0)
     * @param changeSetId the id attribute in the changeset tag
     */
    public void setChangeSetIdStart(int changeSetId) {
        this.changeSetId = changeSetId;
    }

    /**
     * Set the schema to be added to liquibase tags
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Convert the added domain packages to liquibase changeset xml
     * @return changeset xml
     */
    public String convert() {
        StringBuffer liquibaseXml = new StringBuffer();
        Iterator it = domainPackages.iterator();
        while (it.hasNext()) {
            String domainPackage = (String)it.next();
            try {
                Class[] domainClasses = getClasses(domainPackage);
                for (int i = 0; i < domainClasses.length; i++) {
                    Class domainClass = domainClasses[i];
                    convertClass(liquibaseXml, domainClass);
                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return liquibaseXml.toString();
    }

    //convert a single class
    private void convertClass(StringBuffer liquibaseXml, Class domainClass) {
        String objectName = Convert.toSimpleName(domainClass.getName());
        String tableName = Convert.toDB(objectName);
        startTagChangeSet(liquibaseXml, author, "" + (changeSetId++));
        startTagCreateTable(liquibaseXml, schema, tableName);
        addIdColumn(liquibaseXml, tableName);
        List foreignKeyConstraints = new LinkedList();

        BeanWrapper wr = new BeanWrapperImpl(domainClass);
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();

        for (int i = 0; i < pdArr.length; i++) {
            Class fieldClass = pdArr[i].getPropertyType(); //field class
            String fieldName = pdArr[i].getName(); //field name
            String columnName = Convert.toDB(pdArr[i].getName()); //column name

            if (fieldName.equals("id")) {
                continue;
            }

            //direct database class (Integer, String, Date, etc)
            if (dbClasses.keySet().contains(fieldClass)) {
                addPrimitiveColumn(liquibaseXml, columnName, ((Integer)dbClasses.get(fieldClass)).intValue());

            }

            //many to one association (object property)
            if (fieldClass.getPackage() != null && domainPackages.contains(fieldClass.getPackage().getName())) {
                addPrimitiveColumn(liquibaseXml, columnName + "_id", COLUMN_TYPE_INT);

                //handle foreign key
                String referencedTableName = Convert.toDB(Convert.toSimpleName(fieldClass.getName()));
                Map fkey = new HashMap();
                fkey.put("baseTableName", tableName);
                fkey.put("baseColumnNames", columnName + "_id");
                fkey.put("referencedTableName", referencedTableName);
                if (processedTables.contains(referencedTableName)) foreignKeyConstraints.add(fkey);
                else unhandledForeignKeyConstraints.add(fkey);
            }

        }
        endTagCreateTable(liquibaseXml);
        endTagChangeSet(liquibaseXml);

        //mark table as processed
        processedTables.add(tableName);
        //add fkeys waiting for this table
        notifyUnhandledForeignKeyConstraints(liquibaseXml, tableName);
        //add fkeys not waiting for this table
        Iterator it = foreignKeyConstraints.iterator();
        while (it.hasNext()) {
            Map fkey = (Map)it.next();
            addForeignKeyConstraint(liquibaseXml, fkey);
        }
    }

    //private methods
    //--------------------------------------------------------------------------
    //start changeset tag
    private void startTagChangeSet(StringBuffer buffer, String author, String id) {
        buffer.append("<changeSet author=\"").append(author).append("\" id=\"").append(id).append("\">\n");
    }

    //end changeset tag
    private void endTagChangeSet(StringBuffer buffer) {
        buffer.append("</changeSet>\n");
    }

    //start createtable tag
    private void startTagCreateTable(StringBuffer buffer, String schema, String tableName) {
        buffer.append("\t<createTable schemaName=\"").append(schema).append("\" tableName=\"").append(tableName).append("\">\n");
    }

    //end createtable tag
    private void endTagCreateTable(StringBuffer buffer) {
        buffer.append("\t</createTable>\n");
    }

    //id column tag
    private void addIdColumn(StringBuffer buffer, String tableName) {
        buffer.append("\t\t<column autoIncrement=\"true\" name=\"id\" type=\"serial\">\n");
        buffer.append("\t\t\t<constraints nullable=\"false\" primaryKey=\"true\" primaryKeyName=\"").append(tableName).append("_pkey\"/>\n");
        buffer.append("\t\t</column>\n");
    }

    //primitive column tag
    private void addPrimitiveColumn(StringBuffer buffer, String columnName, int columnType) {
        buffer.append("\t\t<column name=\"" + columnName + "\" type=\"" + getDataType(columnType) + "\"/>\n");
    }

    //foreign key constraint tag
    private void addForeignKeyConstraint(StringBuffer buffer, Map fkey) {
        startTagChangeSet(buffer, author, "" + (changeSetId++));
        String baseTableName = (String)fkey.get("baseTableName");
        String baseColumnNames = (String)fkey.get("baseColumnNames");
        String referencedTableName = (String)fkey.get("referencedTableName");
        String constraintName = baseTableName + "_" + baseColumnNames + "_fkey";
        buffer.append("\t<addForeignKeyConstraint\n"
                + "\t\tconstraintName=\"" + constraintName + "\"\n"
                + "\t\tbaseTableSchemaName=\"" + schema + "\" baseTableName=\"" + baseTableName + "\" baseColumnNames=\"" + baseColumnNames + "\"\n"
                + "\t\treferencedTableSchemaName=\"" + schema + "\" referencedTableName=\"" + referencedTableName + "\" referencedColumnNames=\"id\"\n"
                + "\t\tdeferrable=\"false\" initiallyDeferred=\"false\" onDelete=\"CASCADE\" onUpdate=\"CASCADE\" />\n");
        endTagChangeSet(buffer);
    }

    //handle fkeys waiting for a table
    private void notifyUnhandledForeignKeyConstraints(StringBuffer buffer, String tableName) {
        Iterator it = unhandledForeignKeyConstraints.iterator();
        while (it.hasNext()) {
            Map fkey = (Map)it.next();
            if (fkey.get("referencedTableName").equals(tableName)) addForeignKeyConstraint(buffer, fkey);
        }

    }

    private String getDataType(int columnType) {
        //TODO: these are for postgresql, do it for all dbs, or make it generic.
        switch (columnType) {
            case COLUMN_TYPE_VARCHAR_1000:
                return "VARCHAR(1000)";
            case COLUMN_TYPE_TEXT:
                return "TEXT(2147483647)";
            case COLUMN_TYPE_DATE:
                return "TIMESTAMP WITHOUT TIME ZONE"; //DATE? DATETIME? TIME?
            case COLUMN_TYPE_BOOLEAN:
                return "BOOLEAN";
            case COLUMN_TYPE_INT:
                return "int"; //BIGINT?
            case COLUMN_TYPE_DOUBLE:
                return "DOUBLE";
        }
        return "";
    }
}
