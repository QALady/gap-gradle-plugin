package com.gap.pipeline.ec

import com.gap.gradle.exceptions.InvalidPropertyAccessException

class Property {

    String value
    String key

    public static final String INVALID = "invalid"

    Property(key = "", value = "") {
        this.key = key
        this.value = value
    }

    static invalidProperty(def key = ""){
        return new Property(key, INVALID)
    }

    def isValid(){
        !value.equals(INVALID)
    }

    def getValue(){
        if(value.equals(INVALID)){
            throw new InvalidPropertyAccessException("Property with key ${this.key} does not exist. Cannot access value.")
        }
        return value
    }
}
