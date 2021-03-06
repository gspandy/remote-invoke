package com.haotian.remote;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 远程服务调用消费者：在实现类中加入此标记，发布工程会自动将实现类包装为远程服务提供者
 * @author liuzy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ProxyProvider {
    String version();
    long clientTimeout() default 0;
    String serializeType() default "hessian";
    int corePoolSize() default 0;
    int maxPoolSize() default 0;
    String group() default "";
}
