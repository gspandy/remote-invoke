package com.haotian.remote;

import com.haotian.plugins.config.PropertiesBeanFactory;
import com.haotian.remote.dubbo.DubboConsumerWriter;
import com.haotian.remote.dubbo.DubboProviderWriter;
import com.haotian.remote.hsf.HsfConsumerWriter;
import com.haotian.remote.hsf.HsfProviderWriter;
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
    private static final Set<String> BEAN_NAMES_HOLDER = new HashSet<String>();
    private static final Map<String, ConsumerWriter> CONSUMER_STORE = new HashMap<String, ConsumerWriter>();
    private static final Map<String, ProviderWriter> PROVIDER_STORE = new HashMap<String, ProviderWriter>();

    static {
        ConsumerWriter consumerWriter = new HsfConsumerWriter();
        CONSUMER_STORE.put(consumerWriter.strategy(), consumerWriter);
        consumerWriter = new DubboConsumerWriter();
        CONSUMER_STORE.put(consumerWriter.strategy(), consumerWriter);
        ProviderWriter providerWriter = new HsfProviderWriter();
        PROVIDER_STORE.put(providerWriter.strategy(), providerWriter);
        providerWriter = new DubboProviderWriter();
        PROVIDER_STORE.put(providerWriter.strategy(), providerWriter);
    }

    private static final void addProxyBean(Class<?> beanClass, String beanName) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("loaded proxy bean[beanName:" + beanName + ", class:" + beanClass.getName() + "].");
        }
        if (!BEAN_NAMES_HOLDER.contains(beanName)) {
            BEAN_NAMES_HOLDER.add(beanName);
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

    private static String getRemoteScanPackageName() {
        String scanPackage = CONTEXT_PROPS.getProperty("remote.scan.package");
        String scanTip = "[remote.scan.package] not configured in properties file. scan all package for remote invoke";
        if (!(scanPackage == null || "".equals(scanPackage))) {
            scanTip = "scan package[" + scanPackage + "] for remote invoke";
        }
        logger.info(scanTip);
        return scanPackage;
    }

    public static List<RemoteProvider> extractProviderList() {
        List<RemoteProvider> providerList;
        try {
            providerList = AnnotationUtil.extractProxyProviders(getRemoteScanPackageName());
        } catch (Exception e) {
            throw new RuntimeException("extract proxyprovider error", e);
        }
        return providerList;
    }

    public static List<RemoteConsumer> extractConsumerList() {
        List<RemoteConsumer> consumerList;
        try {
            consumerList = AnnotationUtil.extractProxyConsumers(getRemoteScanPackageName());
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
            } else if (PropertiesFactoryBean.class.isAssignableFrom(beanClass) || PropertiesBeanFactory.class.isAssignableFrom(beanClass)) {
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

    public static String getRemoteStrategy() {
        String remoteStrategy = CONTEXT_PROPS.getProperty("remote.strategy");
        if (remoteStrategy == null) {
            remoteStrategy = "HSF";
            logger.warning("[remote.strategy] not configured in properties file. use [" + remoteStrategy + "] as default invoke strategy");
        }
        return remoteStrategy;
    }

    public static byte[] genenrateConsumerBeans(List<RemoteConsumer> consumers, List<RemoteProvider> providerList) {
        Map<String, String> providerInterfaces = new HashMap<String, String>();
        for (RemoteProvider provider : providerList) {
            providerInterfaces.put(provider.getInterface() + ":" + provider.getVersion() + ":" + provider.getGroup(), provider.getRef());
        }
        ConsumerWriter consumerWriter = CONSUMER_STORE.get(getRemoteStrategy());
        consumerWriter.beforeWrite();
        for (RemoteConsumer consumer : consumers) {
            if (providerInterfaces.containsKey(consumer.getInterface() + ":" + consumer.getVersion() + ":" + consumer.getGroup())) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("consumer[" + consumer.getInterface() + ":" + consumer.getVersion() + ":" + consumer.getGroup() + "]'s provider exists; Do not publish consumer.");
                }
                continue;
            }
            if (consumer.getBeanId() == null || "".equals(consumer.getBeanId())) {
                throw new RuntimeException("consumerId required[" + consumer.getInterface());
            }

            if (providerInterfaces.containsValue(consumer.getBeanId())) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("consumer[" + consumer.getBeanId() + ":" + consumer.getInterface() + ":" + consumer.getVersion() + ":" + consumer.getGroup() + "]'s conflict.");
                }
                continue;
            }
            consumerWriter.write(consumer);
        }
        consumerWriter.afterWrite();
        byte[] springBeans = consumerWriter.toByteArray();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(new String(springBeans));
        }
        return springBeans;
    }

    private byte[] generateProviderBeans(List<RemoteProvider> providers) {
        ProviderWriter providerWriter = PROVIDER_STORE.get(getRemoteStrategy());
        providerWriter.beforeWrite();
        for (RemoteProvider provider : providers) {
            if (provider.getRef() == null || "".equals(provider.getRef())) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Provider [" + getRealValue(provider.getInterface()) + ":" + getRealValue(provider.getVersion()) + "] ref not exists.");
                }
                continue;
            }
            if (provider.getRemoteProviderFactoryBean() != null && BEAN_NAMES_HOLDER.contains(provider.getRef())) {
                throw new IllegalStateException("Class[" + provider.getInterface() + "] bean_id has defined");
            }
            providerWriter.write(provider);
        }
        providerWriter.afterWrite();
        byte[] springBeans = providerWriter.toByteArray();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(new String(springBeans));
        }
        return springBeans;
    }

    public static String getRealValue(final String value) {
        if (value == null || !value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }
        String key = value.substring(2, value.length() - 1);
        String parsedValue = CONTEXT_PROPS.getProperty(key);
        if (parsedValue == null || "".equals(parsedValue)) {
            throw new RuntimeException("extract [" + value + "] value is empty.");
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("extract [" + value + "] value is [" + parsedValue + "].");
        }
        return parsedValue;
    }

}
