package com.gap.pipeline.utils




public class EnvironmentStub {
    def map = [:]

    public  getValue(name){
        map[name]
    }
    public void setValue(name, value){
        map[name] = value
    }
}
