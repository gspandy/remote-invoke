package com.haotian.remote;

@ProxyProvider(group = "HSF", version = "1.0")
@RemoteHandler(value = RemoteInvodeHandlerDemo.class)
public class ProxyProviderDemoProvider implements ProxyConsumerDemo {
}
