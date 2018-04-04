package com.haotian.remote.dubbo;

import com.haotian.remote.AbstractProviderWriter;

public class DubboProviderWriter extends AbstractProviderWriter {
    @Override
    public String strategy() {
        return "DUBBO";
    }
}
