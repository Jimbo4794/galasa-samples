package com.example.myapp.manager.internal;

import com.example.myapp.manager.IMyApp;

public class MyAppImpl implements IMyApp {
    private String tag;
    private String version;

    public MyAppImpl(String tag, String version) {
        this.tag = tag;
        this.version = version;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
    
}