package com.haotian.remote;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteInvokeBeanWrapper implements BeanPostProcessor {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Class<?>[] beanInterfaces = beanClass.getInterfaces();
        if (beanInterfaces == null || beanInterfaces.length == 0) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("beanName[" + beanName + "] has none interface; return source bean");
            }
            return bean;
        }
        List<Class<?>> beanInterfaceList = new ArrayList<Class<?>>();
        for (Class<?> beanInterface : beanInterfaces) {
            if (beanInterface.getAnnotation(ProxyConsumer.class) != null) {
                beanInterfaceList.add(beanInterface);
            }
        }
        if (beanInterfaceList.isEmpty()) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("beanName[" + beanName + "] is not ProxyConsumer implement bean; return source bean");
            }
            return bean;
        }

        Object beanSource = bean;
        boolean findSource = false;
        for (int i = 0; i < 10 && Proxy.isProxyClass(beanClass); i++) {
            if (Proxy.getInvocationHandler(beanSource) instanceof RemoteMethodInvokeHandler) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("beanName[" + beanName + "] has wrappered; return source bean");
                }
                return beanSource;
            }

            Method methodGetTargetClass = null;
            Method methodGetTargetSource = null;
            try {
                methodGetTargetClass = beanClass.getMethod("getTargetClass");
                methodGetTargetSource = beanClass.getMethod("getTargetSource");
                beanClass = (Class<?>) methodGetTargetClass.invoke(beanSource);
                beanSource = methodGetTargetSource.invoke(beanSource);
            } catch(NoSuchMethodException e) {
                logger.warning(e.getMessage());
            } catch (Throwable e) {
                throw new RuntimeException("unwrap proxy class error", e);
            }
            if (beanClass.getAnnotation(ProxyProvider.class) != null) {
                findSource = true;
                break;
            }
        }
        if (!findSource) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("beanName[" + beanName + "] is not signed ProxyProvider; return source bean");
            }
            return bean;
        }
        findSource = false;
        Method[] methods = beanClass.getMethods();
        for (Method method: methods) {
            if (method.getAnnotation(RemoteHandler.class) == null) {
                continue;
            }
            findSource = true;
            break;
        }
        RemoteHandler beanRemoteHandler = beanClass.getAnnotation(RemoteHandler.class);
        findSource = findSource || (beanRemoteHandler != null);
        if (!findSource) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("beanName[" + beanName + "] has not RemoteHandler method; return source bean");
            }
            return bean;
        }
        Class<?>[] supperInterfaces = new Class<?>[beanInterfaceList.size()];
        beanInterfaceList.toArray(supperInterfaces);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("wrap bean[" + beanName + "]");
        }
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), supperInterfaces, new RemoteMethodInvokeHandler(bean, methods, beanRemoteHandler));
    }
}
