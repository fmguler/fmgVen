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
package com.fmguler.ven.util;

import java.util.Locale;

/**
 * Utility class for conversions from database naming convention to java, and vice versa
 * @author Fatih Mehmet Güler
 */
public class Convert {
    /**
     * Convert from java camelCase case to database naming convention
     * <p>
     * For example, if the name of the given name is SomeDomainObject
     * it will be converted to some_domain_object
     *
     * @param camelCase the camelCase case name
     * @return database style name
     */
    public static String toDB(String camelCase) {
        if (camelCase.equals("")) return "";
        StringBuffer result = new StringBuffer();
        result.append(camelCase.substring(0, 1).toLowerCase(Locale.ENGLISH));
        for (int i = 1; i < camelCase.length(); i++) {
            String sCamel = camelCase.substring(i, i + 1);
            String sLower = sCamel.toLowerCase(Locale.ENGLISH);
            if (!sCamel.equals(sLower)) {
                result.append("_").append(sLower);
            } else result.append(sCamel);
        }
        return result.toString();
    }

    /**
     * Convert the full package name to simple name.
     * For Java 1.4 compatibility of Class.getSimpleName()
     * @param fullName Class.getName()
     * @return the part after the last .
     */
    public static String toSimpleName(String fullName) {
        int i = fullName.lastIndexOf(".");
        if (i < 0) return fullName;
        return fullName.substring(i + 1);
    }
}
