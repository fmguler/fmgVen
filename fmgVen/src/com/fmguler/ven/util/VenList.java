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
package com.fmguler.ven.util;

import java.util.LinkedList;
import java.util.List;

/**
 * A Simple Linked List which informs the type of its elements.
 * @author Fatih Mehmet Güler
 */
public class VenList extends LinkedList {
    private Class elementClass;
    private String joinField;

    /**
     * Creates new instance of VenList
     */
    public VenList() {
    }

    /**
     * Creates new instance of VenList, specifying the type of elements
     * @param elementClass the type of elements of this list
     */
    public VenList(Class elementClass) {
        this.elementClass = elementClass;
    }

    /**
     * Creates new instance of VenList, specifying the type of elements and the join field
     * @param elementClass the type of elements of this list
     * @param joinField the field which refers to the parent object
     */
    public VenList(Class elementClass, String joinField) {
        this.elementClass = elementClass;
        this.joinField = joinField;
    }

    /**
     * Returns the type of the elements in this list.
     * @return element type
     */
    public Class getElementClass() {
        return elementClass;
    }

    /**
     * Returns the field in the element class which refers the parent object
     * @return element join field
     */
    public String getJoinField() {
        return joinField;
    }

    /**
     * Return the class of the elements in the list
     * @param list the list
     * @return the element class in the list
     */
    public static Class findElementClass(List list) {
        if (list.size() > 0) {
            Object elem = list.get(0);
            return elem.getClass();
        }
        if (list instanceof VenList) {
            return ((VenList)list).getElementClass();
        } else {
            //find according to 1.5 generic or some convention (e.g. xyzList -> xyz)
            return null;
        }
    }
}
