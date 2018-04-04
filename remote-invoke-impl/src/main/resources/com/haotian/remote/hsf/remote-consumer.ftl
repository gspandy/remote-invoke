<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:hsf="http://www.taobao.com/hsf"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.taobao.com/hsf
        http://www.taobao.com/hsf/hsf.xsd">
    <#list consumers as consumer>
    <hsf:consumer id="${consumer.beanId}" interface="${consumer.interface}" connectionNum="${consumer.connectionNum}" <@print "version", consumer.version/> <@print "group", consumer.group/> <@print "clientTimeout",consumer.clientTimeout?string.number/> <@print "target", consumer.target/>/>
    </#list>
</beans>
<#macro print key value><#if value != '0' && value != ''>${key}="${value}"</#if></#macro>