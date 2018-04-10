package com.haotian.remote;

@ProxyConsumer(beanId = "remoteInvokeService", group = "HSF", version = "1.0")
public interface RemoteInvokeService {
    public String[] getParameterNames(String remoteInvokeUniqueKey);
}
