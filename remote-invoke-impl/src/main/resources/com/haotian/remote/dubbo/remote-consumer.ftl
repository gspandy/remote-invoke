<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://code.alibabatech.com/schema/dubbo http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
<#list consumers as consumer>
    <dubbo:reference id="${consumer.beanId}" interface="${consumer.interface}" connections="${consumer.connectionNum?string('0.##')}" <@print "version", consumer.version/> <@print "group", consumer.group/> <@print "timeout",consumer.clientTimeout?string('0.##')/> <@print "url", consumer.target/> check="false"/>
</#list>
</beans>
<#macro print key value><#if value != '0' && value != ''>${key}="${value}"</#if></#macro>