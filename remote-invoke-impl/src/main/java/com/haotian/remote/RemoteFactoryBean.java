package com.haotian.remote;

public interface RemoteFactoryBean {
    Object getObject();

    Class<?> getObjectType();
}
