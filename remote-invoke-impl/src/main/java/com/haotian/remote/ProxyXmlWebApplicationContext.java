package com.haotian.remote;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * spring context加载：替换原XmlWebApplicationContext加载类，新增远程服务生产者与消费者的调用
 * @author liuzy
 */
public class ProxyXmlWebApplicationContext extends XmlWebApplicationContext {
    private static final Logger logger = Logger.getLogger(ProxyXmlWebApplicationContext.class.getName());
    private static final PathMatchingResourcePatternResolver PMRPR = new PathMatchingResourcePatternResolver(AnnotationUtil.class.getClassLoader());
    private static final Properties CONTEXT_PROPS = new Properties();
    private static final Map<Class<?>, Set<String>> PROXY_BEAN_MAPPINGS = new HashMap<Class<?>, Set<String>>();

    private static final void addProxyBean(Class<?> beanClass, String beanName) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("loaded proxy bean[beanName:" + beanName + ", class:" + beanClass.getName() + "].");
        }
        Set<String> beanNameSet = PROXY_BEAN_MAPPINGS.get(beanClass);
        if (beanNameSet == null) {
            beanNameSet = new HashSet<String>();
            PROXY_BEAN_MAPPINGS.put(beanClass, beanNameSet);
        }
        if (beanNameSet.contains(beanName)) {
            throw new RuntimeException("[id:" + beanName + ", class:" + beanClass + "] repeated!!!");
        }
        beanNameSet.add(beanName);
    }

    public static final Set<String> getBeanNames(Class<?> beanClass) {
        return PROXY_BEAN_MAPPINGS.get(beanClass);
    }

    @Override
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
        super.loadBeanDefinitions(reader);
        String[] configLocations = super.getConfigLocations();
        for (String configLoaction : configLocations) {
            Resource[] locations = PMRPR.getResources(configLoaction);
            if (locations == null) {
                continue;
            }
            SAXReader saxReader = new SAXReader();
            for (Resource location : locations) {
                parseAndLoadResource(saxReader, location);
            }
        }

        List<RemoteProvider> providerList = extractProviderList();
        List<RemoteConsumer> consumerList = extractConsumerList();
        String classpath = Thread.currentThread().getContextClassLoader().getResource(File.separator).getPath();
        if (!classpath.endsWith(File.separator)) {
            classpath += File.separator;
        }
        String remoteFileName = "proxy-remote-provider.xml";
        File remoteFile = new File(classpath + remoteFileName);
        remoteFile.deleteOnExit();
        remoteFile.createNewFile();
        FileOutputStream foutput = new FileOutputStream(remoteFile);
        foutput.write(generateProviderBeans(providerList));
        foutput.close();
        reader.loadBeanDefinitions("classpath:" + remoteFileName);

        remoteFileName = "proxy-remote-consumer.xml";
        remoteFile = new File(classpath + remoteFileName);
        remoteFile.deleteOnExit();
        remoteFile.createNewFile();
        foutput = new FileOutputStream(remoteFile);
        foutput.write(genenrateConsumerBeans(consumerList, providerList));
        foutput.close();
        reader.loadBeanDefinitions("classpath:" + remoteFileName);
    }

    public static List<RemoteProvider> extractProviderList() {
        List<RemoteProvider> providerList;
        try {
            providerList = AnnotationUtil.extractProxyProviders();
        } catch (Exception e) {
            throw new RuntimeException("extract proxyprovider error", e);
        }
        return providerList;
    }

    public static List<RemoteConsumer> extractConsumerList() {
        List<RemoteConsumer> consumerList;
        try {
            consumerList = AnnotationUtil.extractProxyConsumers();
        } catch (Exception e) {
            throw new RuntimeException("extract proxyconsumer error", e);
        }
        return consumerList;
    }

    private void loadedProxyBeansAndInitContextProps(Resource contextLocation, Element rootElement, SAXReader saxReader) throws ClassNotFoundException, IOException {
        List<Element> beanList = (List<Element>) rootElement.elements("bean");
        for (Element bean : beanList) {
            Class<?> beanClass = Class.forName(bean.attributeValue("class"));
            if (beanClass.getAnnotation(ProxyProvider.class) != null) {
                String beanName = bean.attributeValue("id");
                if (beanName == null) {
                    beanName = bean.attributeValue("name");
                }
                if (beanName == null) {
                    throw new RuntimeException("beanName required for class[" + beanClass.getName() + "] in file[" + contextLocation.getFilename() + "]");
                }
                ProxyXmlWebApplicationContext.addProxyBean(beanClass, beanName);
            } else if (PropertiesFactoryBean.class.isAssignableFrom(beanClass)) {
                ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:**/" + contextLocation.getFilename());
                String beanName = bean.attributeValue("id");
                if (beanName == null) {
                    beanName = bean.attributeValue("name");
                }
                if (beanName == null) {
                    continue;
                }
                try {
                    Properties props = applicationContext.getBean(beanName, Properties.class);
                    CollectionUtils.mergePropertiesIntoMap(props, CONTEXT_PROPS);
                } catch (Exception e) {
                    logger.info("load properties[" + contextLocation.getFilename() + "] error:" + e.getMessage());
                }
            }
        }

        List<Element> importList = (List<Element>) rootElement.elements("import");
        for (Element importResource : importList) {
            Resource[] locations = PMRPR.getResources(importResource.attributeValue("resource"));
            for (Resource location : locations) {
                parseAndLoadResource(saxReader, location);
            }
        }
    }

    private void parseAndLoadResource(SAXReader saxReader, Resource location) throws IOException {
        InputStream locationInput = location.getInputStream();
        try {
            loadedProxyBeansAndInitContextProps(location, saxReader.read(locationInput).getRootElement(), saxReader);
        } catch (Exception e) {
            logger.info("parse bean error:" + e.getMessage() + " for [" + location.getFilename() + "]");
        }
        locationInput.close();
    }

    private static String getRemoteStrategy() {
        String remoteStrategy = CONTEXT_PROPS.getProperty("remote.strategy");
        if (remoteStrategy == null) {
            remoteStrategy = "HSF";
        }
        return remoteStrategy;
    }

    public static byte[] genenrateConsumerBeans(List<RemoteConsumer> consumers, List<RemoteProvider> providerList) {
        // TODO: implements DUBBO
        byte[] springBeans = getRemoteStrategy().equals("HSF") ? generateHsfConsumerBeans(consumers, providerList) : generateHsfConsumerBeans(consumers, providerList);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(new String(springBeans));
        }
        return springBeans;
    }

    private byte[] generateProviderBeans(List<RemoteProvider> providers) {
        // TODO: implements DUBBO
        byte[] springBeans = getRemoteStrategy().equals("HSF") ? generateHsfProviderBeans(providers) : generateHsfProviderBeans(providers);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(new String(springBeans));
        }
        return springBeans;
    }

    private static String getRealValue(String value) {
        if (value == null || !value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }
        String key = value.substring(2, value.length() - 1);
        if ("LIQUIDATION_VERSION".equals(key)) {
            return "1.0.0";
        }
        if ("LIQUIDATION_GROUP".equals(key)) {
            return "LIQUIDATION_DEV_GROUP";
        }
        if ("MANAGE_VERSION".equals(key)) {
            return "1.0.0";
        }
        if ("MANAGE_GROUP".equals(key)) {
            return "MANAGE_DEV_GROUP";
        }
        if ("SETTLEMENT_VERSION".equals(key)) {
            return "1.0.0";
        }
        if ("SETTLEMENT_GROUP".equals(key)) {
            return "SETTLEMENT_DEV_GROUP";
        }
        return CONTEXT_PROPS.getProperty(key);
    }

    private static byte[] generateHsfConsumerBeans(List<RemoteConsumer> consumers, List<RemoteProvider> providerList) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(output);
        Map<String, String> providerInterfaces = new HashMap<String, String>();
        for (RemoteProvider provider : providerList) {
            String providerVersion = getRealValue(provider.getVersion());
            String providerGroup = getRealValue(provider.getGroup());
            providerInterfaces.put(provider.getInterface() + ":" + providerVersion + ":" + providerGroup, provider.getRef());
        }
        print.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        print.println("<beans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        print.println("xmlns:hsf=\"http://www.taobao.com/hsf\"");
        print.println("xmlns=\"http://www.springframework.org/schema/beans\"");
        print.println("xsi:schemaLocation=\"http://www.springframework.org/schema/beans");
        print.println("http://www.springframework.org/schema/beans/spring-beans-2.5.xsd");
        print.println("http://www.taobao.com/hsf");
        print.println("http://www.taobao.com/hsf/hsf.xsd\">");
        for (RemoteConsumer consumer : consumers) {
            String consumerVersion = getRealValue(consumer.getVersion());
            String consumerGroup = getRealValue(consumer.getGroup());
            if (providerInterfaces.containsKey(consumer.getInterface() + ":" + consumerVersion + ":" + consumerGroup)) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("consumer[" + consumer.getInterface() + ":" + consumerVersion + ":" + consumerGroup + "]'s provider exists; Do not publish consumer.");
                }
                continue;
            }
            String beanId = getRealValue(consumer.getBeanId());
            if (beanId == null || "".equals(beanId)) {
                throw new RuntimeException("consumerId required[" + consumer.getInterface());
            }

            if (providerInterfaces.containsValue(beanId)) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("consumer[" + beanId + ":" + consumer.getInterface() + ":" + consumer.getVersion() + ":" + consumer.getGroup() + "]'s conflict.");
                }
                continue;
            }

            print.print("<hsf:consumer");

            print.print(" id=\"");
            print.print(beanId);
            print.print("\"");

            print.print(" interface=\"");
            print.print(getRealValue(consumer.getInterface()));
            print.print("\"");

            print.print(" version=\"");
            print.print(getRealValue(consumer.getVersion()));
            print.print("\"");

            if (consumer.getGroup() != null && !"".equals(consumer.getGroup())) {
                print.print(" group=\"");
                print.print(getRealValue(consumer.getGroup()));
                print.print("\"");
            }

            if (consumer.getTarget() != null && !"".equals(consumer.getTarget())) {
                print.print(" target=\"");
                print.print(getRealValue(consumer.getTarget()));
                print.print("\"");
            }

            if (consumer.getClientTimeout() != 0) {
                print.print(" clientTimeout=\"");
                print.print(consumer.getClientTimeout());
                print.print("\"");
            }

            if (consumer.getConnectionNum() != 0) {
                print.print(" connectionNum=\"");
                print.print(consumer.getConnectionNum());
                print.print("\"");
            }

            print.println("></hsf:consumer>");
        }
        print.println("</beans>");
        print.flush();
        print.close();
        return output.toByteArray();
    }

    private byte[] generateHsfProviderBeans(List<RemoteProvider> providers) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(output);
        print.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        print.println("<beans xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        print.println("xmlns:hsf=\"http://www.taobao.com/hsf\"");
        print.println("xmlns=\"http://www.springframework.org/schema/beans\"");
        print.println("xsi:schemaLocation=\"http://www.springframework.org/schema/beans");
        print.println("http://www.springframework.org/schema/beans/spring-beans-2.5.xsd");
        print.println("http://www.taobao.com/hsf");
        print.println("http://www.taobao.com/hsf/hsf.xsd\">");
        for (RemoteProvider provider : providers) {
            String providerRef = getRealValue(provider.getRef());
            if (providerRef == null || "".equals(providerRef)) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Provider [" + getRealValue(provider.getInterface()) + ":" + getRealValue(provider.getVersion()) + "] ref not exists.");
                }
                continue;
            }
            print.print("<hsf:provider");

            print.print(" id=\"");
            print.print(providerRef + "-" + getRealValue(provider.getVersion()) + "-provider");
            print.print("\"");

            print.print(" interface=\"");
            print.print(getRealValue(provider.getInterface()));
            print.print("\"");

            print.print(" ref=\"");
            print.print(getRealValue(provider.getRef()));
            print.print("\"");

            print.print(" version=\"");
            print.print(getRealValue(provider.getVersion()));
            print.print("\"");

            if (provider.getGroup() != null && !"".equals(provider.getGroup())) {
                print.print(" group=\"");
                print.print(getRealValue(provider.getGroup()));
                print.print("\"");
            }

            if (provider.getClientTimeout() != 0) {
                print.print(" clientTimeout=\"");
                print.print(provider.getClientTimeout());
                print.print("\"");
            }

            if (provider.getCorePoolSize() != 0) {
                print.print(" corePoolSize=\"");
                print.print(provider.getCorePoolSize());
                print.print("\"");
            }

            if (provider.getMaxPoolSize() != 0) {
                print.print(" maxPoolSize=\"");
                print.print(provider.getMaxPoolSize());
                print.print("\"");
            }

            if (provider.getSerializeType() != null && !"".equals(provider.getSerializeType())) {
                print.print(" serializeType=\"");
                print.print(getRealValue(provider.getSerializeType()));
                print.print("\"");
            }

            print.println("></hsf:provider>");
        }
        print.println("</beans>");
        print.flush();
        print.close();
        return output.toByteArray();
    }
}
