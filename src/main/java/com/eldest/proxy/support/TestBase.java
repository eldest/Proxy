package com.eldest.proxy.support;

public abstract class TestBase {

    protected String getResource(String name) {
        return ClassLoader.getSystemResource(name).getFile();
    }

}
