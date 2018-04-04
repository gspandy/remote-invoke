package com.haotian.remote.hsf;

import com.haotian.remote.AbstractConsumerWriter;

public class HsfConsumerWriter extends AbstractConsumerWriter {
    @Override
    public String strategy() {
        return "HSF";
    }
}
