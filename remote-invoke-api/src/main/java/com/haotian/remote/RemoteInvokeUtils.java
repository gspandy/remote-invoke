package com.haotian.remote;

public final class RemoteInvokeUtils {
    public static String getRemoteKey(String interfaceName, String methodName, String group, String version) {
        return interfaceName + ":" + methodName + ":" + group + ":" + version;
    }
}
