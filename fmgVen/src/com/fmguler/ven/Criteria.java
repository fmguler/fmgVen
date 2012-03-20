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
package com.fmguler.ven;

import com.fmguler.ven.util.Convert;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * Criteria object, used to filter the result of a query according to some criteria.
 * <p>
 * Can be used in two modes; as string criteria or typed criteria.
 * The string criteria is used like;
 * <pre>
 * new Criteria().add("and SomeDomainObject.anotherDomainObjects.name like :p1").param("p1", "a%");
 * </pre>
 * And the typed criteria is used like;
 * <pre>
 * new Criteria().eq(attr,value).like(attr,value).and().gt(attr, value).or().orderAsc(attr).orderDesc(attr).limit(limit, offset);
 * </pre>
 *
 * This criteria object is used to filter SQL results by adding the "Criteria.criteriaToSQL()" output to the where clause.
 * 
 * @author Fatih Mehmet Güler
 */
public class Criteria {
    private StringBuffer criteriaStringBuffer = new StringBuffer(); //for string criteria
    private StringBuffer orderStringBuffer = new StringBuffer(); //for ordering
    private LinkedList criterionList = new LinkedList(); //for typed criteria
    private Map parameters = new HashMap(); //the parameters used in criteria string
    private int limit = 20;
    private int offset = 0;
    int paramCount = 0;

    //--------------------------------------------------------------------------
    //String Criteria Methods
    /**
     * Add a criteria string
     */
    public Criteria add(String criteriaString) {
        this.criteriaStringBuffer.append(" ").append(criteriaString);
        return this;
    }

    /**
     * Add the parameter used in criteria string
     */
    public Criteria param(String parameter, Object value) {
        this.parameters.put(parameter, value);
        return this;
    }

    /**
     * Converts criteria string to SQL
     * @return the criteria string as SQL where clause
     */
    public String criteriaStringToSQL() {
        StringBuffer resultBuffer = new StringBuffer();
        String criteriaString = criteriaStringBuffer.toString();
        criteriaString = criteriaString.replaceAll("\\(", "\\( ");
        criteriaString = criteriaString.replaceAll("\\)", " \\)");

        String[] parts = criteriaString.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].startsWith(":") && parts[i].indexOf(".") >= 0) {
                int lastDot = parts[i].lastIndexOf('.');
                resultBuffer.append(" ");
                int u = parts[i].length();
                resultBuffer.append(Convert.toDB(parts[i].substring(0, lastDot).replace('.', '_')));
                resultBuffer.append(Convert.toDB(parts[i].substring(lastDot, u)));
            } else {
                resultBuffer.append(" ").append(parts[i]);
            }
        }
        return resultBuffer.toString();
    }

    /**
     * @return the parameter map
     */
    public Map getParameters() {
        return parameters;
    }

    /**
     * Return order string to SQL
     */
    public String orderStringToSQL() {
        if (orderStringBuffer.length() == 0) return "";
        orderStringBuffer.insert(0, " order by");
        return orderStringBuffer.toString();
    }

    //--------------------------------------------------------------------------
    //Typed Criteria Methods
    /**
     * Add a criterion where an attribue equals a value (SQL = operator)
     */
    public Criteria eq(String attribute, Object value) {
        criterionList.add(new Criterion(attribute, Criterion.OP_EQUALS, value));
        return this;
    }

    /**
     * Add a criterion where an attribue is like a value (SQL like operator)
     */
    public Criteria like(String attribute, Object value) {
        criterionList.add(new Criterion(attribute, Criterion.OP_LIKE, value));
        return this;
    }

    /**
     * Add a criterion where an attribue is similar to a value (SQL similar to operator)
     */
    public Criteria similarto(String attribute, Object value) {
        criterionList.add(new Criterion(attribute, Criterion.OP_SIMILAR_TO, value));
        return this;
    }

    /**
     * Add a criterion where an attribue is greater than a value (SQL > operator)
     */
    public Criteria gt(String attribute, Object value) {
        criterionList.add(new Criterion(attribute, Criterion.OP_GREATER_THAN, value));
        return this;
    }

    /**
     * Add a criterion where an attribue is less than a value (SQL < operator)
     */
    public Criteria lt(String attribute, Object value) {
        criterionList.add(new Criterion(attribute, Criterion.OP_LESS_THAN, value));
        return this;
    }

    /**
     * Add a criterion where an attribue is null (SQL is null operator)
     */
    public Criteria isNull(String attribute) {
        criterionList.add(new Criterion(attribute, Criterion.OP_ISNULL, null));
        return this;
    }

    /**
     * Add: order by attribute asc
     */
    public Criteria orderAsc(String attribute) {
        if (this.orderStringBuffer.length() != 0) this.orderStringBuffer.append(",");
        this.orderStringBuffer.append(" ").append(convertAttributeToAlias(attribute)).append(" asc");
        return this;
    }

    /**
     * Add: order by attribute desc
     */
    public Criteria orderDesc(String attribute) {
        if (this.orderStringBuffer.length() != 0) this.orderStringBuffer.append(",");
        this.orderStringBuffer.append(" ").append(convertAttributeToAlias(attribute)).append(" desc");
        return this;
    }

    /**
     * Add limit and offset to the clause
     */
    public Criteria limit(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    //--------------------------------------------------------------------------
    //Connectives
    /**
     * Connect previous two criteria with logical and
     * (as in postfix notation)
     */
    public Criteria and() {
        criterionList.add(new Criterion(Criterion.CONN_AND));
        return this;
    }

    /**
     * Connect previous two criteria with logical or
     * (as in postfix notation)
     */
    public Criteria or() {
        criterionList.add(new Criterion(Criterion.CONN_OR));
        return this;
    }

    /**
     * Connect previous criterion with logical not
     * (as in postfix notation)
     */
    public Criteria not() {
        criterionList.add(new Criterion(Criterion.CONN_NOT));
        return this;
    }

    /**
     * Converts typed criteria to SQL
     * @return the typed criteria as SQL where clause
     */
    public String criteriaToSQL() {
        LinkedList clone = (LinkedList)criterionList.clone();
        Stack stack = new Stack();
        while (!clone.isEmpty()) {
            Criterion criterion = (Criterion)clone.removeFirst();
            if (!criterion.isConnective()) {
                String s = "(" + convertOperatorToSQL(criterion) + ")";
                stack.push(s);
            } else {
                String conn = criterion.connective;
                if (conn.equals(Criterion.CONN_AND)) {
                    String s1 = (String)stack.pop();
                    String s2 = (String)stack.pop();
                    String sr = "(" + s1 + " and " + s2 + ")";
                    stack.push(sr);
                } else if (conn.equals(Criterion.CONN_OR)) {
                    String s1 = (String)stack.pop();
                    String s2 = (String)stack.pop();
                    String sr = "(" + s1 + " or " + s2 + ")";
                    stack.push(sr);
                } else if (conn.equals(Criterion.CONN_NOT)) {
                    String s = (String)stack.pop();
                    String sr = "not (" + s + ")";
                    stack.push(sr);
                }
            }
        }

        if (stack.isEmpty()) return "1=1";

        String result = (String)stack.pop();
        return result;
    }

    //convert the attribute-operator-value criterion to SQL
    private String convertOperatorToSQL(Criterion criterion) {
        String attribute = convertAttributeToAlias(criterion.attribute);
        String operator = criterion.operator;
        Object value = criterion.value;
        String result = "";

        if (value == null) {
            //no value e.g. is null operator
            result = attribute + " " + operator;
        } else {
            String paramName = genParamName();
            parameters.put(paramName, value);
            result = attribute + " " + operator + " :" + paramName + "";
        }

        return result;
    }

    //convert attribute to SQL alias
    protected String convertAttributeToAlias(String attribute) {
        //we convert like: x.y.z.t to x_y_z.t
        StringBuffer resultBuffer = new StringBuffer();
        int lastDot = attribute.lastIndexOf('.');
        resultBuffer.append(" ");
        int u = attribute.length();
        resultBuffer.append(Convert.toDB(attribute.substring(0, lastDot).replace('.', '_')));
        resultBuffer.append(Convert.toDB(attribute.substring(lastDot, u)));
        return resultBuffer.toString();
    }

    //generate parameter for typed criteria
    private String genParamName() {
        return "__p" + paramCount++;
    }

    //test the criteria object
    public static void main(String[] args) {
        Criteria criteria = new Criteria() //criteria object
                .like("SomeDomainObject.anotherDomainObjects.name", "a%") //attribute like value
                .eq("SomeDomainObject.name", "sdo1") //attribute equals value
                .and() //connect previous criteria with and
                .isNull("SomeDomainObject.description") //attribute is null
                .or() //connect previous criteria with or
                .orderDesc("SomeDomainObject.name"); //order by some attribute

        //print the resulting where clause SQL
        System.out.println(criteria.criteriaToSQL());

        //print the resulting order clause SQL
        System.out.println(criteria.orderStringToSQL());

        //the result is;
        //(( some_domain_object.description is null) or (( some_domain_object.name = :__p1) and ( some_domain_object_another_domain_objects.name like :__p0)))
    }
}

/**
 * Each criterion used in the criteria.
 * Used to hold attribute-operator-value triplet.
 * Used internally.
 * @author Fatih Mehmet Güler
 */
class Criterion {
    //available logical connectives
    public static final String CONN_AND = "and";
    public static final String CONN_OR = "or";
    public static final String CONN_NOT = "not";
    //available operators
    public static final String OP_EQUALS = "=";
    public static final String OP_LESS_THAN = "<";
    public static final String OP_GREATER_THAN = ">";
    public static final String OP_LIKE = "like";
    public static final String OP_SIMILAR_TO = "similar to";
    public static final String OP_NOT = "not";
    public static final String OP_ISNULL = "is null";
    //attribute-operator-value triplet
    public String attribute;
    public String operator;
    public Object value;
    //logical connective
    public String connective = null;

    public Criterion(String attribute, String operator, Object value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public Criterion(String connective) {
        this.connective = connective;
    }

    public boolean isConnective() {
        return connective != null;
    }

    /**
     * @return string representation of this criterion
     */
    public String toString() {
        if (isConnective()) return connective;
        return attribute + " " + operator + " " + value;
    }
}
