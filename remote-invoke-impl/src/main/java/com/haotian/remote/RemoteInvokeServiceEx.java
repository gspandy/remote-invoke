package com.haotian.remote;

public interface RemoteInvokeServiceEx extends RemoteInvokeService {
    void registerRemoteInvokeClass(Class<?> remoteClass);
}
