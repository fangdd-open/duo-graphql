# 监控

监控器是通过模块`graphql-monitor`实现的，实现`com.fangdd.graphql.core.ExecutionMonitor`或继承重写`com.fangdd.graphql.monitor.DefaultExecutionMonitor`类方法



接口`com.fangdd.graphql.core.ExecutionMonitor`扩展了`graphql.execution.instrumentation.SimpleInstrumentation`，对`graphql-java`的监控进行了扩展。

为了不破坏它的原意，这里不对原监控接口`graphql.execution.instrumentation.Instrumentation`实现的方法做说明，详见源码内注释。

扩展的监控点详见`com.fangdd.graphql.core.ExecutionMonitor`源码。



目前，监控都还只是空方法，后续，会写个标准的实现，并提供比较丰富的UI。