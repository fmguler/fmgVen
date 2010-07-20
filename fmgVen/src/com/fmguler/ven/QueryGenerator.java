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

import java.util.HashSet;
import java.util.Set;

/**
 * Generates queries in the form of 'Convention over Configuration' of the specified class.
 * @author Fatih Mehmet Güler
 */
public class QueryGenerator {
    private Set domainPackages;

    public QueryGenerator() {
        domainPackages = new HashSet();
    }

    public String generateSelectQuery() {
        return null;
    }

    public String generateCountQuery() {
        return null;
    }

    public String generateUpdateQuery() {
        return null;
    }

    //SETTERS-------------------------------------------------------------------
    public void addDomainPackage(String domainPackage) {
        domainPackages.add(domainPackage);
    }
}
