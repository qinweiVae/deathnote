package com.qinwei.deathnote.support;

import com.google.common.annotations.VisibleForTesting;
import com.qinwei.deathnote.beans.bean.CachedIntrospectionResults;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.registry.SimpleAliasRegistry;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.log.MDCRunnable;
import com.qinwei.deathnote.support.scan.MethodAnnotationScanner;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import com.qinwei.deathnote.support.spi.MaleWorker;
import com.qinwei.deathnote.support.spi.ServiceLoader;
import com.qinwei.deathnote.support.spi.Worker;
import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.MDC;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.qinwei.deathnote.support.scan.ResourcesScanner.CONFIG_PATH;

/**
 * @author qinwei
 * @date 2019-05-14
 */
@Slf4j
public class SupportTest {

    @Test
    public void testConfig() {
        System.setProperty(CONFIG_PATH, "d:/config");
        System.setProperty("qinwei.date", "2019-05-14 14:09:20");
        Config config = StandardConfig.getInstance();
        config.initConfig();
        System.out.println(config.getProperty("qinwei.date", Date.class, new Date()));
        System.out.println(config.getProperty("author"));
    }

    @Test
    public void testScan() {
        System.setProperty(CONFIG_PATH, "d:/config/config-test.properties");
        ResourcesScanner scanner = ResourcesScanner.getInstance();
        scanner.scan().forEach((s, s2) -> System.out.println(s + ":" + s2));
        MethodAnnotationScanner methodAnnotationScanner = new MethodAnnotationScanner();
        Set<Method> classes = methodAnnotationScanner.scan(VisibleForTesting.class, "com.google");
        classes.forEach(aClass -> System.out.println(aClass));
    }

    @Test
    public void testLog() {
        // 入口传入请求ID
        MDC.put("requestId", UUID.randomUUID().toString());

        // 异步线程打印日志，用MDCRunnable装饰Runnable
        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        new Thread(new MDCRunnable(() -> log.info("log in other thread"))).start();

        // 出口移除请求ID
        log.info("start success ...");
        MDC.remove("requestId");
    }

    @Test
    public void testInstantiateClass() throws NoSuchMethodException {
        ClassUtils.instantiateClass(MaleWorker.class);
        ClassUtils.instantiateClass(MaleWorker.class.getConstructor(String.class), "qw");
        ClassUtils.instantiateClass(MaleWorker.class.getConstructor(String.class), "qw", 2);
    }

    @Test
    public void testSortConstructors() {
        Constructor<?>[] constructors = MaleWorker.class.getDeclaredConstructors();
        ClassUtils.sortConstructors(constructors);
        for (Constructor<?> constructor : constructors) {
            System.out.println(constructor);
        }
        Object[] args = new Object[3];
        args[0] = "qw";
        args[1] = 2;
        args[2] = true;
        System.out.println(ClassUtils.instantiateClass(constructors[0], args));
    }

    @Test
    public void testSpi() {
        Worker worker = ServiceLoader.getService(Worker.class);
        worker.work();
        System.out.println(worker == ServiceLoader.getService(Worker.class, false));
        Worker female = ServiceLoader.getService(Worker.class, "female");
        female.work();
        System.out.println(female == ServiceLoader.getService(Worker.class, "female"));
        System.out.println(female == ServiceLoader.getService(Worker.class, "female", false));
        Worker child = ServiceLoader.getService(Worker.class, "child");
        child.work();
    }

    @Test
    public void testSimpleAliasRegistry() {
        SimpleAliasRegistry aliasRegistry = new SimpleAliasRegistry();
        aliasRegistry.registerAlias("B", "A");
        aliasRegistry.registerAlias("B", "1");
        aliasRegistry.registerAlias("A", "C");
        //循环引用
        //aliasRegistry.registerAlias("C", "A");
        aliasRegistry.registerAlias("C", "D");
        System.out.println("B的别名:" + Arrays.toString(aliasRegistry.getAliases("B")));
        System.out.println("A的别名:" + Arrays.toString(aliasRegistry.getAliases("A")));
        System.out.println("C的别名:" + Arrays.toString(aliasRegistry.getAliases("C")));
    }

    @Test
    public void testIntrospector() throws IntrospectionException {
        CachedIntrospectionResults results = CachedIntrospectionResults.forClass(RootBeanDefinition.class);
        Map<String, PropertyDescriptor> cache = results.getPropertyDescriptorCache();
        for (Map.Entry<String, PropertyDescriptor> entry : cache.entrySet()) {
            System.out.println(entry.getKey());
            PropertyDescriptor pd = entry.getValue();
            System.out.println(pd.getReadMethod());
            System.out.println(pd.getWriteMethod());
            System.out.println("----------------------------");
        }
    }
}
