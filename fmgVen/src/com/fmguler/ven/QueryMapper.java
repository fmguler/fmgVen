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
 * Maps the result of the query generated in the form of 'Convention over Configuration' to the specified object.
 * @author Fatih Mehmet Güler
 */
public class QueryMapper {
    private NamedParameterJdbcTemplate template;

    public List list() {
        return null;
    }

    //SETTERS-------------------------------------------------------------------
    public void setDataSource(DataSource dataSource) {
        if (dataSource == null) throw new RuntimeException("fmgVen - DataSource cannot be null");
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    public void addDomainPackage(String domainPackage){

    }
}
