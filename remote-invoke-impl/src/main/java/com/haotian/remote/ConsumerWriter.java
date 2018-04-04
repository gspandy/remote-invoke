package com.haotian.remote;

import java.util.List;

public interface ConsumerWriter {
    void beforeWrite();

    void write(RemoteConsumer consumer);

    void afterWrite();

    String strategy();

    byte[] toByteArray();

}
