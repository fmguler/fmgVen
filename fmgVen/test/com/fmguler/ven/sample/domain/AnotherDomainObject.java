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
package com.fmguler.ven.sample.domain;

import java.util.Date;

/**
 *
 * @author Fatih Mehmet Güler
 */
public class AnotherDomainObject {
    private Integer id;
    private String name;
    private String description;
    private Date date;
    private SomeDomainObject someDomainObject;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the someDomainObject
     */
    public SomeDomainObject getSomeDomainObject() {
        return someDomainObject;
    }

    /**
     * @param someDomainObject the someDomainObject to set
     */
    public void setSomeDomainObject(SomeDomainObject someDomainObject) {
        this.someDomainObject = someDomainObject;
    }

    public String toString() {
        return "{" + id + ", " + name + ", " + description + ", some domain object: " + someDomainObject + "}";
    }
}
