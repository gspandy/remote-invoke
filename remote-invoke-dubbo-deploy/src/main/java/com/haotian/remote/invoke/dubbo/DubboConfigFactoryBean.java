package com.haotian.remote.invoke.dubbo;

import com.alibaba.dubbo.config.ProtocolConfig;
import com.haotian.remote.ProxyXmlWebApplicationContext;
import org.springframework.beans.factory.FactoryBean;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class DubboConfigFactoryBean implements FactoryBean {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    public Object getObject() throws Exception {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName(getValue("${protocol.name}", "dubbo"));
        protocolConfig.setThreads(Integer.parseInt(getValue("${protocol.threads}", "200")));
        int port = Integer.parseInt(getValue("${protocol.port}", "12200"));
        int offset = 20;
        for (int i = 0; i < offset; i++) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.close();
                break;
            } catch (IOException e) {
                logger.info("PORT[" + port + "] is used increment port");
                ++port;
            }
        }
        if (Integer.parseInt(getValue("${protocol.port}", "12200")) + offset == port) {
            throw new IllegalStateException("no port for dubbo server");
        }
        protocolConfig.setPort(port);
        return protocolConfig;
    }

    private String getValue(String key, String defaultValue) {
        String value = null;
        try {
            value = ProxyXmlWebApplicationContext.getRealValue(key);
        } catch (Throwable e) {
            logger.warning(key + "not config, user default value[" + defaultValue + "]");
            value = defaultValue;
        }
        if (value == null || "".equals(value)) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public Class getObjectType() {
        return ProtocolConfig.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
