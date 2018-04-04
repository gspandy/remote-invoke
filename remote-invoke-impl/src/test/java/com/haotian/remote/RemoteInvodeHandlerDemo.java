package com.haotian.remote;

import com.haotian.remote.RemoteInvokeHandler;

import java.lang.reflect.Method;

public class RemoteInvodeHandlerDemo implements RemoteInvokeHandler {
    @Override
    public boolean support(Class<?> targetClass) {
        return true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
