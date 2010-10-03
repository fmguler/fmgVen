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

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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

    public Object get(int no, Class objectClass) {
        return null;
    }

    public void save(Object object) {
    }

    public void delete(int no, Class objectClass) {
    }

    //SETTERS-------------------------------------------------------------------
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
