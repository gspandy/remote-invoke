package com.haotian.remote;

import com.haotiao.proxy.cache.CacheService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Method;

@ProxyProvider(group = "HSF", version = "1.0")
public class RemoteInvokeServiceImpl implements RemoteInvokeServiceEx, ApplicationContextAware {
    private static final LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private CacheService cacheService;
    private static final String[] EMPTY = new String[0];

    @Override
    public String[] getParameterNames(String remoteInvokeUniqueKey) {
        if (this.cacheService == null) {
            return EMPTY;
        }
        String[] parameterNames = this.cacheService.get(remoteInvokeUniqueKey, String[].class);
        if (parameterNames == null) {
            return EMPTY;
        }
        return parameterNames;
    }

    public void registerRemoteInvokeClass(Class<?> remoteClass) {
        if (this.cacheService == null) {
            return;
        }

        if (remoteClass.getAnnotation(ProxyProvider.class) == null) {
            return;
        }
        Class<?>[] remoteInterfaces = remoteClass.getInterfaces();
        Class<?> targetInterface = null;
        for (Class<?> remoteInterface : remoteInterfaces) {
            ProxyConsumer proxyConsumer = remoteInterface.getAnnotation(ProxyConsumer.class);
            if (proxyConsumer == null) {
                continue;
            }
            targetInterface = remoteInterface;
            break;
        }
        if (targetInterface == null) {
            return;
        }
        RemoteConsumer remoteConsumer = new RemoteConsumer(targetInterface);
        Method[] interfaceMethods = targetInterface.getMethods();
        try {
            for (Method interfaceMethod: interfaceMethods) {
                String remoteKey = RemoteInvokeUtils.getRemoteKey(remoteConsumer.getInterface(), interfaceMethod.getName(), remoteConsumer.getGroup(), remoteConsumer.getVersion());
                Method remoteMethod = remoteClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                this.cacheService.set(remoteKey, parameterNameDiscoverer.getParameterNames(remoteMethod));
            }
        } catch (Throwable e) {
            throw new RemoteInvokeException("RegisterRemoteInvokeClass Error", e);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        cacheService = applicationContext.getBean(CacheService.class);
    }

}
