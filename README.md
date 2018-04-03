# 概述

> remote-invoke是使用注解代替HSF/DUBBO配置文件的插件，可以通过配置自由切换是HSF还是DUBBO。

插件配置参数：

| 参数名              | 参数描述                              |
| ------------------- | ------------------------------------- |
| remote.strategy     | 服务发布、调用策略HSF\|DUBBO，默认HSF |
| remote.scan.package | 注解扫描包路径[包名]，默认全部        |
|                     |                                       |

# 使用说明

## 接口注解说明

com.haotian.remote.ProxyConsumer：用于将接口标志为远程接口，即服务消费者。

参数见下表，其中String型参数可使用变量${变量名}，变量需在属性文件中配置

| 属性          | 属性说明                             |
| ------------- | ------------------------------------ |
| beanId        | 接口在spring中的id，与实现的id要一致 |
| version       | 服务版本号                           |
| clientTimeout | 超时时间                             |
| connectionNum | 连接数，默认1                        |
| group         | 服务分组                             |
| target        | 指定路由地址                         |

**样例：**

```
@ProxyConsumer(beanId = "addressInfoAdaptor", group = "${hsf.group}", version = "${hsf.version}")
public interface AddressInfoAdaptor {
    public AddressInfoBO showAddressInfo();
}
```



## 实现使用说明

com.haotian.remote.ProxyProvider：用于将实现标志为远程实现，即服务提供者。

参数见下表，其中String型参数可使用变量${变量名}，变量需在属性文件中配置

| 属性          | 属性说明              |
| ------------- | --------------------- |
| serializeType | 传输协议，默认hession |
| version       | 服务版本号            |
| clientTimeout | 超时时间              |
| corePoolSize  | 线程池初始值          |
| maxPoolSize   | 线程池最大值          |
| group         | 服务分组              |

**样例：**

```
@ProxyProvider(group = "TEST", version = "${hsf.version}")
public class AddressInfoServiceImpl implements AddressInfoService {
}
```

## 扩展机制

remote-invoke可以将其它类型的服务包装为HSF/DUBBO服务，可使用com.haotian.remote.RemoteHandler注解完成此定制。

**注意** 此注解只能在工程的实现包，即服务提供者包使用。

参数见下表：

| 属性     | 属性说明                                                     |
| -------- | ------------------------------------------------------------ |
| value    | 扩展处理类，需实现接口com.haotian.remote.RemoteInvokeHandler |
| addition | 扩展处理类所需的其他未知属性                                 |

**将整个接口包装为HSF/DUBBO服务**

*实现定义:*

```
@ProxyProvider(group = "${hsf.group}", version = "${hsf.version}")
@RemoteHandler(value = InterfaceInvokeHandler.class)
public interface AddressInfoAdaptorImpl extends AddressInfoAdaptor {
}
```

*扩展处理类:*

```
public class InterfaceInvokeHandler implements RemoteInvokeHandler {
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        AddressInfoBO addressInfoBO = new AddressInfoBO();
        addressInfoBO.setAddress(InterfaceInvokeHandler.class.getSimpleName());
        return addressInfoBO;
    }

    @Override
    public boolean support(Class<?> targetClass) {
        return targetClass.isInterface();
    }
}
```

**将整个实现包装为HSF/DUBBO服务** 

*实现定义:*

```
@ProxyProvider(group = "TEST", version = "${hsf.version}")
@RemoteHandler(value = ClassInvokeHandler.class)
public class AddressInfoServiceImpl implements AddressInfoService {
}
```

*扩展处理类:*

```
public class ClassInvokeHandler implements RemoteInvokeHandler {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(bean, args);
        logger.info("ClassInvokeHandler:" + result);
        return result;
    }

    @Override
    public boolean support(Class<?> targetClass) {
        return !targetClass.isInterface();
    }
}
```

**将某个方法包装为HSF/DUBBO服务** 

*实现定义:*

```
@ProxyProvider(group = "TEST", version = "${hsf.version}")
public class AddressInfoServiceImpl implements AddressInfoService {
	@Override
    @RemoteHandler(value = MethodInvokeHandler.class, addition = "/invoke/test.do")
    public List<AddressInfoBO> queryList(String name, int age) {
        return null;
    }
}
```

扩展处理类:

```
public class MethodInvokeHandler implements RemoteInvokeHandler {
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        List<AddressInfoBO> addressList = new ArrayList<AddressInfoBO>();
        AddressInfoBO infoBO = new AddressInfoBO();
        infoBO.setAddress("MethodInvokeHandler");
        addressList.add(infoBO);
        return addressList;
    }

    @Override
    public boolean support(Class<?> targetClass) {
        return !targetClass.isInterface();
    }
}
```

**注意：** 当实现在类注解与方法注解都加入了RemoteHandler注解，方法的注解优先级高，即方法注解生效。