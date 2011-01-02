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

/**
 * All kinds of exceptions in Ven operations.
 * @author Fatih Mehmet Güler
 */
public class VenException extends RuntimeException {
    private String errorCode;
    private String errorParam;
    //Error codes
    //"Invalid object, \"id\" property must be of integer type"
    public static final String EC_GENERATOR_OBJECT_ID_TYPE_INVALID = "ec-generator-object-id-type-invalid";

    /**
     * Init VenException with an error code
     * @param errorCode the error code
     */
    public VenException(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Init VenException with an error code and an error parameter
     * @param errorCode the error code
     * @param errorParam the error param
     */
    public VenException(String errorCode, String errorParam) {
        this.errorCode = errorCode;
        this.errorParam = errorParam;
    }

    /**
     * Init VenException with an error code, error parameter and the cause of the exception
     * @param errorCode the error code
     * @param errorParam the error parameter
     * @param cause the cause of the exception
     */
    public VenException(String errorCode, String errorParam, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.errorParam = errorParam;
    }

    /**
     * @return the error code of the exception
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return the error parameter of the exception, which can contain details about the error
     */
    public String getErrorParam() {
        return errorParam;
    }
}
