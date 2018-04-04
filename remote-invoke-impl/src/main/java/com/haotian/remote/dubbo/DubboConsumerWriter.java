package com.haotian.remote.dubbo;

import com.haotian.remote.AbstractConsumerWriter;

public class DubboConsumerWriter extends AbstractConsumerWriter {

    @Override
    public String strategy() {
        return "DUBBO";
    }
}
