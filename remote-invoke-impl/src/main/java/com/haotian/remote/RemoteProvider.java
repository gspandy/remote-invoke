package com.haotian.remote;

import java.util.Set;
import java.util.logging.Logger;

public class RemoteProvider {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private String version;
    private long clientTimeout;
    private String serializeType;
    private int corePoolSize;
    private int maxPoolSize;
    private String intface;
    private String group;
    private String ref;

    private RemoteProviderFactoryBean remoteProviderFactoryBean;

    public RemoteProvider(Class<?> providerClazz) {
        ProxyProvider proxyProvider = providerClazz.getAnnotation(ProxyProvider.class);
        this.version = ProxyXmlWebApplicationContext.getRealValue(proxyProvider.version());
        this.clientTimeout = proxyProvider.clientTimeout();
        this.serializeType = ProxyXmlWebApplicationContext.getRealValue(proxyProvider.serializeType());
        this.corePoolSize = proxyProvider.corePoolSize();
        this.maxPoolSize = proxyProvider.maxPoolSize();
        this.intface = providerClazz.getInterfaces()[0].getName();
        this.group = ProxyXmlWebApplicationContext.getRealValue(proxyProvider.group());

        RemoteHandler remoteHandler = providerClazz.getAnnotation(RemoteHandler.class);

        Set<String> refSet = ProxyXmlWebApplicationContext.getBeanNames(providerClazz);
        if (refSet == null && remoteHandler == null) {
            return;
        }
        if (refSet != null) {
            if (refSet.size() > 1) {
                throw new RuntimeException("find more providerClass[" + providerClazz.getName() + "] for remote provider.");
            }
            if (!refSet.isEmpty()) {
                this.ref = refSet.iterator().next();
            }
        }

        logger.info("Class[" + providerClazz.getName() + "] remote handler[" + (remoteHandler == null ? "none" : remoteHandler.value().getName()) + "]");
        if (this.ref == null && remoteHandler != null) {
            try {
                RemoteInvokeHandler remoteInvokeHandler = RemoteProviderFactoryBean.getRemoteInvokeHandler(remoteHandler.value());
                if (remoteInvokeHandler.support(providerClazz)) {
                    remoteProviderFactoryBean = new RemoteProviderFactoryBean(remoteInvokeHandler, providerClazz);
                }
            } catch (Throwable e) {
                throw new IllegalArgumentException("Init remote factory bean error", e);
            }
            RemoteProviderFactoryBean remoteProviderFactoryBean = this.getRemoteProviderFactoryBean();
            Class<?> targetClass = remoteProviderFactoryBean.getObjectType();
            Class<?>[] interfaces = targetClass.getInterfaces();
            for (Class<?> intface : interfaces) {
                ProxyConsumer proxyConsumer = intface.getAnnotation(ProxyConsumer.class);
                if (proxyConsumer == null) {
                    continue;
                }
                this.ref = proxyConsumer.beanId();
                break;
            }
            if (this.ref == null) {
                throw new IllegalStateException("Class[" + targetClass + "] is not a ProxyConsumer subclass");
            }
        }
        if (this.ref == null) {
            throw new IllegalStateException("Class[" + providerClazz + "]'s ref not found");
        }
    }

    public RemoteProviderFactoryBean getRemoteProviderFactoryBean() {
        return remoteProviderFactoryBean;
    }

    public String getRef() {
        return ref;
    }

    public String getVersion() {
        return version;
    }

    public long getClientTimeout() {
        return clientTimeout;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public String getInterface() {
        return intface;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "RemoteProvider{" +
                "version='" + version + '\'' +
                ", clientTimeout=" + clientTimeout +
                ", serializeType='" + serializeType + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", interface='" + intface + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
