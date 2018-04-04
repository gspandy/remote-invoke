package com.haotian.remote;

import com.haotian.remote.ConsumerWriter;
import com.haotian.remote.ProxyConsumerDemo;
import com.haotian.remote.RemoteConsumer;
import com.haotian.remote.dubbo.DubboConsumerWriter;
import com.haotian.remote.hsf.HsfConsumerWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ConsumerWriterTest {
    ConsumerWriter consumerWriter;
    @Test
    public void testHsfConsumer() throws IOException {
        consumerWriter = new HsfConsumerWriter();
        consumerWriter.beforeWrite();
        RemoteConsumer remoteConsumer = new RemoteConsumer(ProxyConsumerDemo.class);
        Assert.assertEquals(ProxyConsumerDemo.class.getName(), remoteConsumer.getInterface());
        consumerWriter.write(remoteConsumer);
        consumerWriter.afterWrite();
        System.out.write(consumerWriter.toByteArray());
    }

    @Test
    public void testDubboConsumer() throws IOException {
        consumerWriter = new DubboConsumerWriter();
        consumerWriter.beforeWrite();
        RemoteConsumer remoteConsumer = new RemoteConsumer(ProxyConsumerDemo.class);
        Assert.assertEquals(ProxyConsumerDemo.class.getName(), remoteConsumer.getInterface());
        consumerWriter.write(remoteConsumer);
        consumerWriter.afterWrite();
        System.out.write(consumerWriter.toByteArray());
    }

}
