package com.haotian.remote;

import java.util.Set;

public class RemoteProvider {
    private String version;
    private long clientTimeout;
    private String serializeType;
    private int corePoolSize;
    private int maxPoolSize;
    private String intface;
    private String group;
    private String ref;

    protected RemoteProvider(Class<?> providerClazz) {
        ProxyProvider proxyProvider = providerClazz.getAnnotation(ProxyProvider.class);
        this.version = proxyProvider.version();
        this.clientTimeout = proxyProvider.clientTimeout();
        this.serializeType = proxyProvider.serializeType();
        this.corePoolSize = proxyProvider.corePoolSize();
        this.maxPoolSize = proxyProvider.maxPoolSize();
        this.intface = providerClazz.getInterfaces()[0].getName();
        this.group = proxyProvider.group();

        Set<String> refSet = ProxyXmlWebApplicationContext.getBeanNames(providerClazz);
        if (refSet == null) {
            return;
        }
        if (refSet.size() > 1) {
            throw new RuntimeException("find more providerClass[" + providerClazz.getName() + "] for remote provider.");
        }
        this.ref = refSet.iterator().next();
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
