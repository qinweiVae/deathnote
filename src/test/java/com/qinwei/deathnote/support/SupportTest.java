package com.qinwei.deathnote.support;

import com.qinwei.deathnote.beans.bean.CachedIntrospectionResults;
import com.qinwei.deathnote.beans.bean.Domain;
import com.qinwei.deathnote.beans.bean.RootBeanDefinition;
import com.qinwei.deathnote.beans.registry.SimpleAliasRegistry;
import com.qinwei.deathnote.config.Config;
import com.qinwei.deathnote.config.StandardConfig;
import com.qinwei.deathnote.context.support.ResolveType;
import com.qinwei.deathnote.log.MDCRunnable;
import com.qinwei.deathnote.support.annotation.AnnotationA;
import com.qinwei.deathnote.support.annotation.AnnotationB;
import com.qinwei.deathnote.support.convert.Converter;
import com.qinwei.deathnote.support.convert.StringToLongConverter;
import com.qinwei.deathnote.support.resolve.DefaultPropertyResolver;
import com.qinwei.deathnote.support.scan.ResourcesScanner;
import com.qinwei.deathnote.support.spi.MaleWorker;
import com.qinwei.deathnote.support.spi.ServiceLoader;
import com.qinwei.deathnote.support.spi.Worker;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.BeanUtils;
import com.qinwei.deathnote.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.MDC;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
        System.setProperty(CONFIG_PATH, "d:/config/test/config-test.properties");
        ResourcesScanner scanner = ResourcesScanner.getInstance();
        scanner.scan().forEach((s, s2) -> System.out.println(s + ":" + s2));
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
        aliasRegistry.registerAlias("AnnotationB", "AnnotationA");
        aliasRegistry.registerAlias("AnnotationB", "1");
        aliasRegistry.registerAlias("AnnotationA", "C");
        //循环引用
        //aliasRegistry.registerAlias("C", "AnnotationA");
        aliasRegistry.registerAlias("C", "D");
        System.out.println("B的别名:" + Arrays.toString(aliasRegistry.getAliases("AnnotationB")));
        System.out.println("A的别名:" + Arrays.toString(aliasRegistry.getAliases("AnnotationA")));
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

    @Test
    public void testPropertyResolver() {
        DefaultPropertyResolver resolver = new DefaultPropertyResolver();
        String text = "s${a1}2n${b}m${c2a1}v${a1}";
        //String text = "${a1}2nb}m${c2a1}";
        for (String placeholder : resolver.findPlaceholders(text)) {
            System.out.println(placeholder);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("a1", "Aone");
        map.put("b", "AnnotationB");
        map.put("c2a1", "CtwoAone");
        System.out.println(resolver.resolvePlaceholders(text, map));
    }

    @Test
    public void testAnnotation() throws Exception {
        System.out.println(AnnotationUtils.findAnnotation(Domain.class, AnnotationA.class));
        System.out.println(AnnotationUtils.findAnnotation(Domain.class, AnnotationA.class, false));
        System.out.println(AnnotationUtils.findAnnotation(Domain.class, AnnotationB.class));
        System.out.println(AnnotationUtils.findAnnotation(Domain.class, AnnotationB.class, false));

        System.out.println("-----------------------------------------");

        System.out.println(AnnotationUtils.findAnnotation(Domain.class.getMethod("toString"), AnnotationA.class));
        System.out.println(AnnotationUtils.findAnnotation(Domain.class.getDeclaredField("beanName"), AnnotationA.class));

        System.out.println("-----------------------------------------");
        System.out.println(AnnotationUtils.hasAnnotation(Domain.class.getMethod("toString"), AnnotationA.class.getName()));

        System.out.println("-----------------------------------------");
        AnnotationUtils.getAnnotationAttributes(Domain.class.getMethod("toString"), AnnotationA.class.getName(), true)
                .forEach((s, o) -> System.out.println(s + ":" + o));

    }

    @Test
    public void testFindGenericType() throws Exception {

        Converter<String, Long> converter = new StringToLongConverter();
        System.out.println(ResolveType.forType(converter.getClass()).resolveGeneric(0));
        System.out.println(ResolveType.forType(converter.getClass()).resolveGeneric(1));
        System.out.println("-------------------------");

        System.out.println(ResolveType.forType(StringToLongConverter.class).resolveGeneric(0));
        System.out.println(ResolveType.forType(StringToLongConverter.class).resolveGeneric(1));
        //System.out.println(ResolveType.forType(StringToLongConverter.class).resolveGeneric(2));
        System.out.println("-------------------------");

        PropertyDescriptor someList = BeanUtils.getPropertyDescriptor(Domain.class, "someList");
        System.out.println(ResolveType.forType(someList).resolveGenericType(0));
        //System.out.println(ResolveType.forType(someList).resolveGenericType(1));
        System.out.println("-------------------------");

        PropertyDescriptor someMap = BeanUtils.getPropertyDescriptor(Domain.class, "someMap");
        System.out.println(ResolveType.forType(someMap).resolveGenericType(0));
        System.out.println(ResolveType.forType(someMap).resolveGenericType(1));
        System.out.println("-------------------------");

        Field filed = Domain.class.getDeclaredField("someSet");
        System.out.println(ResolveType.forType(filed).resolveGenericType(0));
        //System.out.println(ResolveType.forType(filed).resolveGenericType(1));
        System.out.println("-------------------------");

        Method method = Domain.class.getDeclaredMethod("writeMethod", Set.class, Collection.class);
        //只能拿到第一个参数的泛型
        System.out.println(ResolveType.forType(method).resolveGenericType(0));

    }
}
