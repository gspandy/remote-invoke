package com.haotian.remote;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RemoteMethodInvokeHandler implements InvocationHandler {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Object targetBean;
    private final Map<String, List<Method>> methodMapping = new HashMap<String, List<Method>>();
    private final InvocationHandler targetBeanInvocationHandler;

    RemoteMethodInvokeHandler(Object targetBean, Method[] methods, RemoteHandler targetBeanRemoteHandler) {
        this.targetBean = targetBean;
        if (targetBeanRemoteHandler != null) {
            try {
                this.targetBeanInvocationHandler = RemoteProviderFactoryBean.getRemoteInvokeHandler(targetBeanRemoteHandler.value());
            } catch (Throwable e) {
                throw new IllegalStateException("init bean invoke handler error", e);
            }
        } else {
            this.targetBeanInvocationHandler = null;
        }
        for (Method method: methods) {
            List<Method> innerMethods = methodMapping.get(method.getName());
            if (innerMethods == null) {
                innerMethods = new ArrayList<Method>();
                methodMapping.put(method.getName(), innerMethods);
            }
            innerMethods.add(method);
        }
    }

    private boolean isSameMethod(Method sourceMethod, Method targetMethod) {
        if (!sourceMethod.getName().equals(targetMethod.getName())) {
            return false;
        }
        if (sourceMethod.getReturnType() != targetMethod.getReturnType()) {
            return false;
        }
        Class<?>[] sourceParams = sourceMethod.getParameterTypes();
        Class<?>[] targetParams = targetMethod.getParameterTypes();
        if (sourceParams == null) {
            return targetParams == null;
        }
        if (sourceParams.length != targetParams.length) {
            return false;
        }
        for (int i = 0; i < sourceParams.length; i++) {
            if (sourceParams[i] != targetParams[i]) {
                return  false;
            }
        }
        return true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Method> invokeMethods = methodMapping.get(method.getName());
        Method invokeMethod = method;
        for (int i = 0; invokeMethods != null && i < invokeMethods.size(); i++) {
            Method imethod = invokeMethods.get(i);
            if (isSameMethod(imethod, method)) {
                invokeMethod = imethod;
                break;
            }
        }
        Method methodGetTargetSource = null;
        RemoteHandler remoteHandler = invokeMethod.getAnnotation(RemoteHandler.class);
        if (remoteHandler == null) {
            return targetBeanInvocationHandler == null ? method.invoke(this.targetBean, args): targetBeanInvocationHandler.invoke(this.targetBean, method, args);
        }
        InvocationHandler invocationHandler = RemoteProviderFactoryBean.getRemoteInvokeHandler(remoteHandler.value());
        return invocationHandler.invoke(this.targetBean, method, args);
    }
}
