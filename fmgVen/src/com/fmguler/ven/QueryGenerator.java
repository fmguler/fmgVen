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
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generates queries in the form of 'Convention over Configuration' of the specified class.
 * @author Fatih Mehmet Güler
 */
public class QueryGenerator {
    private Set domainPackages;
    private Set dbClasses;

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

    public String generateSelectQuery() {
        return null;
    }

    public String generateCountQuery() {
        return null;
    }

    public String generateInsertQuery(Object object) throws VenException {
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
     * Generates insert/update query
     * @return the insert update SQL query
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
        query.append(" where id = :id ;"); //TODO: remove the last comma
        return query.toString();
    }

    public String generateSequenceQuery(Object object) throws VenException {
        String objectName = Convert.toSimpleName(object.getClass().getName());
        String tableName = Convert.toDB(objectName);
        return "select nextval('" + tableName + "_id_seq');";
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void addDomainPackage(String domainPackage) {
        domainPackages.add(domainPackage);
    }
}
