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
package com.fmguler.ven.sample;

import com.fmguler.ven.LiquibaseUtil;
import com.fmguler.ven.Ven;
import com.fmguler.ven.sample.domain.AnotherDomainObject;
import com.fmguler.ven.sample.domain.SomeDomainObject;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates sample usage of fmgVen.
 * @author Fatih Mehmet Güler
 */
public class Sample {
    public static void main(String[] args) {
        //build the sample database
        LiquibaseUtil.buildDatabase();

        //save an object
        testSave();
        //get an object
        testGet();
        //list the objects
        testList();
        //delete an object
        testDelete();

        //rollback the sample database to original state
        LiquibaseUtil.rollbackDatabase("tag-init");
    }

    /**
     * Test save an object
     */
    public static void testSave() {
        System.out.println("******SAVE******");
        Ven ven = getVen();

        //insert
        SomeDomainObject obj = new SomeDomainObject();
        obj.setName("name");
        obj.setDescription("desc");
        obj.setDate(new Date());
        ven.save(obj);
        System.out.println(obj);

        //update
        obj.setName("name update");
        ven.save(obj);
        System.out.println(obj);
    }

    /**
     * Test delete an object
     */
    public static void testDelete() {
        System.out.println("******DELETE******");
        Ven ven = getVen();
        ven.delete(2, SomeDomainObject.class);
    }

    /**
     * Test get an object by primary key
     */
    public static void testGet() {
        System.out.println("******GET******");
        Ven ven = getVen();

        //get with includes
        Set joins = new HashSet();
        joins.add("SomeDomainObject.anotherDomainObjects");
        joins.add("SomeDomainObject.anotherDomainObject");
        SomeDomainObject obj = (SomeDomainObject)ven.get(1, SomeDomainObject.class, joins);
        System.out.println(obj);

        Set joins2 = new HashSet();
        joins2.add("AnotherDomainObject.someDomainObject");
        AnotherDomainObject obj2 = (AnotherDomainObject)ven.get(1, AnotherDomainObject.class, joins2);
        System.out.println(obj2);
    }

    /**
     * Test list the collection of objects
     */
    public static void testList() {
        System.out.println("******LIST******");
        Ven ven = getVen();

        //list with includes
        Set joins = new HashSet();
        joins.add("SomeDomainObject.anotherDomainObjects");
        joins.add("SomeDomainObject.anotherDomainObject");
        List objList = ven.list(SomeDomainObject.class, joins);
        
        Iterator it = objList.iterator();
        while (it.hasNext()) {
            SomeDomainObject someDomainObject = (SomeDomainObject)it.next();
            System.out.println(someDomainObject);
        }
    }

    /**
     * Test list the collection of objects by some criteria
     */
    public static void testListByCriteria() {
        Ven ven = getVen();
        //List objList = ven.list(SomeDomainObject.class/*, criteria */);
        //System.out.println(objList);
    }

    //---------------------------------------------------------
    private static Ven getVen() {
        Ven ven = new Ven();
        ven.setDataSource(LiquibaseUtil.getDataSource());
        ven.addDomainPackage("com.fmguler.ven.sample.domain").addDomainPackage("another.package");
        return ven;
    }
}
