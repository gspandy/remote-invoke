package com.haotian.remote;

import java.lang.reflect.Proxy;

public class RemoteFactoryBeanImpl implements RemoteFactoryBean {
    private RemoteInvokeHandler remoteInvokeHandler;
    private Class<?> objectType;

    public RemoteFactoryBeanImpl(RemoteInvokeHandler remoteInvokeHandler, Class<?> objectType) {
        this.remoteInvokeHandler = remoteInvokeHandler;
        this.objectType = objectType;
    }
    @Override
    public Object getObject() {
        Object targetBean;
        try {
            targetBean = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType}, remoteInvokeHandler);
        } catch (Throwable e) {
            throw new IllegalStateException("init remote bean error.", e);
        }
        return targetBean;
    }

    @Override
    public Class<?> getObjectType() {
        return this.objectType;
    }
}
