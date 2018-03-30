package com.haotian.remote;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 远程服务调用消费者：在接口类中加入此标记，发布工程会自动将接口包装为远程服务调用者
 * @author liuzy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ProxyConsumer {
    String beanId();

    String version();

    long clientTimeout() default 0;

    int connectionNum() default 1;

    String group() default "";

    String target() default "";
}
