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
package com.fmguler.ven;

import com.fmguler.ven.util.Convert;
import com.fmguler.ven.util.VenList;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generates queries in the form of 'Convention over Configuration' for the specified objects.
 * @author Fatih Mehmet Güler
 */
public class QueryGenerator {
    private Set domainPackages;
    private Set dbClasses;
    private boolean debug = true;

    public QueryGenerator() {
        domainPackages = new HashSet();
        dbClasses = new HashSet();
        //the predefined database classes;
        this.dbClasses.add(Integer.class);
        this.dbClasses.add(String.class);
        this.dbClasses.add(Date.class);
        this.dbClasses.add(Double.class);
        this.dbClasses.add(Boolean.class);
    }

    /**
     * Generates select query for the specified object class and specified joins.
     * @param joins set of joins that the query will contain
     * @param objectClass the object class to select from
     * @return the select SQL query
     */
    public String generateSelectQuery(Class objectClass, Set joins) {
        long t1 = System.currentTimeMillis();
        String objectName = Convert.toSimpleName(objectClass.getName());
        String tableName = Convert.toDB(objectName);
        StringBuffer selectClause = new StringBuffer("select ");
        StringBuffer fromClause = new StringBuffer("from " + tableName);
        generateRecursively(0, tableName, objectName, objectClass, joins, selectClause, fromClause);
        selectClause.append(" 1=1");
        if (debug) System.out.println("Ven - query generation time = " + (System.currentTimeMillis() - t1));
        return selectClause.toString() + " \n" + fromClause.toString();
    }

    public String generateCountQuery() {
        return null;
    }

    /**
     * Generates insert query for the specified object
     * @param object the object to generate insert query for
     * @return the insert SQL query
     */
    public String generateInsertQuery(Object object) {
        BeanWrapper wr = new BeanWrapperImpl(object);
        String objectName = Convert.toSimpleName(object.getClass().getName());
        String tableName = Convert.toDB(objectName);
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();

        //generate insert query
        StringBuffer query = new StringBuffer("insert into " + tableName + "(");
        StringBuffer values = new StringBuffer(" values(");
        for (int i = 0; i < pdArr.length; i++) {
            Class fieldClass = pdArr[i].getPropertyType(); //field class
            String columnName = Convert.toDB(pdArr[i].getName()); //column name
            String fieldName = pdArr[i].getName(); //field name
            //if (fieldName.equals("id")) continue; //remove if it does not break the sequence
            if (dbClasses.contains(fieldClass)) { //direct database field (Integer,String,Date, etc)
                query.append(columnName);
                query.append(",");
                values.append(":").append(fieldName);
                values.append(",");
            }
            if (fieldClass.getPackage() != null && domainPackages.contains(fieldClass.getPackage().getName())) { //object
                query.append(Convert.toDB(fieldName)).append("_id");
                query.append(",");
                values.append(":").append(fieldName).append(".id");
                values.append(",");
            }
        }
        query.deleteCharAt(query.length() - 1);
        query.append(")");
        values.deleteCharAt(values.length() - 1);
        values.append(");");
        query.append(values);

        return query.toString();
    }

    /**
     * Generates update query for the specified object
     * @param object the object to generate update query for
     * @return the update SQL query
     */
    public String generateUpdateQuery(Object object) throws VenException {
        BeanWrapper wr = new BeanWrapperImpl(object);
        String objectName = Convert.toSimpleName(object.getClass().getName());
        String tableName = Convert.toDB(objectName);
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();

        StringBuffer query = new StringBuffer("update " + tableName + " set ");
        for (int i = 0; i < pdArr.length; i++) {
            Class fieldClass = pdArr[i].getPropertyType(); //field class
            String columnName = Convert.toDB(pdArr[i].getName()); //column name
            String fieldName = pdArr[i].getName(); //field name
            if (dbClasses.contains(fieldClass)) { //direct database field (Integer,String,Date, etc)
                query.append(columnName).append("=:").append(fieldName);
                query.append(",");
            }
            if (fieldClass.getPackage() != null && domainPackages.contains(fieldClass.getPackage().getName())) { //object
                query.append(columnName).append("_id=:").append(fieldName).append(".id");
                query.append(",");
            }
        }
        query.deleteCharAt(query.length() - 1);
        query.append(" where id = :id ;");
        return query.toString();
    }

    /**
     * Generates delete query for the specified object class
     * @param objectClass the object class to generate query for
     * @return the delete SQL query
     */
    public String generateDeleteQuery(Class objectClass) {
        StringBuffer query = new StringBuffer();
        query.append("delete from ").append(Convert.toDB(Convert.toSimpleName(objectClass.getName()))).append(" where id = :id;");
        return query.toString();
    }

    /**
     * Generates sequence query for the specified object
     * @param object the objectc to generate sequence query for
     * @return the SQL query to select next id
     */
    public String generateSequenceQuery(Object object) throws VenException {
        String objectName = Convert.toSimpleName(object.getClass().getName());
        String tableName = Convert.toDB(objectName);
        return "select nextval('" + tableName + "_id_seq');";
    }

    //--------------------------------------------------------------------------
    //PRIVATE METHODS
    //recursively generate select query
    private void generateRecursively(int level, String tableName, String objectPath, Class objectClass, Set joins, StringBuffer selectClause, StringBuffer fromClause) {
        BeanWrapper wr = new BeanWrapperImpl(objectClass);
        PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();

        for (int i = 0; i < pdArr.length; i++) {
            Class fieldClass = pdArr[i].getPropertyType(); //field class
            String fieldName = pdArr[i].getName(); //field name
            Object fieldValue = wr.getPropertyValue(fieldName);
            String columnName = Convert.toDB(pdArr[i].getName()); //column name

            //direct database class (Integer, String, Date, etc)
            if (dbClasses.contains(fieldClass)) {
                selectClause.append(tableName).append(".").append(columnName).append(" as ").append(tableName).append("_").append(columnName); //column
                selectClause.append(", ");
            }

            //many to one association (object property)
            if (fieldClass.getPackage() != null && domainPackages.contains(fieldClass.getPackage().getName()) && joinsContain(joins, objectPath + "." + fieldName)) {
                String joinTableAlias = tableName + "_" + columnName; //alias for table to join since there can be multiple joins to the same table
                String joinTable = Convert.toDB(Convert.toSimpleName(fieldClass.getName())); //table to join
                fromClause.append(" left join ").append(joinTable).append(" ").append(joinTableAlias);
                fromClause.append(" on ").append(joinTableAlias).append(".id = ").append(tableName).append(".").append(columnName).append("_id");
                generateRecursively(++level, joinTableAlias, objectPath + "." + fieldName, fieldClass, joins, selectClause, fromClause);
            }

            //one to many association (list property)
            if (fieldValue instanceof List && joinsContain(joins, objectPath + "." + fieldName)) {
                Class elementClass = VenList.findElementClass((List)fieldValue);
                String joinTableAlias = tableName + "_" + columnName; //alias for table to join since there can be multiple joins to the same table
                String joinTable = Convert.toDB(Convert.toSimpleName(elementClass.getName())); //table to join
                String joinField = Convert.toDB(findJoinField((List)fieldValue)); //field to join
                fromClause.append(" left join ").append(joinTable).append(" ").append(joinTableAlias);
                fromClause.append(" on ").append(joinTableAlias).append(".").append(joinField).append("_id = ").append(tableName).append(".id");
                generateRecursively(++level, joinTableAlias, objectPath + "." + fieldName, elementClass, joins, selectClause, fromClause);
            }
        }
    }

    //check if the joins contain the specified join
    private boolean joinsContain(Set joins, String join) {
        Iterator it = joins.iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            if (str.startsWith(join)) {
                if (str.length() == join.length()) return true;
                else if (str.charAt(join.length()) == '.') return true;
            }
        }
        return false;
    }

    //return the join field of the elements in the list
    private String findJoinField(List list) {
        if (list instanceof VenList) {
            return ((VenList)list).getJoinField();
        } else {
            //find according to 1.5 generic or some convention (e.g. parent_obj_id)
            return null;
        }
    }

    //--------------------------------------------------------------------------
    //SETTERS
    /**
     * Add the domain packages that will be considered persistent
     * @param domainPackage the domain package
     */
    public void addDomainPackage(String domainPackage) {
        domainPackages.add(domainPackage);
    }
}
