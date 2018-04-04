package com.haotian.remote;

import com.haotian.remote.ProviderWriter;
import com.haotian.remote.ProxyProviderDemoProvider;
import com.haotian.remote.RemoteProvider;
import com.haotian.remote.dubbo.DubboProviderWriter;
import com.haotian.remote.hsf.HsfProviderWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ProviderWriterTest {
    ProviderWriter providerWriter;
    @Test
    public void testHsfProvider() throws IOException {
        providerWriter = new HsfProviderWriter();
        providerWriter.beforeWrite();
        RemoteProvider remoteProvider = new RemoteProvider(ProxyProviderDemoProvider.class);
        providerWriter.write(remoteProvider);
        providerWriter.afterWrite();
        System.out.write(providerWriter.toByteArray());
    }

    @Test
    public void testDubboProvider() throws IOException {
        providerWriter = new DubboProviderWriter();
        providerWriter.beforeWrite();
        RemoteProvider remoteProvider = new RemoteProvider(ProxyProviderDemoProvider.class);
        providerWriter.write(remoteProvider);
        providerWriter.afterWrite();
        System.out.write(providerWriter.toByteArray());
    }
}
