package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.context.ApplicationContext;
import com.qinwei.deathnote.context.annotation.EventListener;
import com.qinwei.deathnote.context.support.BridgeMethodResolver;
import com.qinwei.deathnote.utils.AnnotationUtils;
import com.qinwei.deathnote.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author qinwei
 * @date 2019-06-26
 */
public class ApplicationListenerMethodAdapter implements ApplicationListener<ApplicationEvent> {

    private final String beanName;

    private final Method method;

    private final List<Class> declaredEventTypes;

    private final Method bridgedMethod;

    private ApplicationContext applicationContext;

    public ApplicationListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
        this.beanName = beanName;
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        Method targetMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        EventListener ann = AnnotationUtils.findAnnotation(targetMethod, EventListener.class);
        this.declaredEventTypes = resolveDeclaredEventTypes(method, ann);
    }

    private List<Class> resolveDeclaredEventTypes(Method method, EventListener eventListener) {
        int count = method.getParameterTypes().length;
        // 方法最多只能有一个 参数
        if (count > 1) {
            throw new IllegalStateException("Maximum one parameter is allowed for event listener method: " + method);
        }
        // 如果 方法上有 @EventListener 且 classes 有值
        if (eventListener != null && eventListener.classes().length > 0) {
            List<Class> types = new ArrayList<>(eventListener.classes().length);
            for (Class<?> eventType : eventListener.classes()) {
                types.add(eventType);
            }
            return types;
        }
        // 否则的话，方法必须有一个参数
        else {
            if (count == 0) {
                throw new IllegalStateException("Event parameter is mandatory for event listener method: " + method);
            }
            return Collections.singletonList(method.getParameterTypes()[0]);
        }
    }

    public void init(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        processEvent(event);
    }

    public void processEvent(ApplicationEvent event) {
        Object[] args = resolveArguments(event);
        if (shouldHandle(event, args)) {
            Object result = doInvoke(args);
            // 如果返回值不是 null ，将返回值进行发布
            if (result != null) {
                handleResult(result);
            }
        }
    }

    private void handleResult(Object result) {
        publishEvent(result);
    }

    private void publishEvent(Object event) {
        if (event != null) {
            assert this.applicationContext != null : "ApplicationContext must not be null";
            this.applicationContext.publishEvent(event);
        }
    }

    private Object doInvoke(Object[] args) {
        Object bean = this.applicationContext.getBean(this.beanName);
        ClassUtils.makeAccessible(this.bridgedMethod);
        try {
            return this.bridgedMethod.invoke(bean, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke event listener method", e);
        }
    }

    private boolean shouldHandle(ApplicationEvent event, Object[] args) {
        return args != null;
    }

    private Object[] resolveArguments(ApplicationEvent event) {
        Class resolveType = getResolveType(event);
        if (resolveType == null) {
            return null;
        }
        if (this.method.getParameterTypes().length == 0) {
            return new Object[0];
        }
        if (!ApplicationEvent.class.isAssignableFrom(resolveType) &&
                event instanceof PayloadApplicationEvent) {
            return new Object[]{((PayloadApplicationEvent) event).getPayload()};
        } else {
            return new Object[]{event};
        }
    }

    private Class getResolveType(ApplicationEvent event) {
        for (Class eventType : this.declaredEventTypes) {
            if (event instanceof PayloadApplicationEvent) {
                PayloadApplicationEvent payloadApplicationEvent = (PayloadApplicationEvent) event;
                if (ClassUtils.isAssignable(eventType, payloadApplicationEvent.getPayload().getClass())) {
                    return eventType;
                }
            } else {
                if (ClassUtils.isAssignable(eventType, event.getClass())) {
                    return eventType;
                }
            }
        }
        return null;
    }
}
