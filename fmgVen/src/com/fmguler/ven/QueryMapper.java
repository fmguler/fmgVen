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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Maps the result of the query generated in the form of 'Convention over Configuration' to the specified object.
 * @author Fatih Mehmet Güler
 */
public class QueryMapper {
    private NamedParameterJdbcTemplate template;
    private Set domainPackages;
    private Set dbClasses;
    private boolean debug = false;

    public QueryMapper() {
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
     * Executes the specified query setting the specified parameters,
     * and maps the results to the instances of the specified objectClass.
     * @param query select SQL query in the conventional format
     * @param parameters named query parameter values
     * @param objectClass the type of the object to be mapped
     * @return the list of mapped objects
     */
    public List list(String query, Map parameters, final Class objectClass) {
        long t1 = System.currentTimeMillis();
        final List results = new LinkedList();
        final String tableName = Convert.toDB(Convert.toSimpleName(objectClass.getName()));
        final Set columns = new HashSet();

        template.query(query, parameters, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                enumerateColumns(columns, rs);
                mapRecursively(rs, columns, tableName, objectClass, results);
            }
        });

        System.out.println("Ven - list time = " + (System.currentTimeMillis() - t1));
        return results;
    }

    //--------------------------------------------------------------------------
    //PRIVATE METHODS
    //recursively map the resultSet to the object and add to the list
    protected void mapRecursively(ResultSet rs, Set columns, String tableName, Class objectClass, List parentList) {
        try {
            if (!columns.contains(tableName + "_id")) return; //this object does not exist in the columns
            Object id = rs.getObject(tableName + "_id");
            if (id == null) return; //this object exists in the columns but null, probably because of left join

            //create bean wrapper for the object class
            BeanWrapper wr = new BeanWrapperImpl(objectClass); //already caches class introspection (CachedIntrospectionResults.forClass())
            wr.setPropertyValue("id", id); //set the id property
            Object object = wr.getWrappedInstance();
            boolean map = true;

            //check if this object exists in the parent list (since SQL joins are cartesian products, do not create new object if this row is just the same as previous)
            for (Iterator it = parentList.iterator(); it.hasNext();) {
                Object objectInList = (Object)it.next();
                if (objectIdEquals(objectInList, id)) {
                    wr.setWrappedInstance(objectInList); //already exists in the list, use that instance
                    map = false; // and do not map again
                    break;
                }
            }
            if (map) parentList.add(object); //could not find in the parent list, add the new object

            PropertyDescriptor[] pdArr = wr.getPropertyDescriptors();
            for (int i = 0; i < pdArr.length; i++) {
                PropertyDescriptor pd = pdArr[i];
                Class fieldClass = pd.getPropertyType(); //field class
                String fieldName = Convert.toDB(pd.getName()); //field name
                Object fieldValue = wr.getPropertyValue(pd.getName());
                String columnName = tableName + "_" + fieldName;

                //database class (primitive property)
                if (map && dbClasses.contains(fieldClass)) {
                    if (columns.contains(columnName)) {
                        if (debug) System.out.println(">>field is found: " + columnName);
                        wr.setPropertyValue(pd.getName(), rs.getObject(columnName));
                    } else {
                        if (debug) System.out.println("--field not found: " + columnName);
                    }
                }

                //many to one association (object property)
                if (map && fieldClass.getPackage() != null && domainPackages.contains(fieldClass.getPackage().getName())) {
                    if (columns.contains(columnName + "_id")) {
                        if (debug) System.out.println(">>object is found " + columnName);
                        List list = new ArrayList(1); //we know there will be single result
                        mapRecursively(rs, columns, columnName, fieldClass, list);
                        if (list.size() > 0) wr.setPropertyValue(pd.getName(), list.get(0));
                    } else {
                        if (debug) System.out.println("--object not found: " + columnName);
                    }
                }

                //one to many association (list property)
                if (fieldValue instanceof List) { //Note: here recurring row's list property is mapped and add to parent's list
                    if (columns.contains(columnName + "_id")) {
                        Class elementClass = VenList.findElementClass((List)fieldValue);
                        if (debug) System.out.println(">>list is found " + columnName);
                        mapRecursively(rs, columns, columnName, elementClass, (List)fieldValue);
                    } else {
                        if (debug) System.out.println("--list not found: " + columnName);
                    }
                }
            }
        } catch (Exception ex) {
            if (debug) {
                System.out.println("Ven - error while mapping row; ");
                ex.printStackTrace();
            }
        }
    }

    //enumerate columns from the resultset
    private void enumerateColumns(Set columns, ResultSet rs) throws SQLException {
        if (!columns.isEmpty()) return;
        for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
            columns.add(rs.getMetaData().getColumnName(i));
        }
    }

    //check if the specified objects are the same entity (according to id fields)
    private boolean objectIdEquals(Object object, Object id) {
        //return obj1.equals(obj2); //objects need to implement equals()
        //TODO: more efficient (invoke getId method)
        BeanWrapper wr = new BeanWrapperImpl(object);
        return id.equals(wr.getPropertyValue("id"));
    }

    //--------------------------------------------------------------------------
    //SETTERS
    /**
     * @param dataSource used for accessing database
     */
    public void setDataSource(DataSource dataSource) {
        if (dataSource == null) throw new RuntimeException("fmgVen - DataSource cannot be null");
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Add the domain packages that will be considered persistent
     * @param domainPackage the domain package
     */
    public void addDomainPackage(String domainPackage) {
        domainPackages.add(domainPackage);
    }

    /**
     * Set debug mode, true will log all debug messages to System.out
     * <p>
     * Note: Use debug mode to detect problems only. It is not a general purpose logging mode.
     * @param debug set true to enable debug mode
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
