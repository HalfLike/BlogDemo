package com.halflike.module;

/**
 * Created by luox on 15/6/14.
 */
public class QQModule implements ModuleInterface {
    @Override
    public String print(String msg) {
        return "It is QQModule. " + msg;
    }
}
