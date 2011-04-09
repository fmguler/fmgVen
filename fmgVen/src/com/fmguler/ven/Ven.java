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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The main class for data access
 * @author Fatih Mehmet Güler
 */
public class Ven {
    private NamedParameterJdbcTemplate template;
    private QueryGenerator generator;
    private QueryMapper mapper;
    private boolean debug = true;

    public Ven() {
        generator = new QueryGenerator();
        mapper = new QueryMapper();
    }

    public int count() {
        return 0;
    }

    /**
     * Get the object with the specified id, of the specified objectClass type.
     * <p>
     * By default none of the associations will be retrieved.
     * To include the object associations (retrieve the object graph) joins should be specified, e.g.
     * <code>SomeObject.anotherObject</code>
     * 
     * @param id the id of the object to be retrieved
     * @param objectClass the class of the object to be retrieved
     * @param joins the set of object graphs to be included with the object
     * @return the retrieved object including specified associations
     */
    public Object get(int id, Class objectClass, Set joins) {
        String query = generator.generateSelectQuery(objectClass, joins);
        query += " where 1=1 and " + Convert.toDB(Convert.toSimpleName(objectClass.getName())) + ".id = :___id ";

        Map paramMap = new HashMap();
        paramMap.put("___id", new Integer(id));
        if (debug) System.out.println("Ven - SQL: " + query);

        List result = mapper.list(query, paramMap, objectClass);
        if (result.isEmpty()) return null;
        if (result.size() > 1) System.out.println("Ven - WARNING >> get(id) returns more than one row");
        return result.get(0);
    }

    /**
     * List the objects of the specified objectClass type.
     * <p>
     * By default none of the associations will be retrieved.
     * To include the object associations (retrieve the object graph) joins should be specified, e.g.
     * <code>SomeObject.anotherObject</code>
     * 
     * @param objectClass the class of the objects to be retrieved
     * @param joins the set of object graphs to be included with objects
     * @return the list of objects including specified associations
     */
    public List list(Class objectClass, Set joins) {
        String query = generator.generateSelectQuery(objectClass, joins);

        Map paramMap = new HashMap();
        if (debug) System.out.println("Ven - SQL: " + query);

        List result = mapper.list(query, paramMap, objectClass);
        return result;
    }

    /**
     * List the objects of the specified objectClass type, filtering according to some criteria.
     * <p>
     * By default none of the associations will be retrieved.
     * To include the object associations (retrieve the object graph) joins should be specified, e.g.
     * <code>SomeObject.anotherObject</code>
     * 
     * @param objectClass the class of the objects to be retrieved
     * @param joins the set of object graphs to be included with objects
     * @param criteria to filter and order the result according to some criteria
     * @return the list of objects including the specified associations filtered according to the specified criteria
     */
    public List list(Class objectClass, Set joins, Criteria criteria) {
        String query = generator.generateSelectQuery(objectClass, joins);
        query += " where 1=1 " + criteria.criteriaStringToSQL() + " and " + criteria.criteriaToSQL();

        if (debug) System.out.println("Ven - SQL: " + query);

        List result = mapper.list(query, criteria.getParameters(), objectClass);
        return result;
    }

    /**
     * Save the object. If it has a non null (or non zero) "id" property it will be updated.
     * It will be inserted otherwise.
     * <p>
     * The object will be saved to a table with the same name as the object,
     * The fields of object will be mapped to the table fields.
     * 
     * @param object the object to be saved
     */
    public void save(Object object) {
        String query = null;

        if (isObjectNew(object)) {
            //if this is a new object assign a new id first
            generateId(object);
            query = generator.generateInsertQuery(object);
        } else {
            query = generator.generateUpdateQuery(object);
        }

        //execute the insert/update query
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(object);
        template.update(query, parameterSource);
    }

    /**
     * Delete the the object with the specified id of the specified objectClass type
     * @param id the id of the object to be deleted
     * @param objectClass the class of the object to be deleted
     */
    public void delete(int id, Class objectClass) {
        String query = generator.generateDeleteQuery(objectClass);
        Map parameterMap = new HashMap();
        parameterMap.put("id", new Integer(id));
        template.update(query, parameterMap);
    }

    //--------------------------------------------------------------------------
    //PRIVATE METHODS
    //return true if the object id is zero or null false otherwise
    private boolean isObjectNew(Object object) throws VenException {
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        Object objectId = beanWrapper.getPropertyValue("id");
        if (objectId == null) return true;
        if (!(objectId instanceof Integer)) throw new VenException(VenException.EC_GENERATOR_OBJECT_ID_TYPE_INVALID);
        return ((Integer)objectId).intValue() == 0;
    }

    //set new object id
    private void generateId(Object object) {
        Integer newObjectId = new Integer(template.queryForInt(generator.generateSequenceQuery(object), new HashMap()));
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        beanWrapper.setPropertyValue("id", newObjectId);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    /**
     * Set the DataSource to be used to access to the database
     */
    public void setDataSource(DataSource dataSource) {
        if (dataSource == null) throw new RuntimeException("fmgVen - DataSource cannot be null");
        this.template = new NamedParameterJdbcTemplate(dataSource);
        mapper.setDataSource(dataSource);
    }

    /**
     * Add the domain packages that have corresponding database tables.
     * <p>
     * The objects in these packages are considered persistable.
     * @param domainPackage the package of the entity classes.
     * @return this instance to allow chaining.
     */
    public Ven addDomainPackage(String domainPackage) {
        generator.addDomainPackage(domainPackage);
        mapper.addDomainPackage(domainPackage);
        return this;
    }
}
