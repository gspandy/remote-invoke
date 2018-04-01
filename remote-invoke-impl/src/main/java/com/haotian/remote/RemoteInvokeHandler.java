package com.haotian.remote;

import java.lang.reflect.InvocationHandler;

public interface RemoteInvokeHandler extends InvocationHandler {
    boolean support(Class<?> targetClass);
}
