<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://code.alibabatech.com/schema/dubbo http://code.alibabatech.com/schema/dubbo/dubbo.xsd">

<#list providers as provider>
    <dubbo:service interface="${provider.interface}" ref="${provider.ref}" <@print "version", provider.version/> <@print "group", provider.group/> <@print "timeout", provider.clientTimeout?string('0.##')/> <@print "executes", provider.maxPoolSize?string('0.##')/>/>
    <#if (provider.remoteProviderFactoryBean??)?then(true, false)>
        <bean id="${provider.ref}" class="${provider.remoteProviderFactoryBean.class.name}">
            <constructor-arg index="0" value="${provider.remoteProviderFactoryBean.remoteInvokeHandlerClass.name}"/>
            <constructor-arg index="1" value="${provider.remoteProviderFactoryBean.objectType.name}"/>
        </bean>
    </#if>
</#list>
</beans>
<#macro print key value><#if value != '0' && value != ''>${key}="${value}"</#if></#macro>