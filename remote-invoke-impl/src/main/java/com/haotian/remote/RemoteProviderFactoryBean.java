package com.haotian.remote;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class RemoteProviderFactoryBean implements FactoryBean {
    private Object target;
    private Class objectType;
    private RemoteInvokeHandler remoteInvokeHandler;
    private static final Map<Class<? extends RemoteInvokeHandler>, RemoteInvokeHandler> REMOTE_INVOKE_HANDLER_MAPPING = new HashMap<Class<? extends RemoteInvokeHandler>, RemoteInvokeHandler>();

    RemoteProviderFactoryBean(RemoteInvokeHandler remoteInvokeHandler, Class<?> objectType) {
        this.remoteInvokeHandler = remoteInvokeHandler;
        this.objectType = objectType;
        this.target = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{objectType.isInterface() ? objectType : objectType.getInterfaces()[0]}, this.remoteInvokeHandler);
    }

    static RemoteInvokeHandler getRemoteInvokeHandler(Class<? extends RemoteInvokeHandler> remoteInvokeHandlerClass) throws IllegalAccessException, InstantiationException {
        RemoteInvokeHandler remoteInvokeHandler = REMOTE_INVOKE_HANDLER_MAPPING.get(remoteInvokeHandlerClass);
        if (remoteInvokeHandler == null) {
            remoteInvokeHandler = remoteInvokeHandlerClass.newInstance();
            REMOTE_INVOKE_HANDLER_MAPPING.put(remoteInvokeHandlerClass, remoteInvokeHandler);
        }
        return remoteInvokeHandler;
    }

    protected RemoteProviderFactoryBean(Class<RemoteInvokeHandler> remoteInvokeHandlerClass, Class<?> objectType) throws IllegalAccessException, InstantiationException {
        this(getRemoteInvokeHandler(remoteInvokeHandlerClass), objectType);
    }

    @Override
    public Object getObject() throws Exception {
        return this.target;
    }

    @Override
    public Class getObjectType() {
        return this.objectType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<? extends RemoteInvokeHandler> getRemoteInvokeHandlerClass() {
        return remoteInvokeHandler.getClass();
    }
}
