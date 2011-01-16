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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Ven() {
        generator = new QueryGenerator();
        mapper = new QueryMapper();
    }

    public List list(Class objectClass) {
        return null;
    }

    public int count() {
        return 0;
    }

    public Object get(int id, Class objectClass) {
        return null;
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
    public void setDataSource(DataSource dataSource) {
        if (dataSource == null) throw new RuntimeException("fmgVen - DataSource cannot be null");
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    public Ven addDomainPackage(String domainPackage) {
        generator.addDomainPackage(domainPackage);
        mapper.addDomainPackage(domainPackage);
        return this;
    }
}
