package com.haotian.remote;

public class RemoteConsumer {
    private String beanId;
    private String version;
    private long clientTimeout;
    private int connectionNum;
    private String intface;
    private String group;
    private String target;

    protected RemoteConsumer(Class<?> consumerClass) {
        ProxyConsumer proxyConsumer = consumerClass.getAnnotation(ProxyConsumer.class);
        this.version = proxyConsumer.version();
        this.clientTimeout = proxyConsumer.clientTimeout();
        this.connectionNum = proxyConsumer.connectionNum();
        this.intface = consumerClass.getName();
        this.beanId = proxyConsumer.beanId();
        this.group = proxyConsumer.group();
        this.target = proxyConsumer.target();
    }

    public String getTarget() {
        return target;
    }

    public String getBeanId() {
        return beanId;
    }

    public String getVersion() {
        return version;
    }

    public long getClientTimeout() {
        return clientTimeout;
    }

    public int getConnectionNum() {
        return connectionNum;
    }

    public String getInterface() {
        return intface;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "RemoteConsumer{" +
                "beanId='" + beanId + '\'' +
                ", version='" + version + '\'' +
                ", clientTimeout=" + clientTimeout +
                ", connectionNum=" + connectionNum +
                ", interface='" + intface + '\'' +
                ", group='" + group + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
