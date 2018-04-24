<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:hsf="http://www.taobao.com/hsf"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.taobao.com/hsf
        http://www.taobao.com/hsf/hsf.xsd">
    <#list providers as provider>
    <hsf:provider id="${provider.ref}-${provider.version}-${provider.group}-provider" interface="${provider.interface}" ref="${provider.ref}" <@print "version", provider.version/> <@print "group", provider.group/> <@print "clientTimeout", provider.clientTimeout?string('0.##')/> <@print "serializeType", provider.serializeType/> <@print "corePoolSize", provider.corePoolSize?string('0.##')/> <@print "maxPoolSize", provider.maxPoolSize?string('0.##')/>/>
    <#if (provider.remoteProviderFactoryBean??)?then(true, false)>
    <bean id="${provider.ref}" class="${provider.remoteProviderFactoryBean.class.name}">
        <constructor-arg index="0" value="${provider.remoteProviderFactoryBean.remoteInvokeHandlerClass.name}"/>
        <constructor-arg index="1" value="${provider.remoteProviderFactoryBean.objectType.name}"/>
    </bean>
    </#if>
    </#list>
</beans>
<#macro print key value><#if value != '0' && value != ''>${key}="${value}"</#if></#macro>