package com.haotian.remote;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class RemoteProviderFactoryBean implements FactoryBean, InitializingBean {
    private Object target;
    private Class targetClass;
    private RemoteFactoryBean remoteFactoryBean;

    public void setRemoteFactoryBean(RemoteFactoryBean remoteFactoryBean) {
        this.remoteFactoryBean = remoteFactoryBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.target = remoteFactoryBean.getObject();
        this.targetClass = this.target.getClass();
    }

    @Override
    public Object getObject() throws Exception {
        return this.target;
    }

    @Override
    public Class getObjectType() {
        return this.targetClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
