package com.haotian.remote;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产者、消费者辅助类：主要用于扫描当前工程中生产者与消费者注解并将其暴露给外部使用
 *
 * @author liuzy
 */
public class AnnotationUtil {
    private static final PathMatchingResourcePatternResolver PMRPR = new PathMatchingResourcePatternResolver(AnnotationUtil.class.getClassLoader());

    /**
     * 获取工程中所有消费者
     *
     * @return 消息者列表
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<RemoteConsumer> extractProxyConsumers() throws IOException, ClassNotFoundException {
        List<String> consumerList = extractAnnotationClazzes(ProxyConsumer.class, "classpath*:**/*.class");
        List<RemoteConsumer> proxyConsumerList = new ArrayList<RemoteConsumer>(consumerList.size());
        for (String consumer : consumerList) {
            proxyConsumerList.add(new RemoteConsumer(Class.forName(consumer)));
        }
        return proxyConsumerList;
    }

    /**
     * 获取工程中所有生产者
     *
     * @return 生产者列表
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<RemoteProvider> extractProxyProviders() throws IOException, ClassNotFoundException {
        List<String> providerList = extractAnnotationClazzes(ProxyProvider.class, "classpath*:**/*.class");
        List<RemoteProvider> proxyProviderList = new ArrayList<RemoteProvider>(providerList.size());
        for (String provider : providerList) {
            proxyProviderList.add(new RemoteProvider(Class.forName(provider)));
        }
        return proxyProviderList;
    }

    private static List<String> extractAnnotationClazzes(final Class<?> annotationClazz, final String scanPath) throws IOException, ClassNotFoundException {
        Resource[] clazzes = PMRPR.getResources(scanPath);
        final List<String> clazzList = new ArrayList<String>();
        final ClassVisitor anaotationClassVisitor = new ClassVisitor(327680) {
            private final String PROXY_REMOTE_INTERFACE = "L" + annotationClazz.getName().replaceAll("\\.", "/") + ";";
            private String clazz;

            @Override
            public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
                super.visit(i, i1, s, s1, s2, strings);
                clazz = s;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b) {
                if (PROXY_REMOTE_INTERFACE.equals(s)) {
                    clazzList.add(clazz.replaceAll("/", "."));
                }
                return super.visitAnnotation(s, b);
            }
        };
        for (Resource clazz : clazzes) {
            ClassReader cr = new ClassReader(clazz.getInputStream());
            cr.accept(anaotationClassVisitor, 0);
        }
        return clazzList;
    }
}
