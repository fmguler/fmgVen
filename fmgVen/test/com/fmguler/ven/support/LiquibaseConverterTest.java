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
package com.fmguler.ven.support;

/**
 * Test LiquibaseConverter which converts domain to liquibase changeset xml
 * @author Fatih Mehmet Güler
 */
public class LiquibaseConverterTest {
    public static void main(String[] args) {
        //test convert
        testConvert();
    }

    /**
     * Test convert domain to changeset xml
     */
    public static void testConvert() {
        LiquibaseConverter liquibaseConverter = new LiquibaseConverter();
        liquibaseConverter.setAuthor("fmguler");
        liquibaseConverter.setChangeSetIdStart(1);
        liquibaseConverter.addDomainPackage("com.fmguler.ven.sample.domain");
        String liquibaseXml = liquibaseConverter.convert();
        System.out.println(liquibaseXml);
    }
}
