package com.qinwei.deathnote.context.event;

import com.qinwei.deathnote.beans.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * @author qinwei
 * @date 2019-06-11
 */
@Slf4j
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    private Executor taskExecutor;

    private ErrorHandler errorHandler;

    public SimpleApplicationEventMulticaster() {
    }

    public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
        setBeanFactory(beanFactory);
    }


    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected Executor getTaskExecutor() {
        return this.taskExecutor;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    protected ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        for (ApplicationListener<?> listener : getApplicationListeners(event)) {
            Executor executor = getTaskExecutor();
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                invokeListener(listener, event);
            }
        }
    }

    protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        ErrorHandler errorHandler = getErrorHandler();
        if (errorHandler != null) {
            try {
                doInvokeListener(listener, event);
            } catch (Exception e) {
                errorHandler.handleError(e);
            }
        } else {
            doInvokeListener(listener, event);
        }
    }

    private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
        try {
            listener.onApplicationEvent(event);
        } catch (Exception e) {
            log.error("Unable to invoke ApplicationListener.onApplicationEvent()", e);
            throw e;
        }
    }
}
