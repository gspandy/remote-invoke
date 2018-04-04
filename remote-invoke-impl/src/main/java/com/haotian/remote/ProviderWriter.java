package com.haotian.remote;

import java.util.List;

public interface ProviderWriter {
    void beforeWrite();

    void write(RemoteProvider provider);

    void afterWrite();

    String strategy();

    byte[] toByteArray();
}
