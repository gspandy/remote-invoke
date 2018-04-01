package com.haotian.remote;

public interface RemoteFactoryBean {
    Object getObject();

    void setObjectType(Class<?> targetClass);

    Class<?> getObjectType();
}
